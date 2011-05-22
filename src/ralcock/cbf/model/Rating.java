package ralcock.cbf.model;

import ralcock.cbf.R;

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
}
