package ralcock.cbf.view;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import ralcock.cbf.R;


public class BeerCursorAdapter extends CursorAdapter {
    public BeerCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.beer_item, viewGroup, false);
        BeerItemView beerItemView = new BeerItemView(view);
        view.setTag(beerItemView);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        BeerItemView beerItemView = (BeerItemView)view.getTag();

        // todo
        String beerName = cursor.getString(cursor.getColumnIndexOrThrow("beer_name"));
        //String beerNotes = cursor.getString(cursor.getColumnIndexOrThrow("beer_notes"));
        float beerAbv = cursor.getFloat(cursor.getColumnIndexOrThrow("beer_abv"));
        String breweryName = cursor.getString(cursor.getColumnIndexOrThrow("brewery_name"));
        //String breweryNotes = cursor.getString(cursor.getColumnIndexOrThrow("brewery_notes"));

        //Brewery brewery = new Brewery(breweryName, breweryNotes);
        //Beer beer = new Beer(context, brewery, beerName, beerAbv, beerNotes);

        //String breweryText = beer.getBrewery().getName();
        beerItemView.brewery.setText(breweryName);

        //beerItemView.rating.setRating(beer.getRating().getNumberOfStars());

        String beerText = beerName + " (" +  beerAbv + "%)";
        beerItemView.beer.setText(beerText);
    }
}
