package ralcock.cbf.model;

public class BeerBuilder {
    private String fFestivalId = "";
    private String fName = "";
    private float fAbv = 0.0f;
    private String fDescription = "";
    private String fStyle = "";
    private String fStatus = "";
    private String fDispense = "";
    private String fAllergens = "";
    private String fCategory = "beer";
    private Brewery fBrewery = null;

    public static BeerBuilder aBeer() {
        return new BeerBuilder();
    }

    public BeerBuilder withFestivalId(String festivalId) {
        fFestivalId = festivalId;
        return this;
    }

    public BeerBuilder called(String name) {
        fName = name;
        return this;
    }

    public BeerBuilder withABV(float abv) {
        fAbv = abv;
        return this;
    }

    public BeerBuilder withDescription(String description) {
        fDescription = description;
        return this;
    }

    public BeerBuilder withStyle(String style) {
        fStyle = style;
        return this;
    }

    public BeerBuilder withStatus(String status) {
        fStatus = status;
        return this;
    }

    public BeerBuilder withDispenseMethod(String dispense) {
        fDispense = dispense;
        return this;
    }

    public BeerBuilder withAllergens(String allergens) {
        fAllergens = allergens;
        return this;
    }

    public BeerBuilder withCategory(String category) {
        fCategory = category;
        return this;
    }

    public BeerBuilder fromBrewery(Brewery brewery) {
        fBrewery = brewery;
        return this;
    }

    public Beer build() {
        return new Beer(fFestivalId, fName, fAbv, fDescription, fStyle, fStatus, fDispense, fAllergens, fCategory, fBrewery);
    }

    public BeerBuilder from(final BreweryBuilder breweryBuilder) {
        fromBrewery(breweryBuilder.build());
        return this;
    }
}
