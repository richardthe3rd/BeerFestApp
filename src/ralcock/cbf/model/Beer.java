package ralcock.cbf.model;

import java.io.Serializable;

public final class Beer implements Serializable {
    private final Brewery fBrewery;
    private final String fName;
    private final float fAbv;
    private final String fNotes;
    private final String fStatus;

    public Beer(final Brewery brewery, 
                final String name, final float abv, final String notes, final String status) {
        fBrewery = brewery;
        fName = name;
        fAbv = abv;
        fNotes = notes;
        fStatus = status;
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

    public String getStatus() {
        return fStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Beer beer = (Beer) o;

        if (Float.compare(beer.fAbv, fAbv) != 0) return false;
        if (fBrewery != null ? !fBrewery.equals(beer.fBrewery) : beer.fBrewery != null) return false;
        if (fName != null ? !fName.equals(beer.fName) : beer.fName != null) return false;
        if (fNotes != null ? !fNotes.equals(beer.fNotes) : beer.fNotes != null) return false;
        if (fStatus != null ? !fStatus.equals(beer.fStatus) : beer.fStatus != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fBrewery != null ? fBrewery.hashCode() : 0;
        result = 31 * result + (fName != null ? fName.hashCode() : 0);
        result = 31 * result + (fAbv != +0.0f ? Float.floatToIntBits(fAbv) : 0);
        result = 31 * result + (fNotes != null ? fNotes.hashCode() : 0);
        result = 31 * result + (fStatus != null ? fStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Beer{" +
                "fBrewery=" + fBrewery +
                ", fName='" + fName + '\'' +
                ", fAbv=" + fAbv +
                ", fNotes='" + fNotes + '\'' +
                ", fStatus='" + fStatus + '\'' +
                '}';
    }

}
