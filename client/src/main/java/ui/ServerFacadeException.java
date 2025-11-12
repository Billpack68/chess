package ui;

public class ServerFacadeException extends RuntimeException {
    private final int id;

    public ServerFacadeException(String message, int id) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}