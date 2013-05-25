package ralcock.cbf.view;

import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.StatusToShow;

import java.util.Set;

public interface ListChangedListener {
    void filterTextChanged(String filterText);

    void sortOrderChanged(SortOrder sortOrder);

    void stylesToHideChanged(Set<String> stylesToHide);

    void statusToShowChanged(StatusToShow statusToShow);

    void beersChanged();
}
