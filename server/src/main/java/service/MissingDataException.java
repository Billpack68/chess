package service;

public class MissingDataException extends Exception {
    public MissingDataException(String message) {
        super(message);
    }
    public MissingDataException(String message, Throwable ex) {
        super(message, ex);
    }
}
