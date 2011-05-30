package ralcock.cbf.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;

import java.util.List;

public class BeerArrayAdapter extends ArrayAdapter<Beer>{

    public BeerArrayAdapter(Context context, List<Beer> beerList) {
        super(context, R.layout.beer_item, beerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        BeerItemView beerItemView;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.beer_item, parent, false);
            beerItemView = new BeerItemView(view);
            view.setTag(beerItemView);
        } else {
            view = convertView;
            beerItemView = (BeerItemView)view.getTag();
        }

        Beer beer = getItem(position);

        String breweryText = beer.getBrewery().getName();
        beerItemView.brewery.setText(breweryText);

        beerItemView.rating.setRating(beer.getRating().getNumberOfStars());

        String beerText = beer.getName() + " (" +  beer.getAbv() + "%)";
        beerItemView.beer.setText(beerText);

        return view;
    }

}