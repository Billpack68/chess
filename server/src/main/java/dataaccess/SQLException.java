package dataaccess;

public class SQLException extends Exception {
    public SQLException(String message) { super(message); }
    public SQLException(String message, Throwable ex) { super(message, ex); }
}
