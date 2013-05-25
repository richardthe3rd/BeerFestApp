package ralcock.cbf.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;
import ralcock.cbf.model.SortOrder;

public class SortByDialogFragment extends DialogFragment {

    public static DialogFragment newInstance(SortOrder currentSortOrder) {
        SortByDialogFragment fragment = new SortByDialogFragment();
        Bundle args = new Bundle();
        args.putString("sortOrder", currentSortOrder.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        SortOrder currentSortOrder = SortOrder.valueOf(getArguments().getString("sortOrder"));

        final SortOrder[] sortOrders = SortOrder.values();
        final CharSequence[] items = new CharSequence[sortOrders.length];
        int checkedItem = -1;
        for (int i = 0; i < items.length; i++) {
            items[i] = sortOrders[i].toString();
            if (sortOrders[i] == currentSortOrder) {
                checkedItem = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sort_dialog_title);
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                CamBeerFestApplication app = (CamBeerFestApplication) getActivity();
                app.doDismissSortDialog(sortOrders[i]);
            }
        });
        return builder.create();
    }

}
