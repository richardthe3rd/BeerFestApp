package ralcock.cbf.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import ralcock.cbf.R;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class BeerStyleListAdapter extends BaseAdapter {
    private final Vector<String> fStyleList;
    private final Context fContext;
    private final Set<String> fStylesToHide;

    public BeerStyleListAdapter(final Context context, final Set<String> allStyles, final Set<String> stylesToHide) {
        super();
        fContext = context;
        fStylesToHide = new HashSet<String>(stylesToHide);
        fStyleList = new Vector<String>(allStyles);
    }

    public int getCount() {
        return fStyleList.size() + 1;
    }

    public Object getItem(final int i) {
        if (i == 0) {
            return null;
        } else {
            return fStyleList.get(i);
        }
    }

    public long getItemId(final int i) {
        return i;
    }

    public View getView(final int i, View view, final ViewGroup viewGroup) {

        ItemView itemView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(fContext);
            view = layoutInflater.inflate(android.R.layout.select_dialog_multichoice, viewGroup, false);
            itemView = new ItemView(view);
            view.setTag(itemView);
        } else {
            itemView = (ItemView) view.getTag();
        }

        if (i == 0) {
            itemView.StyleTextView.setText(R.string.filter_style_dialog_show_all);
            itemView.StyleTextView.setTypeface(Typeface.DEFAULT_BOLD);
            itemView.ShowStyleCheck.setChecked(fStylesToHide.isEmpty());
            itemView.ShowStyleCheck.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View view) {
                    toggleShowAllStyles();
                }
            });
        } else {
            String style = fStyleList.get(i - 1);
            itemView.StyleTextView.setText(style);
            itemView.StyleTextView.setTypeface(Typeface.DEFAULT);
            itemView.ShowStyleCheck.setChecked(!fStylesToHide.contains(style));
            itemView.ShowStyleCheck.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View view) {
                    toggleShowStyle(i - 1);
                }
            });
        }
        return view;
    }

    private void toggleShowAllStyles() {
        if (fStylesToHide.isEmpty()) {
            fStylesToHide.addAll(fStyleList);
        } else {
            fStylesToHide.clear();
        }
        notifyDataSetChanged();
    }

    private void toggleShowStyle(final int itemIndex) {
        String style = fStyleList.get(itemIndex);
        if (fStylesToHide.contains(style)) {
            fStylesToHide.remove(style);
        } else {
            fStylesToHide.add(style);
        }
        notifyDataSetChanged();
    }

    public Set<String> getStylesToHide() {
        return fStylesToHide;
    }

    private static class ItemView {
        TextView StyleTextView;
        CheckedTextView ShowStyleCheck;

        private ItemView(final View view) {
            StyleTextView = (TextView) view;
            ShowStyleCheck = (CheckedTextView) view;
        }
    }

}
