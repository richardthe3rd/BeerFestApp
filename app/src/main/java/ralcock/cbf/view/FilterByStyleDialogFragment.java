package ralcock.cbf.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ralcock.cbf.CamBeerFestApplication;
import ralcock.cbf.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FilterByStyleDialogFragment extends DialogFragment {

    public static FilterByStyleDialogFragment newInstance(final Set<String> stylesToHide,
            final Set<String> allStyles) {
        FilterByStyleDialogFragment fragment = new FilterByStyleDialogFragment();
        Bundle args = new Bundle();
        putStringSet(args, "stylesToHide", stylesToHide);
        putStringSet(args, "allStyles", allStyles);
        fragment.setArguments(args);
        return fragment;
    }

    private static void putStringSet(final Bundle args, final String key, final Set<String> stringSet) {
        ArrayList<String> list = new ArrayList<String>(stringSet);
        args.putStringArrayList(key, list);
    }

    private static Set<String> getStringSet(final Bundle arguments, String key) {
        ArrayList<String> list = arguments.getStringArrayList(key);
        return new HashSet<String>(list);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Set<String> stylesToHide = getStringSet(getArguments(), "stylesToHide");
        Set<String> allStyles = getStringSet(getArguments(), "allStyles");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.filter_style_dialog_title);

        final BeerStyleListAdapter listAdapter = new BeerStyleListAdapter(requireActivity(), allStyles, stylesToHide);

        builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                // NOTHING TO DO HERE
            }
        });

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                AlertDialog alertDialog = (AlertDialog) dialogInterface;
                BeerStyleListAdapter listAdapter = (BeerStyleListAdapter) alertDialog.getListView().getAdapter();

                dialogInterface.dismiss();
                CamBeerFestApplication app = (CamBeerFestApplication) requireActivity();
                app.doDismissFilterByStyleDialog(listAdapter.getStylesToHide());

            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }
}
