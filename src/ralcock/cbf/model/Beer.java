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
}
