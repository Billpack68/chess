package service;

public class ColorAlreadyTakenException extends RuntimeException {
    public ColorAlreadyTakenException(String message) {
        super(message);
    }
}
