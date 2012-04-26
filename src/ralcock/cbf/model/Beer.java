package ralcock.cbf.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Beer.TABLE_NAME)
public final class Beer implements Serializable {

    public static final String TABLE_NAME = "beers";

    public static final String NAME_FIELD = "name";
    public static final String BREWERY_FIELD = "brewery";
    public static final String ABV_FIELD = "abv";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String STATUS_FIELD = "status";


    @DatabaseField(columnName = "_id", generatedId = true)
    private long fId;

    @DatabaseField(columnName = BREWERY_FIELD, foreign = true, foreignAutoRefresh = true)
    private Brewery fBrewery;

    @DatabaseField(columnName = NAME_FIELD)
    private String fName;

    @DatabaseField(columnName = ABV_FIELD)
    private float fAbv;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String fDescription;

    @DatabaseField(columnName = STATUS_FIELD)
    private String fStatus;

    @SuppressWarnings("UnusedDeclaration")
        // needed by ormlite
    Beer() {
    }

    public Beer(final Brewery brewery,
                final String name,
                final float abv,
                final String description,
                final String status) {
        fBrewery = brewery;
        fName = name;
        fAbv = abv;
        fDescription = description;
        fStatus = status;
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

    public long getId() {
        return fId;
    }

    public void setBrewery(final Brewery brewery) {
        fBrewery = brewery;
    }

    public void setName(final String name) {
        fName = name;
    }

    public void setAbv(final float abv) {
        fAbv = abv;
    }

    public void setDescription(final String description) {
        fDescription = description;
    }

    public void setStatus(final String status) {
        fStatus = status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Beer beer = (Beer) o;

        if (Float.compare(beer.fAbv, fAbv) != 0) return false;
        if (fBrewery != null ? !fBrewery.equals(beer.fBrewery) : beer.fBrewery != null) return false;
        if (fName != null ? !fName.equals(beer.fName) : beer.fName != null) return false;
        if (fDescription != null ? !fDescription.equals(beer.fDescription) : beer.fDescription != null) return false;
        if (fStatus != null ? !fStatus.equals(beer.fStatus) : beer.fStatus != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fBrewery != null ? fBrewery.hashCode() : 0;
        result = 31 * result + (fName != null ? fName.hashCode() : 0);
        result = 31 * result + (fAbv != +0.0f ? Float.floatToIntBits(fAbv) : 0);
        result = 31 * result + (fDescription != null ? fDescription.hashCode() : 0);
        result = 31 * result + (fStatus != null ? fStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Beer{" +
                "fBrewery=" + fBrewery +
                ", fName='" + fName + '\'' +
                ", fAbv=" + fAbv +
                ", fDescription='" + fDescription + '\'' +
                ", fStatus='" + fStatus + '\'' +
                '}';
    }

}
