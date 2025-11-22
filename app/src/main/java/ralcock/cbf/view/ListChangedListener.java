package ralcock.cbf.view;

import ralcock.cbf.model.SortOrder;
import ralcock.cbf.model.StatusToShow;

import java.util.Set;

public interface ListChangedListener {
    void filterTextChanged(final String filterText);

    void sortOrderChanged(final SortOrder sortOrder);

    void stylesToHideChanged(final Set<String> stylesToHide);

    void allergensToHideChanged(final Set<String> allergensToHide);

    void statusToShowChanged(final StatusToShow statusToShow);

    void beersChanged();
}
