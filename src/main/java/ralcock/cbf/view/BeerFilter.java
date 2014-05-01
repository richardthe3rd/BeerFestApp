package ralcock.cbf.view;

import android.widget.Filter;
import ralcock.cbf.model.BeerList;

final class BeerFilter extends Filter {
    private static final String TAG = BeerFilter.class.getName();

    private final BeerListAdapter fBeerListAdapter;
    private final BeerList fBeerList;

    public BeerFilter(final BeerListAdapter adapter,
                      final BeerList beerList) {
        fBeerListAdapter = adapter;
        fBeerList = beerList;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence charSequence) {
        fBeerList.filterBy(charSequence);
        return new FilterResults();

    }

    @Override
    protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
        fBeerListAdapter.notifyDataSetChanged();
    }
}
