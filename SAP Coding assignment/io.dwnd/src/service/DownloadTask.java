package service;

import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class DownloadTask implements Runnable {
    private final String fileUrl;
    private final Path filePath;
    private final long start, end;

    public DownloadTask(String fileUrl, Path filePath, long start, long end) {
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .header("Range", "bytes=" + start + "-" + end)
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            RandomAccessFile ref = new RandomAccessFile(filePath.toFile(), "rw");
            ref.seek(start);
            ref.write(response.body());
            ref.close();
        } catch (Exception e) {
            System.err.println("Failed to download chunk: " + start + "-" + end);
            e.printStackTrace();
        }
    }
}
