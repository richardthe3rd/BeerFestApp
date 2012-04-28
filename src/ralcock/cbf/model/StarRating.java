package ralcock.cbf.model;

import java.io.Serializable;

public final class StarRating implements Serializable, Comparable<StarRating> {

    public static final StarRating NO_STARS = new StarRating(0);

    private final int fRating;

    public StarRating(final int rating) {
        fRating = rating;
    }

    public StarRating(final float rating) {
        fRating = (int) rating;
    }

    public int getNumberOfStars() {
        return fRating;
    }

    public int compareTo(final StarRating starRating) {
        if (fRating > starRating.fRating) return -1;
        if (fRating == starRating.fRating) return 0;
        return 1;
    }
}
