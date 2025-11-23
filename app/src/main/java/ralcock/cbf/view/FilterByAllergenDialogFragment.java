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

/**
 * Dialog for filtering beers by allergens to hide.
 * Users select which allergens they want to exclude from the beer list.
 */
public class FilterByAllergenDialogFragment extends DialogFragment {

    public static FilterByAllergenDialogFragment newInstance(final Set<String> allergensToHide) {
        FilterByAllergenDialogFragment fragment = new FilterByAllergenDialogFragment();
        Bundle args = new Bundle();
        putStringSet(args, "allergensToHide", allergensToHide);
        fragment.setArguments(args);
        return fragment;
    }

    private static void putStringSet(final Bundle args, final String key, final Set<String> stringSet) {
        ArrayList<String> list = new ArrayList<String>(stringSet);
        args.putStringArrayList(key, list);
    }

    private static Set<String> getStringSet(final Bundle arguments, final String key) {
        ArrayList<String> list = arguments.getStringArrayList(key);
        if (list == null) {
            return new HashSet<String>();
        }
        return new HashSet<String>(list);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Set<String> allergensToHide = getStringSet(getArguments(), "allergensToHide");

        // Get allergens actually present in the festival beers
        CamBeerFestApplication app = (CamBeerFestApplication) requireActivity();
        Set<String> availableAllergens = app.getBeers().getAvailableAllergens();
        final String[] allAllergens = availableAllergens.toArray(new String[0]);
        final boolean[] checkedItems = new boolean[allAllergens.length];

        // Mark currently hidden allergens as checked
        for (int i = 0; i < allAllergens.length; i++) {
            checkedItems[i] = allergensToHide.contains(allAllergens[i].toLowerCase());
        }

        final Set<String> selectedAllergens = new HashSet<>(allergensToHide);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.filter_allergen_dialog_title);

        builder.setMultiChoiceItems(allAllergens, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String allergen = allAllergens[which].toLowerCase();
                        if (isChecked) {
                            selectedAllergens.add(allergen);
                        } else {
                            selectedAllergens.remove(allergen);
                        }
                    }
                });

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
                CamBeerFestApplication app = (CamBeerFestApplication) requireActivity();
                app.doDismissFilterByAllergenDialog(selectedAllergens);
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton(R.string.filter_allergen_clear_all, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
                CamBeerFestApplication app = (CamBeerFestApplication) requireActivity();
                app.doDismissFilterByAllergenDialog(new HashSet<String>());
            }
        });

        return builder.create();
    }
}
