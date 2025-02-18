package exceptions;

public class DownloadException extends Exception {
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
