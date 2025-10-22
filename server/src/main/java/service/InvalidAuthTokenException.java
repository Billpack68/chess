package service;

public class InvalidAuthTokenException extends Exception {
    public InvalidAuthTokenException(String message) {
        super(message);
    }
}
