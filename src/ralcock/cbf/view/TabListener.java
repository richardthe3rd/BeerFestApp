package ralcock.cbf.view;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import ralcock.cbf.R;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private final Activity fActivity;
    private final String fTag;
    private final Class<T> fClazz;
    private Fragment fFragment;

    public TabListener(Activity activity, String tag, Class<T> clazz) {
        fActivity = activity;
        fTag = tag;
        fClazz = clazz;
    }

    public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction ft) {
        if (fFragment == null) {
            fFragment = Fragment.instantiate(fActivity, fClazz.getName());

            ft.replace(R.id.tabContainer, fFragment, fTag);
        } else {
            ft.attach(fFragment);
        }
    }

    public void onTabUnselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
        if (fFragment != null) {
            ft.detach(fFragment);
        }
    }

    public void onTabReselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
    }
}
