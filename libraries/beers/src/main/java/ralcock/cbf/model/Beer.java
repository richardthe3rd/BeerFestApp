package ralcock.cbf.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import ralcock.cbf.model.dao.BeersImpl;

import java.io.Serializable;

@DatabaseTable(tableName = Beer.TABLE_NAME, daoClass = BeersImpl.class)
public final class Beer implements Serializable {

    public static final String TABLE_NAME = "beers";

    public static final String NAME_FIELD = "name";
    public static final String BREWERY_FIELD = "brewery";
    public static final String ABV_FIELD = "abv";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String STATUS_FIELD = "status";
    public static final String FESTIVAL_ID_FIELD = "festival_id";
    public static final String STYLE_FIELD = "style";
    public static final String DISPENSE_FIELD = "dispense";
    public static final String CATEGORY_FIELD = "category";

    public static final String RATING_FIELD = "rating";
    public static final String ON_WISH_LIST_FIELD = "on_wish_list";
    public static final String USER_COMMENTS_FIELD = "user_comments";
    public static final String ALLERGENS_FIELD = "allergens";

    @DatabaseField(columnName = "_id", generatedId = true)
    private long fId;

    @DatabaseField(columnName = BREWERY_FIELD, foreign = true, foreignAutoRefresh = true)
    private Brewery fBrewery;

    @DatabaseField(columnName = NAME_FIELD, index = true)
    private String fName;

    @DatabaseField(columnName = ABV_FIELD)
    private float fAbv;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String fDescription;

    @DatabaseField(columnName = STATUS_FIELD, index = true)
    private String fStatus;

    @DatabaseField(columnName = RATING_FIELD)
    private int fRating;

    @DatabaseField(columnName = FESTIVAL_ID_FIELD, index = true, unique = true)
    private String fFestivalID;

    @DatabaseField(columnName = STYLE_FIELD, index = true)
    private String fStyle;

    @DatabaseField(columnName = DISPENSE_FIELD)
    private String fDispense;

    @DatabaseField(columnName = CATEGORY_FIELD, index = true)
    private String fCategory;

    @DatabaseField(columnName = ON_WISH_LIST_FIELD, index = true)
    private boolean fIsOnWishList;

    @DatabaseField(columnName = USER_COMMENTS_FIELD)
    private String fUserComments;

    @DatabaseField(columnName = ALLERGENS_FIELD)
    private String fAllergens;

    @SuppressWarnings("UnusedDeclaration")
        // needed by ormlite
    Beer() {
    }

    public Beer(final String festivalId,
                final String name,
                final float abv,
                final String description,
                final String style,
                final String status,
                final String dispense,
                final String allergens,
                final String category,
                final Brewery brewery) {
        fFestivalID = festivalId;
        fBrewery = brewery;
        fName = name;
        fAbv = abv;
        fDescription = description;
        fStyle = style;
        fStatus = status;
        fDispense = dispense;
        fAllergens = allergens;
        fCategory = category;
    }

    public String getFestivalID() {
        return fFestivalID;
    }

    public String getName() {
        return fName;
    }

