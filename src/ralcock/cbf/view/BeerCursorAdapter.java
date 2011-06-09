package ralcock.cbf.view;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import ralcock.cbf.R;
import ralcock.cbf.model.BeerDatabase;


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

        String beerName = cursor.getString(cursor.getColumnIndexOrThrow(BeerDatabase.BEER_NAME_COLUMN));
        float beerAbv = cursor.getFloat(cursor.getColumnIndexOrThrow(BeerDatabase.BEER_ABV_COLUMN));
        String breweryName = cursor.getString(cursor.getColumnIndexOrThrow(BeerDatabase.BREWERY_NAME_COLUMN));
        int beerRating = cursor.getInt(cursor.getColumnIndexOrThrow(BeerDatabase.BEER_RATING_COLUMN));

        beerItemView.brewery.setText(breweryName);
        beerItemView.rating.setRating(beerRating);

        String beerText = beerName + " (" +  beerAbv + "%)";
        beerItemView.beer.setText(beerText);
    }



    @Override
    public CharSequence convertToString(Cursor cursor) {
        String beerName = cursor.getString(cursor.getColumnIndexOrThrow(BeerDatabase.BEER_NAME_COLUMN));
        String breweryName = cursor.getString(cursor.getColumnIndexOrThrow(BeerDatabase.BREWERY_NAME_COLUMN));
        return breweryName + " " + beerName;
    }
}
