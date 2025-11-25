package ralcock.cbf.view;

import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.content.Context;
import ralcock.cbf.R;

public class BeerListFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = BeerListFragmentPagerAdapter.class.getName();
    private final String tabTitles[];

    public BeerListFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        tabTitles = new String[] {
            context.getString(R.string.tab_beer),
            context.getString(R.string.tab_low_no),
            context.getString(R.string.tab_bookmarks)
        };
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "Returning new BeerListFragment for position " + position);
        switch (position) {
            case 0:
                return new AllBeersListFragment();
            case 1:
                return new LowNoAlcoholListFragment();
            case 2:
                return new BookmarkedBeerListFragment();
            default:
                return new AllBeersListFragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
