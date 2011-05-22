package ralcock.cbf.model;

import java.io.Serializable;

public class Beer implements Serializable {
    private final Brewery fBrewery;
    private final String fName;
    private final float fAbv;
    private final String fNotes;

    public Beer(Brewery brewery, String name, float abv, String notes) {
        fBrewery = brewery;
        fName = name;
        fAbv = abv;
        fNotes = notes;
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
            return beer1.fName.compareTo(beer2.fName);
        }
        public String getDescription() {
            return "sorted by beer";
        }
    }

    public static class BreweryComparator implements Beer.Comparator {
        public int compare(Beer beer1, Beer beer2) {
            return beer1.getBrewery().getName().compareTo(beer2.getBrewery().getName());
        }
        public String getDescription() {
            return "sorted by brewery";
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
