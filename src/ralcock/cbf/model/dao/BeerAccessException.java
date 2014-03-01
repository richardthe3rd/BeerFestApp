package ralcock.cbf.model.dao;

import java.sql.SQLException;

public class BeerAccessException extends RuntimeException {
    public BeerAccessException(final String msg, final SQLException cause) {
        super(msg, cause);
    }
}
