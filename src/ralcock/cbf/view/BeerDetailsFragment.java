package ralcock.cbf.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import ralcock.cbf.R;
import ralcock.cbf.actions.BeerSearcher;
import ralcock.cbf.model.Beer;
import ralcock.cbf.model.BeerDatabaseHelper;
import ralcock.cbf.model.StarRating;

public class BeerDetailsFragment extends Fragment {
    private BeerDetailsView fBeerDetailsView;
    private BeerDatabaseHelper fDBHelper;

    private BeerDatabaseHelper getHelper() {
        if (fDBHelper == null) {
            fDBHelper = OpenHelperManager.getHelper(getActivity(),
                    BeerDatabaseHelper.class);
        }
        return fDBHelper;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beer_details_fragment, container, false);
        fBeerDetailsView = new BeerDetailsView(view);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Beer beer = ((BeerDetailsActivity) getActivity()).getBeer();
        displayBeer(beer);
    }

    public void displayBeer(final Beer beer) {
        fBeerDetailsView.BeerNameAndAbv.setText(String.format("%s (%.1f%%)", beer.getName(), beer.getAbv()));
        fBeerDetailsView.BeerDescription.setText(beer.getDescription());
        fBeerDetailsView.BeerStyle.setText(beer.getStyle());

        fBeerDetailsView.BeerStatus.setText(beer.getStatus());

        fBeerDetailsView.BreweryName.setText(beer.getBrewery().getName());
        fBeerDetailsView.BreweryDescription.setText(beer.getBrewery().getDescription());

        fBeerDetailsView.BeerRatingBar.setRating(beer.getRating());

        fBeerDetailsView.BeerRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser)
                    rateBeer(beer, new StarRating(rating));
            }
        });

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(final View view) {
                BeerSearcher beerSearcher = new BeerSearcher(getActivity());
                beerSearcher.searchBeer(beer);
            }
        };
        Spannable span = (Spannable) fBeerDetailsView.SearchOnline.getText();
        span.setSpan(clickableSpan,
                0, span.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void rateBeer(Beer beer, StarRating rating) {
        beer.setNumberOfStars(rating);
        getHelper().getBeers().updateBeer(beer);
        displayBeer(beer);
    }

    private static final class BeerDetailsView {

        final TextView BeerNameAndAbv;
        final TextView BeerStyle;
        final TextView BeerDescription;
        final RatingBar BeerRatingBar;
        final TextView BreweryName;
        final TextView BreweryDescription;
        final TextView BeerStatus;
        final TextView SearchOnline;

        private BeerDetailsView(final View view) {
            BeerNameAndAbv = (TextView) view.findViewById(R.id.detailsViewBeerNameAndAbv);
            BeerStyle = (TextView) view.findViewById(R.id.detailsViewBeerStyle);
            BeerDescription = (TextView) view.findViewById(R.id.detailsViewBeerDescription);
            BreweryName = (TextView) view.findViewById(R.id.detailsViewBreweryName);
            BreweryDescription = (TextView) view.findViewById(R.id.detailsViewBreweryDescription);
            BeerRatingBar = (RatingBar) view.findViewById(R.id.detailsViewBeerRatingBar);
            BeerStatus = (TextView) view.findViewById(R.id.detailsViewBeerStatus);
            SearchOnline = (TextView) view.findViewById(R.id.clickToSearchOnline);
            SearchOnline.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
