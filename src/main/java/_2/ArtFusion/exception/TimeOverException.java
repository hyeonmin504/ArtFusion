package _2.ArtFusion.exception;

public class TimeOverException extends RuntimeException {
    public TimeOverException() {
    }

    public TimeOverException(String message) {
        super(message);
    }

    public TimeOverException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeOverException(Throwable cause) {
        super(cause);
    }

    public TimeOverException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
