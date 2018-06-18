package qeorm;

public class SqlErrorException extends RuntimeException {
    public SqlErrorException(String message) {
        super(message);
    }
}
