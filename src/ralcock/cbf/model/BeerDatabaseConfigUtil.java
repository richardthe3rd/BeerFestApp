package ralcock.cbf.model;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

public class BeerDatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws IOException, SQLException {
        String configFile = args[0];
        writeConfigFile(configFile);
    }
}
