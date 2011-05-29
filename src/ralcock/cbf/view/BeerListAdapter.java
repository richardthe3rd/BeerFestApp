package ralcock.cbf.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;

import java.util.List;

public class BeerListAdapter extends ArrayAdapter<Beer>{

    public BeerListAdapter(Context context, List<Beer> beerList) {
        super(context, R.layout.beer_item, beerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        BeerView beerView;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.beer_item, parent, false);
            beerView = new BeerView(view);
            view.setTag(beerView);
        } else {
            view = convertView;
            beerView = (BeerView)view.getTag();
        }

        Beer beer = getItem(position);

        String breweryText = beer.getBrewery().getName();
        beerView.brewery.setText(breweryText);

        beerView.rating.setRating(beer.getRating().getNumberOfStars());

        String beerText = beer.getName() + " (" +  beer.getAbv() + "%)";
        beerView.beer.setText(beerText);

        return view;
    }

    private static class BeerView {
        TextView brewery;
        TextView beer;
        RatingBar rating;
        private BeerView(View view) {
            brewery = findTextViewById(view, R.id.brewery);
            beer = findTextViewById(view, R.id.beer);
            rating = (RatingBar)view.findViewById(R.id.rating);
        }
        private TextView findTextViewById(View view, int id) {
            return (TextView)view.findViewById(id);
        }
    }

}