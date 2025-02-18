package interfaces;

import exceptions.DownloadException;
import models.DownloadOptions;
import service.DownloaderImpl;

import java.nio.file.Path;
import java.util.List;

public interface Downloader {
    List<Path> download(List<String> urls) throws DownloadException;
}
