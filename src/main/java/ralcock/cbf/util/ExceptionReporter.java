package ralcock.cbf.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ExceptionReporter {
    private final Context fContext;

    public ExceptionReporter(final Context context) {
        fContext = context;
    }

    public void report(final String tag, final String message, final Throwable throwable) {
        Log.e(tag, message, throwable);
        Toast.makeText(fContext, message, Toast.LENGTH_LONG).show();
    }
}
