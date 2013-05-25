package ralcock.cbf.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import ralcock.cbf.R;

public class LoadBeersProgressDialogFragment extends DialogFragment {

    private ProgressDialog fProgressDialog;

    public static DialogFragment newInstance() {
        return new LoadBeersProgressDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        fProgressDialog = new ProgressDialog(getActivity());
        fProgressDialog.setMessage(getText(R.string.loading_message));
        fProgressDialog.setIndeterminate(true);
        fProgressDialog.setCancelable(false);
        return fProgressDialog;
    }

    public void setMessage(final String message) {
        fProgressDialog.setMessage(message);
    }
}
