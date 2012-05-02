package ralcock.cbf.view;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;

final class BeerItemView {
    TextView brewery;
    TextView beer;
    TextView style;
    TextView status;
    RatingBar rating;

    BeerItemView(View view) {
        brewery = findTextViewById(view, R.id.brewery_name);
        beer = findTextViewById(view, R.id.beer_name);
        status = findTextViewById(view, R.id.beer_status);
        style = findTextViewById(view, R.id.beer_style);
        rating = (RatingBar) view.findViewById(R.id.beer_rating);
    }

    private TextView findTextViewById(View view, int id) {
        return (TextView) view.findViewById(id);
    }
}
