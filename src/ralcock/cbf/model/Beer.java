package ralcock.cbf.model;

import java.io.Serializable;

public final class Beer implements Serializable {
    private final Brewery fBrewery;
    private final String fName;
    private final float fAbv;
    private final String fNotes;

    public Beer(final Brewery brewery, final String name, final float abv, final String notes) {
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

    @Override
    public String toString() {
        return "Beer{" +
                "fBrewery=" + fBrewery +
                ", fName='" + fName + '\'' +
                ", fAbv=" + fAbv +
                ", fNotes='" + fNotes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beer beer = (Beer) o;

        if (Float.compare(beer.fAbv, fAbv) != 0) return false;
        if (!fBrewery.equals(beer.fBrewery)) return false;
        if (!fName.equals(beer.fName)) return false;
        if (!fNotes.equals(beer.fNotes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fBrewery.hashCode();
        result = 31 * result + fName.hashCode();
        result = 31 * result + (fAbv != +0.0f ? Float.floatToIntBits(fAbv) : 0);
        result = 31 * result + fNotes.hashCode();
        return result;
    }
}