    public String getDescription() {
        return fDescription;
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

    public int getRating() {
        return fRating;
    }

    public StarRating getNumberOfStars() {
        return new StarRating(fRating);
    }

    public void setNumberOfStars(final StarRating rating) {
        fRating = rating.getNumberOfStars();
    }

    public long getId() {
        return fId;
    }

    public void setId(final long id) {
        fId = id;
    }

    public String getStyle() {
        return fStyle;
    }

    public boolean isIsOnWishList() {
        return fIsOnWishList;
    }

    public void setIsOnWishList(final boolean isOnWishList) {
        fIsOnWishList = isOnWishList;
    }

    public void setName(final String name) {
        fName = name;
    }

    public String getUserComments() {
        return fUserComments;
    }

    public void setUserComments(final String userComments) {
        fUserComments = userComments;
    }

    public String getDispenseMethod() {
        return fDispense;
    }

    public void setDispenseMethod(String dispense) {
        fDispense = dispense;
    }

    public String getAllergens() {
        return fAllergens;
    }

    public void setAllergens(final String allergens) {
        fAllergens = allergens;
    }

    public boolean hasAllergens() {
        return fAllergens != null && !fAllergens.isEmpty();
    }

    public boolean containsAllergen(final String allergen) {
        if (fAllergens == null || fAllergens.isEmpty()) {
            return false;
        }
        String lowerAllergens = fAllergens.toLowerCase();
        return lowerAllergens.contains(allergen.toLowerCase());
    }

    /**
     * Returns the category of this beer.
     * <p>
     * The category indicates the type of beer, such as "beer" for standard beers,
     * or "low-no" for low or no alcohol beers. Other possible values may be defined
     * by the festival or data source.
     *
     * @return the category string for this beer (e.g., "beer", "low-no")
     */
    public String getCategory() {
        return fCategory;
    }

    /**
     * Sets the category for this beer.
     * <p>
     * Valid category values include "beer" for standard beers and "low-no" for low or no alcohol beers.
     * Other values may be used as defined by the festival or data source.
     *
     * @param category the category string to set (e.g., "beer", "low-no")
     */
    public void setCategory(final String category) {
        fCategory = category;
    }

    /**
     * Determines if this beer is classified as low or no alcohol.
     * <p>
     * A beer is considered low or no alcohol if its ABV (Alcohol By Volume) is less than or equal to 0.5%.
     * This threshold is used to identify beers that are suitable for those seeking minimal or no alcohol content.
     *
     * @return true if the beer's ABV is less than or equal to 0.5%, false otherwise
     */
    public boolean isLowOrNoAlcohol() {
        return fAbv <= 0.5f;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Beer beer = (Beer) o;

        if (Float.compare(beer.fAbv, fAbv) != 0) return false;
        if (fIsOnWishList != beer.fIsOnWishList) return false;
        if (fRating != beer.fRating) return false;
        if (fBrewery != null ? !fBrewery.equals(beer.fBrewery) : beer.fBrewery != null) return false;
        if (fDescription != null ? !fDescription.equals(beer.fDescription) : beer.fDescription != null) return false;
        if (fFestivalID != null ? !fFestivalID.equals(beer.fFestivalID) : beer.fFestivalID != null) return false;
        if (fName != null ? !fName.equals(beer.fName) : beer.fName != null) return false;
        if (fStatus != null ? !fStatus.equals(beer.fStatus) : beer.fStatus != null) return false;
        if (fStyle != null ? !fStyle.equals(beer.fStyle) : beer.fStyle != null) return false;
        if (fDispense != null ? !fDispense.equals(beer.fDispense) : beer.fDispense != null) return false;
        if (fUserComments != null ? !fUserComments.equals(beer.fUserComments) : beer.fUserComments != null)
            return false;
        if (fAllergens != null ? !fAllergens.equals(beer.fAllergens) : beer.fAllergens != null) return false;
        if (fCategory != null ? !fCategory.equals(beer.fCategory) : beer.fCategory != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fBrewery != null ? fBrewery.hashCode() : 0;
        result = 31 * result + (fName != null ? fName.hashCode() : 0);
        result = 31 * result + (fAbv != +0.0f ? Float.floatToIntBits(fAbv) : 0);
        result = 31 * result + (fDescription != null ? fDescription.hashCode() : 0);
        result = 31 * result + (fStatus != null ? fStatus.hashCode() : 0);
        result = 31 * result + fRating;
        result = 31 * result + (fFestivalID != null ? fFestivalID.hashCode() : 0);
        result = 31 * result + (fStyle != null ? fStyle.hashCode() : 0);
        result = 31 * result + (fDispense != null ? fDispense.hashCode() : 0);
        result = 31 * result + (fIsOnWishList ? 1 : 0);
        result = 31 * result + (fUserComments != null ? fUserComments.hashCode() : 0);
        result = 31 * result + (fAllergens != null ? fAllergens.hashCode() : 0);
        result = 31 * result + (fCategory != null ? fCategory.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Beer");
        sb.append("{fId=").append(fId);
        sb.append(", fBrewery=").append(fBrewery);
        sb.append(", fName='").append(fName).append('\'');
        sb.append(", fAbv=").append(fAbv);
        sb.append(", fDescription='").append(fDescription).append('\'');
        sb.append(", fStatus='").append(fStatus).append('\'');
        sb.append(", fRating=").append(fRating);
        sb.append(", fFestivalID='").append(fFestivalID).append('\'');
        sb.append(", fStyle='").append(fStyle).append('\'');
        sb.append(", fDispense='").append(fDispense).append('\'');
        sb.append(", fIsOnWishList=").append(fIsOnWishList);
        sb.append(", fUserComments='").append(fUserComments).append('\'');
        sb.append(", fAllergens='").append(fAllergens).append('\'');
        sb.append(", fCategory='").append(fCategory).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
