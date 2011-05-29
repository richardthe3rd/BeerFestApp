package ralcock.cbf.model;

import java.io.Serializable;

public class StarRating implements Serializable, Comparable<StarRating> {

    private int fRating;

    public StarRating(int rating) {
        fRating = rating;
    }

    public int getNumberOfStars() {
        return fRating;
    }

    public int compareTo(StarRating starRating) {
        if (fRating > starRating.fRating) return -1;
        if (fRating == starRating.fRating) return 0;

        return 1;
    }
}
