package ralcock.cbf.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import ralcock.cbf.R;

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
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.about_dialog, null);
        TextView version = (TextView) layout.findViewById(R.id.aboutVersion);
        version.setText(appName + " v" + versionName);

        TextView festival = (TextView) layout.findViewById(R.id.festivalLink);
        festival.setText(R.string.festival_name, TextView.BufferType.SPANNABLE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(final View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(R.string.festival_website_url)));
                startActivity(browserIntent);
            }
        };
        Spannable span = (Spannable) festival.getText();
        span.setSpan(clickableSpan,
                0, span.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        festival.setMovementMethod(LinkMovementMethod.getInstance());

        return new AlertDialog.Builder(getActivity())
                .setTitle(appName)
                .setView(layout)
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }
}
