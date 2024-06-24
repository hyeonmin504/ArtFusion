package _2.ArtFusion.exception;

public class NotFoundImageException extends RuntimeException{
    public NotFoundImageException() {
    }

    public NotFoundImageException(String message) {
        super(message);
    }

    public NotFoundImageException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundImageException(Throwable cause) {
        super(cause);
    }

    public NotFoundImageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
