package ralcock.cbf.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ImageView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.Brewery;

public final class BeerListAdapter extends BaseAdapter implements Filterable {

    private final Context fContext;
    private final BeerList fBeerList;
    private final BeerFilter fFilter;
    private final BeerListFragment fBeerListFragment;

    public BeerListAdapter(final Context context,
                           final BeerList beerList,
                           final BeerListFragment fragment) {
        fContext = context;
        fBeerList = beerList;
        fFilter = new BeerFilter(this, fBeerList);
        fBeerListFragment = fragment;
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
        beerListItemView.BeerDispense.setText(beer.getDispenseMethod());

        if (beer.isIsOnWishList()) {
            beerListItemView.BookmarkImage.setImageResource(R.drawable.ic_bookmark_black_48dp);
            beerListItemView.BeerName.setTypeface(beerListItemView.BeerName.getTypeface(),
                                                  Typeface.BOLD_ITALIC);
        } else {
            beerListItemView.BookmarkImage.setImageResource(R.drawable.ic_bookmark_border_black_48dp);
            beerListItemView.BeerName.setTypeface(beerListItemView.BeerName.getTypeface(),
                                                  Typeface.BOLD);
        }

        beerListItemView.BookmarkImage.setClickable(true);
        beerListItemView.BookmarkImage.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    fBeerListFragment.toggleBookmark(beer);
                }
            });
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
        TextView BeerDispense;
        ImageView BookmarkImage;

        BeerListItemView(final View view) {
            BreweryName = findTextViewById(view, R.id.breweryName);
            BeerName = findTextViewById(view, R.id.beerName);
            BeerStatus = findTextViewById(view, R.id.beerStatus);
            BeerStyle = findTextViewById(view, R.id.beerStyle);
            BeerRatingBar = (RatingBar) view.findViewById(R.id.beerRatingBar);
            BeerDispense = findTextViewById(view, R.id.beerDispense);
            BookmarkImage = (ImageView)view.findViewById(R.id.bookmark_image);
        }

        private TextView findTextViewById(final View view, final int id) {
            return (TextView) view.findViewById(id);
        }
    }
}
