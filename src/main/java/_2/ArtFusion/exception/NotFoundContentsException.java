package _2.ArtFusion.exception;

public class NotFoundContentsException extends RuntimeException{
    public NotFoundContentsException() {
    }

    public NotFoundContentsException(String message) {
        super(message);
    }

    public NotFoundContentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundContentsException(Throwable cause) {
        super(cause);
    }

    public NotFoundContentsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
