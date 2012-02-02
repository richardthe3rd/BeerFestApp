package ralcock.cbf.model;

import java.io.Serializable;

public final class StarRating implements Serializable, Comparable<StarRating> {

    private final int fRating;

    public StarRating(final int rating) {
        fRating = rating;
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
