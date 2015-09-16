package info.thepass.altmetro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.ui.TrackListFragment;

public class TrackListAdapter extends ArrayAdapter<Track> {
	private final static String TAG = "TrakListAdapter";
	private Context context;
	private ArrayList<Track> listTrack;
	private HelperMetro h;
	public int selectedItem = -1;
    public TrackListFragment frag;
    private TrackData trackData;

	public TrackListAdapter(Context cont, int layout,
                            TrackData trackData2, HelperMetro hConstructor) {
		super(cont, layout, trackData2.tracks);
		h = hConstructor;
		h.logD(TAG, "constructor");
        trackData = trackData2;
		context = cont;
		listTrack = trackData.tracks;
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
            holder.titel = (TextView) rowView.findViewById(R.id.tv_tracklist_titel);
            holder.edit = (ImageView) rowView.findViewById(R.id.iv_tracklist_edit);
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "TrackList edit onclicklistener " );
                    frag.editTrackListItem(v);
                }
            });
			rowView.setTag(holder);
		} else {
			holder = (ViewHolderPattern) rowView.getTag();
		}

		Track track = listTrack.get(position);
        holder.titel.setText(track.getTitle(trackData, position));
		return rowView;
	}

	public static class ViewHolderPattern {
		public LinearLayout rij;
		public TextView titel;
        public ImageView edit;
    }
}