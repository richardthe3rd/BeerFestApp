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
}
