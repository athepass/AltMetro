package info.thepass.altmetro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pattern;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackItemsAdapter extends ArrayAdapter<Pattern> {
	private final static String TAG = "TrakItemsAdapter";
	private Context context;
	private ArrayList<Pattern> listPat;
	private HelperMetro h;
	public int selectedItem = -1;
    private Track track;

	public TrackItemsAdapter(Context cont, int layout,
                             Track track, HelperMetro hConstructor) {
		super(cont, layout, track.pats);
		h = hConstructor;
		h.logD(TAG, "constructor");
        this.track = track;
		context = cont;
		listPat = track.pats;
	}

	public int getCount() {
		return listPat.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolderPattern holder;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.fragment_tracklist_row,
					parent, false);
			holder = new ViewHolderPattern();
            holder.titel = (TextView) rowView.findViewById(R.id.tv_tracklist_titel);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolderPattern) rowView.getTag();
		}

		Pattern Pattern = (Pattern) listPat.get(position);
        holder.titel.setText(Pattern.getTitle());
		return rowView;
	}

	public static class ViewHolderPattern {
		public LinearLayout rij;
		public TextView titel;
	}
}