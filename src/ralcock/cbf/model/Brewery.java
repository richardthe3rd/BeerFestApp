package ralcock.cbf.model;

import java.io.Serializable;

public final class Brewery implements Serializable {

    private final String fName;

    private final String fDescription;

    public Brewery(final String name, final String description) {
        fName = name;
        fDescription = description;
    }

    public String getName() {
        return fName;
    }

    public String getDescription() {
        return fDescription;
    }

    @Override
    public String toString() {
        return "Brewery{" +
                "fName='" + fName + '\'' +
                ", fDescription='" + fDescription + '\'' +
                '}';
    }


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
