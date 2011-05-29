package ralcock.cbf.model;

import android.content.Context;

import java.io.Serializable;

public class Beer implements Serializable {
    private final Brewery fBrewery;
    private final String fName;
    private final float fAbv;
    private final String fNotes;
    private StarRating fRating = null;

    private transient RatingDatabase fRatingDatabase;

    public void setContext(Context context) {
        fRatingDatabase = new RatingDatabase(context);
    }

    public Beer(Context context, Brewery brewery, String name, float abv, String notes) {
        fBrewery = brewery;
        fName = name;
        fAbv = abv;
        fNotes = notes;
        fRatingDatabase = new RatingDatabase(context);
        fRating = fRatingDatabase.getRatingForBeer(this);
    }

    public String getName() {
        return fName;
    }

    public String getNotes() {
        return fNotes;
    }

    public Brewery getBrewery() {
        return fBrewery;
    }

    public float getAbv() {
        return fAbv;
    }

    public StarRating getRating() {
        return fRating;
    }

    public void updateRating() {
        fRating = fRatingDatabase.getRatingForBeer(this);
    }

    public void setRating(StarRating starRating) {
        fRating = starRating;
        fRatingDatabase.setRatingForBeer(this, starRating);
    }

    // Used by the test filtering
    public String toString() {
        return fBrewery.getName() + " " + fName;
    }

    public static class AbvComparator implements Beer.Comparator {
        public int compare(Beer beer1, Beer beer2) {
            return Float.compare(beer1.fAbv, beer2.fAbv);
        }

        public String getDescription() {
            return "sorted by ABV";
        }
    }

    public static class BeerComparator implements Beer.Comparator {
        public int compare(Beer beer1, Beer beer2) {
            int result = beer1.getName().compareTo(beer2.getName());
            if (result == 0) {
                return beer1.getBrewery().getName().compareTo(beer2.getBrewery().getName());
            } else {
                return result;
            }
        }
        public String getDescription() {
            return "sorted by beer";
        }
    }

    public static class BreweryComparator implements Beer.Comparator {
        public int compare(Beer beer1, Beer beer2) {
            int result = beer1.getBrewery().getName().compareTo(beer2.getBrewery().getName());
            if (result == 0) {
                return beer1.getName().compareTo(beer2.getName());
            } else {
                return result;
            }

        }
        public String getDescription() {
            return "sorted by brewery";
        }
    }

    public static class RatingComparator implements Beer.Comparator {
        public String getDescription() {
            return "sorted by rating";
        }

        public int compare(Beer beer1, Beer beer2) {
            return beer1.getRating().compareTo(beer2.getRating());
        }
    }

    public static class ReverseComparator implements Beer.Comparator {
        private Comparator fWrappedComparator;

        public ReverseComparator(Comparator comparator) {
            fWrappedComparator = comparator;
        }

        public String getDescription() {
            return fWrappedComparator.getDescription();
        }

        public int compare(Beer beer1, Beer beer2) {
            return -1 * fWrappedComparator.compare(beer1, beer2);
        }
    }

    public static interface Comparator extends java.util.Comparator<Beer>, Serializable {
        String getDescription();
    }
}
