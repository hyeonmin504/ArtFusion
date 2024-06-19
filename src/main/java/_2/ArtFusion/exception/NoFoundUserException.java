package _2.ArtFusion.exception;

public class NoFoundUserException extends RuntimeException{
    public NoFoundUserException() {
    }

    public NoFoundUserException(String message) {
        super(message);
    }

    public NoFoundUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFoundUserException(Throwable cause) {
        super(cause);
    }

    public NoFoundUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
