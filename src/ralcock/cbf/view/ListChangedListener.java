package ralcock.cbf.view;

import ralcock.cbf.model.SortOrder;

public interface ListChangedListener {
    void filterTextChanged(String filterText);

    void sortOrderChanged(SortOrder sortOrder);
}
