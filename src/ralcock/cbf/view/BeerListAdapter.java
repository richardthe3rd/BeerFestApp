package ralcock.cbf.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ralcock.cbf.R;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.Rating;
import ralcock.cbf.model.RatingDatabase;

import java.util.List;

public class BeerListAdapter extends ArrayAdapter<Beer>{

    public BeerListAdapter(Context context, List<Beer> beerList) {
        super(context, R.layout.beer_item, beerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.beer_item, parent, false);

        Beer beer = getItem(position);

        String breweryText = beer.getBrewery().getName();
        setTextViewTextById(view, R.id.brewery, breweryText);

        RatingDatabase ratingDb = new RatingDatabase(getContext());
        Rating rating = ratingDb.getRatingForBeer(beer);

        // Don't show UNRATED
        if (rating != Rating.UNRATED) {
            setTextViewTextById(view, R.id.rating, getContext().getText(rating.getId()));
        }

        String beerText = beer.getName() + " (" +  beer.getAbv() + "%)";
        setTextViewTextById(view, R.id.beer, beerText);

        return view;
    }

    private void setTextViewTextById(View view, int id, CharSequence text) {
        TextView textView = findTextViewById(view, id);
        textView.setText(text);
    }

    private TextView findTextViewById(View view, int id) {
        return (TextView)view.findViewById(id);
    }

    private ImageView findImageViewById(View view, int id) {
        return (ImageView)view.findViewById(id);
    }

}