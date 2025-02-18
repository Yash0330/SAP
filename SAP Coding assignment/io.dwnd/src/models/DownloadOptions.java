package models;

import java.nio.file.Path;

public record DownloadOptions(Path downloadDir, int numParallelParts, int numParallelFiles) {

}
