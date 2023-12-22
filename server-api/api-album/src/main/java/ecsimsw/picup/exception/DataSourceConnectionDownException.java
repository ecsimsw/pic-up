package ecsimsw.picup.exception;

public class DataSourceConnectionDownException extends IllegalArgumentException {

    public DataSourceConnectionDownException(String message) {
        super(message);
    }
}
