import java.io.*;
import java.nio.file.*;
import java.security.*;

public class FileHasher {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java FileHasher <file-path>");
            return;
        }

        Path filePath = Path.of(args[0]);
        try {
            String hash = computeSHA256(filePath);
            System.out.println("SHA-256 hash for " + filePath.getFileName() + ": " + hash);
        } catch (Exception e) {
            System.err.println("Failed to compute hash: " + e.getMessage());
        }
    }

    public static String computeSHA256(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = Files.newInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(fis, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {} // Read file completely
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
