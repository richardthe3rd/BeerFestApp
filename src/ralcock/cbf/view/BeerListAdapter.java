package ralcock.cbf.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerList;
import ralcock.cbf.model.Brewery;

public class BeerListAdapter extends BaseAdapter implements Filterable {

    private final Context fContext;
    private final BeerList fBeerList;
    private final BeerFilter fFilter;

    public BeerListAdapter(final Context context, final BeerList beerList) {
        fContext = context;
        fBeerList = beerList;
        fFilter = new BeerFilter(this, fBeerList);
    }

    private View newView(ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(fContext);
        View view = layoutInflater.inflate(R.layout.beer_item, viewGroup, false);
        BeerItemView beerItemView = new BeerItemView(view);
        view.setTag(beerItemView);
        return view;
    }

    private void bindView(View view, final Beer beer) {
        BeerItemView beerItemView = (BeerItemView) view.getTag();

        Brewery brewery = beer.getBrewery();
        beerItemView.brewery.setText(brewery == null ? "<NULL_BREWERY>" : brewery.getName());

        beerItemView.rating.setRating(beer.getRating());

        String beerText = beer.getName() + " (" + beer.getAbv() + "%)";
        beerItemView.beer.setText(beerText);

        beerItemView.status.setText(beer.getStatus());
        beerItemView.style.setText(beer.getStyle());
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

}
