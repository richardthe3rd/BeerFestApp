package ralcock.cbf.view;

import android.widget.Filter;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.util.ExceptionReporter;

import java.sql.SQLException;

class BeerFilter extends Filter {
    private static final String TAG = BeerFilter.class.getName();

    private final BeerListAdapter fBeerListAdapter;
    private final BeerList fBeerList;
    private final ExceptionReporter fExceptionReporter;

    public BeerFilter(final BeerListAdapter adapter,
                      final BeerList beerList,
                      final ExceptionReporter exceptionReporter) {
        fBeerListAdapter = adapter;
        fBeerList = beerList;
        fExceptionReporter = exceptionReporter;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence charSequence) {
        try {
            fBeerList.filterBy(charSequence);
        } catch (SQLException e) {
            fExceptionReporter.report(TAG, e.getMessage(), e);
        }
        return new FilterResults();
    }

    @Override
    protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
        fBeerListAdapter.notifyDataSetChanged();
    }
}
