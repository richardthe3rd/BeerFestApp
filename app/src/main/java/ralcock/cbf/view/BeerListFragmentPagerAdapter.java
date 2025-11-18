package ralcock.cbf.view;

import android.content.Context;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class BeerListFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = BeerListFragmentPagerAdapter.class.getName();
    private final String tabTitles[] = new String[] {"All Beers", "Bookmarks"};

    // private Context context;

    public BeerListFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        // this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "Returning new BeerListFragment for position " + position);
        if (position == 0) {
            return new AllBeersListFragment();
        } else {
            return new BookmarkedBeerListFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
