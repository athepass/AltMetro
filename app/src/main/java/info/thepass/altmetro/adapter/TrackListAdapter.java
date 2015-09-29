package info.thepass.altmetro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.aaaUI.TrackListFragment;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackListAdapter extends ArrayAdapter<Track> {
    private final static String TAG = "TrakListAdapter";
    public ListView lv;
    public int selectedItem = -1;
    public int positionToolbar = -1;
    public TrackListFragment frag;
    private Context context;
    private ArrayList<Track> listTrack;
    private HelperMetro h;
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

    private void initButtons(View rowView, ViewHolderTrack holder) {
        holder.play = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_play);
        holder.play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                frag.getFragmentManager().popBackStack();
            }
        });

        holder.edit = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_edit);
        holder.edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = lv.getPositionForView(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.editTrackList(position, false);
            }
        });

        holder.delete = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_delete);
        holder.delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = lv.getPositionForView(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.confirmDeleteItem(position);
            }
        });

        holder.add = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_add);
        holder.add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = lv.getPositionForView(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.editTrackList(position, true);
            }
        });

        holder.up = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_up);
        holder.up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = lv.getPositionForView(v);
                if (position >= 1) {
                    positionToolbar--;
                    lv.setItemChecked(positionToolbar,true);
                    lv.setSelection(positionToolbar);
                    Track track0 = trackData.tracks.get(position - 1);
                    Track track1 = trackData.tracks.get(position);
                    trackData.tracks.set(position - 1, track1);
                    trackData.tracks.set(position, track0);
                }
                notifyDataSetChanged();
            }
        });

        holder.down = (ImageView) rowView.findViewById(R.id.iv_tracklistrow_down);
        holder.down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = lv.getPositionForView(v);
                if (position < trackData.tracks.size() - 1) {
                    positionToolbar++;
                    lv.setItemChecked(positionToolbar,true);
                    lv.setSelection(positionToolbar);
                    Track track0 = trackData.tracks.get(position);
                    Track track1 = trackData.tracks.get(position + 1);
                    trackData.tracks.set(position, track1);
                    trackData.tracks.set(position + 1, track0);
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderTrack holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_tracklist_row,
                    parent, false);
            holder = new ViewHolderTrack();
            holder.rijToolbar = (LinearLayout) rowView.findViewById(R.id.ll_tracklistrow_toolbar);
            holder.titel = (TextView) rowView.findViewById(R.id.tv_tracklist_titel);
            initButtons(rowView, holder);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderTrack) rowView.getTag();
        }

        Track track = listTrack.get(position);
        holder.titel.setText(track.display(h, position));
        holder.rijToolbar.setVisibility((position == positionToolbar) ? View.VISIBLE : View.GONE);

        return rowView;
    }

    public static class ViewHolderTrack {
        public LinearLayout rijToolbar;
        public TextView titel;
        public ImageView play;
        public ImageView edit;
        public ImageView delete;
        public ImageView add;
        public ImageView up;
        public ImageView down;
    }
}