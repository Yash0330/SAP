package service;

import exceptions.DownloadException;
import interfaces.Downloader;
import models.DownloadOptions;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DownloaderImpl implements Downloader {
    private final int numParallelParts;
    private final int numParallelFiles;

    private final Path downloadDir;

    private final ExecutorService executorService;

    private final Map<String, String> expectedChecksums;

    public DownloaderImpl(DownloadOptions downloadOptions, Map<String, String> expectedChecksums) {
        this.numParallelParts = downloadOptions.numParallelParts();
        this.numParallelFiles = downloadOptions.numParallelFiles();
        this.downloadDir = downloadOptions.downloadDir();
        this.expectedChecksums = expectedChecksums;
        this.executorService = Executors.newFixedThreadPool(numParallelFiles);
    }

    @Override
    public List<Path> download(List<String> urls) throws DownloadException {
        try {
            Files.createDirectories(downloadDir);
            List<Future<Path>> futures = urls.stream()
                    .map(url -> executorService.submit(() -> downloadFile(url))).toList();
            executorService.shutdown();

            List<Path> downloadedFiles = futures.stream().map(this::getFutureResult).toList();

            for (Path file : downloadedFiles) {
                String fileUrl = "http://localhost:8080/" + file.getFileName();
                validateChecksum(file, expectedChecksums.get(fileUrl));
            }

            return downloadedFiles;
        } catch (Exception e) {
            throw new DownloadException(e.getMessage(), e);
        }
    }

    private Path downloadFile(String fileUrl) throws Exception {
        String fileName = Paths.get(new URI(fileUrl).getPath()).getFileName().toString();
        Path filePath = downloadDir.resolve(fileName);
        HttpClient client = HttpClient.newBuilder().executor(Executors.newFixedThreadPool(numParallelParts)).build();
        long fileSize = getFileSize(client, fileUrl);

        RandomAccessFile ref = new RandomAccessFile(filePath.toFile(), "rw");
        ref.setLength(fileSize);
        ref.close();

        ExecutorService partExecutor = Executors.newFixedThreadPool(numParallelParts);
        for (long start = 0; start < fileSize; start += (fileSize / numParallelParts)) {
            long end = Math.min(start + (fileSize / numParallelParts)-1, fileSize-1);
            partExecutor.execute(new DownloadTask(fileUrl, filePath, start, end));
        }
        partExecutor.shutdown();
        partExecutor.awaitTermination(1, TimeUnit.HOURS);
        return filePath;
    }

    private long getFileSize(HttpClient client, String fileUrl) throws Exception {
        HttpRequest headRequest = HttpRequest.newBuilder().uri(URI.create(fileUrl)).method("HEAD",
                HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<Void>  headResponse = client.send(headRequest, HttpResponse.BodyHandlers.discarding());
        return Long.parseLong(headResponse.headers().firstValue("Content-Length").orElseThrow());
    }

    private Path getFutureResult(Future<Path> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException("Download failed", e);
        }
    }

    private void validateChecksum(Path file, String expectedChecksum) throws DownloadException {
        if (expectedChecksum == null) {
            return; // No checksum provided, skip validation
        }

        try {
            String computedChecksum = computeSHA256(file);
            if (!computedChecksum.equalsIgnoreCase(expectedChecksum)) {
                throw new DownloadException("Checksum mismatch for " + file.getFileName() + ". Expected: " + expectedChecksum + ", but got: " + computedChecksum, null);
            }
            System.out.println("Checksum validated for " + file.getFileName());
        } catch (Exception e) {
            throw new DownloadException("Failed to validate checksum for " + file.getFileName(), e);
        }
    }

    private String computeSHA256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = Files.newInputStream(file);
             DigestInputStream dis = new DigestInputStream(fis, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {} // Read file fully
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
