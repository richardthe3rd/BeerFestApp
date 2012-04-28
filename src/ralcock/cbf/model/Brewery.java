package ralcock.cbf.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import ralcock.cbf.model.dao.BreweryDaoImpl;

import java.io.Serializable;

@DatabaseTable(tableName = Brewery.TABLE_NAME, daoClass = BreweryDaoImpl.class)
public final class Brewery implements Serializable {

    public static final String TABLE_NAME = "breweries";

    public static final String NAME_FIELD = "name";

    public static final String DESCRIPTION_FIELD = "description";

    public static final String ID_FIELD = "_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int fId;

    @DatabaseField(columnName = NAME_FIELD)
    private String fName;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String fDescription;

    @SuppressWarnings("UnusedDeclaration")
        // needed by ormlite
    Brewery() {
    }

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

    public int getId() {
        return fId;
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
