package ralcock.cbf.model;

public class BeerWithRating {
    private Beer fBeer;
    private StarRating fRating;

    public BeerWithRating(Beer beer, StarRating rating) {
        fBeer = beer;
        fRating = rating;
    }

    public Beer getBeer() {
        return fBeer;
    }

    public StarRating getRating() {
        return fRating;
    }

    public BeerWithRating rate(StarRating starRating) {
        return new BeerWithRating(fBeer, starRating);
    }
}
