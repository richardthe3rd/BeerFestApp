package ralcock.cbf.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.Serializable;
import ralcock.cbf.model.dao.BreweriesImpl;

@DatabaseTable(tableName = Brewery.TABLE_NAME, daoClass = BreweriesImpl.class)
public final class Brewery implements Serializable {

    public static final String TABLE_NAME = "breweries";
    public static final String ID_FIELD = "_id";
    public static final String FESTIVAL_ID_FIELD = "festival_id";
    public static final String NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private long fId;

    @DatabaseField(columnName = FESTIVAL_ID_FIELD, index = true, unique = true)
    private String fFestivalID;

    @DatabaseField(columnName = NAME_FIELD, index = true)
    private String fName;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String fDescription;

    @SuppressWarnings("UnusedDeclaration")
    // needed by ormlite
    Brewery() {}

    public Brewery(final String festivalId, final String name, final String description) {
        fFestivalID = festivalId;
        fName = name;
        fDescription = description;
    }

    public String getName() {
        return fName;
    }

    public String getDescription() {
        return fDescription;
    }

    public String getFestivalID() {
        return fFestivalID;
    }

    public void setId(final long id) {
        fId = id;
    }

    public long getId() {
        return fId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Brewery brewery = (Brewery) o;

        if (fDescription != null
                ? !fDescription.equals(brewery.fDescription)
                : brewery.fDescription != null) return false;
        if (fFestivalID != null
                ? !fFestivalID.equals(brewery.fFestivalID)
                : brewery.fFestivalID != null) return false;
        if (fName != null ? !fName.equals(brewery.fName) : brewery.fName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fFestivalID != null ? fFestivalID.hashCode() : 0;
        result = 31 * result + (fName != null ? fName.hashCode() : 0);
        result = 31 * result + (fDescription != null ? fDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Brewery");
        sb.append("{fId=").append(fId);
        sb.append(", fFestivalID='").append(fFestivalID).append('\'');
        sb.append(", fName='").append(fName).append('\'');
        sb.append(", fDescription='").append(fDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
