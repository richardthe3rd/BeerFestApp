package ralcock.cbf.model;

public class BreweryBuilder {

    private String fName = "";
    private String fFestivalID = "";
    private String fDescription = "";

    public static BreweryBuilder aBrewery() {
        return new BreweryBuilder();
    }

    public Brewery build() {
        return new Brewery(fFestivalID, fName, fDescription);
    }

    public BreweryBuilder called(String name) {
        fName = name;
        return this;
    }

    public BreweryBuilder withDescription(String description) {
        fDescription = description;
        return this;
    }

    public BreweryBuilder withFestivalId(String festivalId) {
        fFestivalID = festivalId;
        return this;
    }
}
