package qeorm;

/**
 * Created by ashen on 2017-2-4.
 */
public class DataSourceNotExistException extends RuntimeException {
    public DataSourceNotExistException() {
        super();
    }

    public DataSourceNotExistException(String message) {
        super(message);
    }

    public DataSourceNotExistException(String message, Throwable cause) {
        super(message, cause);
    }


    public DataSourceNotExistException(Throwable cause) {
        super(cause);
    }


    protected DataSourceNotExistException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
