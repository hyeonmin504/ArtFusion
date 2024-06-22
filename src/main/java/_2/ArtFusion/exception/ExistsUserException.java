package _2.ArtFusion.exception;

public class ExistsUserException extends RuntimeException{
    public ExistsUserException() {
    }

    public ExistsUserException(String message) {
        super(message);
    }

    public ExistsUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistsUserException(Throwable cause) {
        super(cause);
    }

    public ExistsUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
