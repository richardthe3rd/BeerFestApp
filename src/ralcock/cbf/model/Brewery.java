package ralcock.cbf.model;

import java.io.Serializable;

public class Brewery implements Serializable {

    public Brewery(String name, String description) {
        this.fName = name;
        this.fDescription = description;
    }

    public String getName() {
        return fName;
    }

    public String getDescription() {
        return fDescription;
    }

    public String toString() {
        return fName;
    }

    private String fName;
    private String fDescription;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Brewery brewery = (Brewery) o;

        if (!fDescription.equals(brewery.fDescription)) return false;
        if (!fName.equals(brewery.fName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fName.hashCode();
        result = 31 * result + fDescription.hashCode();
        return result;
    }
}
