package ralcock.cbf.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.DialogFragment;
import ralcock.cbf.R;

public class UpdateBeersProgressDialogFragment extends DialogFragment {

    private ProgressDialog fProgressDialog;

    public static DialogFragment newInstance(final int progress, final int max) {
        DialogFragment fragment = new UpdateBeersProgressDialogFragment();

        Bundle args = new Bundle();
        args.putInt("progress", progress);
        args.putInt("max", max);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        int progress = getArguments().getInt("progress");
        int max = getArguments().getInt("max");

        fProgressDialog = new ProgressDialog(getActivity());
        fProgressDialog.setMessage(getString(R.string.updating_database));
        fProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fProgressDialog.setIndeterminate(false);
        fProgressDialog.setCancelable(false);
        fProgressDialog.setProgress(progress);
        fProgressDialog.setMax(max);
        return fProgressDialog;
    }

    public void setMessage(final String message) {
        fProgressDialog.setMessage(message);
    }

    public void incrementProgressBy(final int i) {
        fProgressDialog.incrementProgressBy(i);
    }
}
