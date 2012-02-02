package ralcock.cbf.model;

public final class BeerWithRating {
    private final Beer fBeer;
    private final StarRating fRating;

    public BeerWithRating(final Beer beer, final StarRating rating) {
        fBeer = beer;
        fRating = rating;
    }

    public Beer getBeer() {
        return fBeer;
    }

    public StarRating getRating() {
        return fRating;
    }

    public BeerWithRating rate(final StarRating starRating) {
        return new BeerWithRating(fBeer, starRating);
    }

}
