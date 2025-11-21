package ralcock.cbf.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ralcock.cbf.R;

public class AboutDialogFragment extends DialogFragment {
    public static AboutDialogFragment newInstance(final String appName, final String versionName) {
        AboutDialogFragment fragment = new AboutDialogFragment();
        Bundle args = new Bundle();
        args.putString("appName", appName);
        args.putString("versionName", versionName);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String versionName = getArguments().getString("versionName");
        String appName = getArguments().getString("appName");
        View layout = LayoutInflater.from(requireActivity()).inflate(R.layout.about_dialog, null);
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

        return new AlertDialog.Builder(requireActivity())
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
