package exceptions;

public class GeneralException extends Exception {

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, Throwable e) {
        super(message, e);
    }
}
