import exceptions.DownloadException;
import interfaces.Downloader;
import models.DownloadOptions;
import service.DownloaderImpl;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            DownloadOptions options = new DownloadOptions(Path.of("downloads"), 4, 2);
            Map<String, String> checksums = new HashMap<>();
            checksums.put("http://localhost:8080/bigfile.txt", "a3b15c8e0f1b54e3e1b4970dca2ef3d0bd6c8f6b7a31a1d49dcb2dfabe0846ac");
            checksums.put("http://localhost:8080/bigfile1.txt", "d2d2f1f77a19c58f8a0efb1f8a52b55fdfed6f1b8c9e2457d2f02e83287b3c0a");
            Downloader downloader = new DownloaderImpl(options, checksums);
            List<Path> downloadFiles = downloader.download(List.of(
                    "http://localhost:8080/bigfile.txt",
                    "http://localhost:8080/bigfile1.txt"
            ));
            System.out.println("Downloaded files: " + downloadFiles);
        } catch (DownloadException e) {
            e.printStackTrace();
        }
    }
}