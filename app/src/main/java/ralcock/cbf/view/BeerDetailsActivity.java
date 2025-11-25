package ralcock.cbf.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSharer;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerAccessor;

//OrmLiteBaseActivity<BeerDatabaseHelper>
public final class BeerDetailsActivity extends AppCompatActivity {

    private static final String TAG = BeerDetailsActivity.class.getName();
    public static final String EXTRA_BEER_ID = "BEER";

    private final BeerSharer fBeerSharer;
    private final BeerAccessor fBeerAccessor;
    private long fBeerId;

    public BeerDetailsActivity() {
        fBeerSharer = new BeerSharer(this);
        fBeerAccessor = new BeerAccessor(this);
    }

    Beer getBeer() {
        return fBeerAccessor.getBeer(fBeerId);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        // Enable edge-to-edge display for Android 15+ compatibility (fixes issues #60, #61)
        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);

        fBeerId = getIntent().getExtras().getLong(EXTRA_BEER_ID);

        setContentView(R.layout.beer_details_activity);

        // Handle window insets for edge-to-edge display
        final View rootView = findViewById(R.id.details_root);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(final View v, final WindowInsetsCompat windowInsets) {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                // Don't consume - allow child views (fragment) to also handle insets
                return windowInsets;
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(getBeer().getName());
        setSupportActionBar(myToolbar);

        Log.i(TAG, "In BeerDetailsActivity.onCreate with ID " + fBeerId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        } else if (itemId == R.id.shareBeer) {
            fBeerSharer.shareBeer(getBeer());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void shareBeer(View button) {
        fBeerSharer.shareBeer(getBeer());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
