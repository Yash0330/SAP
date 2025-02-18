# Concurrent File Downloader (Java)

This project is a **concurrent file downloader** in Java that:
- Supports **parallel downloads** of multiple files
- **Splits large files** into parts for faster downloading
- **Validates file integrity** using **SHA-256 checksum**
- Uses **Java 17+ standard HTTP client** (no third-party libraries)

---

## ** Prerequisites**
- Java 17+ installed
- Maven or Gradle (for running tests)
- An HTTP server to serve files (e.g., `nginx`, `SimpleHTTPServer`)

---

## ** Generating SHA-256 Hashes Before Downloading**
Before downloading files, you need to compute their SHA-256 hash to verify integrity.

### ** Generate Hash for a File**
Run the `FileHasher` utility:
```sh
javac -d . src/io/dwnd/FileHasher.java
java io.dwnd.FileHasher /path/to/file.bin
``` 

### Add Hash to the Downloader
In Main.java, update:

```sh
expectedChecksums.put("http://localhost/file.bin", "a3b15c8e0f1b54e3e1b4970dca2ef3d0bd6c8f6b7a31a1d49dc
```
