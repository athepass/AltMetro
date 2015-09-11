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
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackListAdapter extends ArrayAdapter<Track> {
	private final static String TAG = "TrackListAdapter";
	private Context context;
	private ArrayList<Track> listTrack;
	private HelperMetro h;
	public int selectedItem = -1;

	public TrackListAdapter(Context cont, int layout,
                            ArrayList<Track> tracks, HelperMetro hConstructor) {
		super(cont, layout, tracks);
		h = hConstructor;
		h.logD(TAG, "constructor");
		context = cont;
		listTrack = tracks;
	}

	public int getCount() {
		return listTrack.size();
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
			holder.nummer = (TextView) rowView.findViewById(R.id.tv_tracklist_nummer);
            holder.titel = (TextView) rowView.findViewById(R.id.tv_tracklist_titel);
            holder.multi = (TextView) rowView.findViewById(R.id.tv_tracklist_multi);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolderPattern) rowView.getTag();
		}

		Track track = (Track) listTrack.get(position);
		holder.nummer.setText((position +1) + "");
        holder.titel.setText(track.titel + "");
		holder.multi.setText((track.multi) ? "*" : "-" );
		return rowView;
	}

	public static class ViewHolderPattern {
		public LinearLayout rij;
		public TextView nummer;
		public TextView titel;
		public TextView multi;
	}
}