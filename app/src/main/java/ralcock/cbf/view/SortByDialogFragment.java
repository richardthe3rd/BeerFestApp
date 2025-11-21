package ralcock.cbf.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;
import ralcock.cbf.model.SortOrder;

public class SortByDialogFragment extends DialogFragment {

    public static SortByDialogFragment newInstance(final SortOrder currentSortOrder) {
        SortByDialogFragment fragment = new SortByDialogFragment();
        Bundle args = new Bundle();
        args.putString("sortOrder", currentSortOrder.name());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.sort_dialog_title);
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
                CamBeerFestApplication app = (CamBeerFestApplication) requireActivity();
                app.doDismissSortDialog(sortOrders[i]);
            }
        });
        return builder.create();
    }

}
