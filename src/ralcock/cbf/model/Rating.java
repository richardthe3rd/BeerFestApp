package ralcock.cbf.model;

import ralcock.cbf.R;

@Deprecated
public enum Rating {
    UNRATED(R.string.Rating_Unrated),
    HORRIBLE(R.string.Rating_Horrible),
    OK(R.string.Rating_OK),
    LOVE(R.string.Rating_Love);

    private int fId;

    Rating(int id) {
        fId = id;
    }

    public int getId() {
        return fId;
    }

    public StarRating toStarRating() {
        switch(this) {
            case LOVE:
                return new StarRating(5);
            case OK:
                return new StarRating(3);
            case HORRIBLE:
                return new StarRating(1);
            default:
                return new StarRating(0);
        }
    }
}
