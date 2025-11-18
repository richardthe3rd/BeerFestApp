package ralcock.cbf.model.dao;

public class BeerAccessException extends RuntimeException {
    public BeerAccessException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
