package ralcock.cbf.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AboutDialogFragment extends DialogFragment {
    public static DialogFragment newInstance(String appName, String versionName) {
        DialogFragment fragment = new AboutDialogFragment();
        Bundle args = new Bundle();
        args.putString("appName", appName);
        args.putString("versionName", versionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String versionName = getArguments().getString("versionName");
        String appName = getArguments().getString("appName");
        return new AlertDialog.Builder(getActivity())
                .setMessage(appName + "\nVersion: " + versionName)
                .create();
    }
}
