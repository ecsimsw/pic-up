package ecsimsw.picup.exception;

public class FileNotExistsException extends IllegalArgumentException {

    public FileNotExistsException(String msg) {
        super(msg);
    }
}
