package ralcock.cbf.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static void safeClose(final String tag, final InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException iox) {
                Log.e(tag, "Exception closing input stream.", iox);
            }
        }
    }

}
