package ralcock.cbf.view;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import ralcock.cbf.R;

final class BeerItemView {
    TextView brewery;
    TextView beer;
    RatingBar rating;

    BeerItemView(View view) {
        brewery = findTextViewById(view, R.id.brewery);
        beer = findTextViewById(view, R.id.beer);
        rating = (RatingBar)view.findViewById(R.id.rating);
    }

    private TextView findTextViewById(View view, int id) {
        return (TextView)view.findViewById(id);
    }
}
