package ralcock.cbf.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.Brewery;
import ralcock.cbf.util.ExceptionReporter;

public final class BeerListAdapter extends BaseAdapter implements Filterable {

    private final Context fContext;
    private final BeerList fBeerList;
    private final BeerFilter fFilter;

    public BeerListAdapter(final Context context, final BeerList beerList) {
        fContext = context;
        fBeerList = beerList;
        fFilter = new BeerFilter(this, fBeerList, new ExceptionReporter(context));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private View newView(ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(fContext);
        View view = layoutInflater.inflate(R.layout.beer_listitem, viewGroup, false);
        BeerListItemView beerListItemView = new BeerListItemView(view);
        view.setTag(beerListItemView);
        return view;
    }

    private void bindView(View view, final Beer beer) {
        BeerListItemView beerListItemView = (BeerListItemView) view.getTag();

        Brewery brewery = beer.getBrewery();
        beerListItemView.BreweryName.setText(brewery == null ? "<NULL_BREWERY>" : brewery.getName());

        beerListItemView.BeerRatingBar.setRating(beer.getRating());

        String beerText = beer.getName() + " (" + beer.getAbv() + "%)";
        beerListItemView.BeerName.setText(beerText);

        beerListItemView.BeerStatus.setText(beer.getStatus());
        beerListItemView.BeerStyle.setText(beer.getStyle());

        if (beer.isIsOnWishList()) {
            beerListItemView.BookmarkStatus.setText("Bookmarked");
        } else {
            beerListItemView.BookmarkStatus.setText("");
        }
    }

    public int getCount() {
        return fBeerList.getCount();
    }

    public Object getItem(final int i) {
        return fBeerList.getBeerAt(i);
    }

    public long getItemId(final int i) {
        return fBeerList.getBeerAt(i).getId();
    }

    public View getView(final int i, View view, final ViewGroup viewGroup) {
        if (view == null) {
            view = newView(viewGroup);
        }
        bindView(view, fBeerList.getBeerAt(i));
        return view;
    }

    public Filter getFilter() {
        return fFilter;
    }

    private static final class BeerListItemView {
        TextView BreweryName;
        TextView BeerName;
        TextView BeerStyle;
        TextView BeerStatus;
        RatingBar BeerRatingBar;
        TextView BookmarkStatus;

        BeerListItemView(final View view) {
            BreweryName = findTextViewById(view, R.id.breweryName);
            BeerName = findTextViewById(view, R.id.beerName);
            BeerStatus = findTextViewById(view, R.id.beerStatus);
            BeerStyle = findTextViewById(view, R.id.beerStyle);
            BeerRatingBar = (RatingBar) view.findViewById(R.id.beerRatingBar);
            BookmarkStatus = findTextViewById(view, R.id.bookmarkStatus);
        }

        private TextView findTextViewById(final View view, final int id) {
            return (TextView) view.findViewById(id);
        }
    }
}
