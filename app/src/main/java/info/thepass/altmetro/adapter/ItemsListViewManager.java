package info.thepass.altmetro.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.dialog.DialogEditTrackPattern;
import info.thepass.altmetro.dialog.DialogEditTrackRepeat;
import info.thepass.altmetro.dialog.DialogEditTrackStudy;
import info.thepass.altmetro.dialog.DialogEditTrackTap;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;
import info.thepass.altmetro.ui.TrackFragment;

/**
 * Created by nl03192 on 22-9-2015.
 */
public class ItemsListViewManager {
    public final static String TAG = "trak: ItemsListViewManager";
    public ListView itemsListView;
    public TrackItemsAdapter itemsAdapter;
    public TrackFragment frag;
    public HelperMetro h;
    public Track track;
    public TrackData trackData;
    public LinearLayout llRoot;

    private int indexDelRepeat;
    private int indexDelPattern;

    public void initListView() {
        llRoot= (LinearLayout) h.getActivity().findViewById(R.id.ll_track_root);

        itemsListView = (ListView) h.getActivity().findViewById(R.id.track_listView);
        itemsAdapter = new TrackItemsAdapter(h.getActivity(), R.layout.fragment_tracklist_row, itemsListView,
                track, trackData, h, frag);
        itemsListView.setAdapter(itemsAdapter);
        itemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(TAG, "itemClickListener " + position);
                switch (itemsAdapter.getItemViewType(position)) {
                    case TrackItemsAdapter.ROWTYPEREPEAT:
                        itemsAdapter.selectedRepeat = track.getItemRepeatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEREPEATADD:
                        editRepeat(track.repeats.size(), true);
                        break;
                    case TrackItemsAdapter.ROWTYPEPAT:
                        itemsAdapter.selectedPat = track.getItemPatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEPATADD:
                        frag.lvManager.editPattern(trackData.pats.size(), true);
                        break;
                    default:
                        throw new RuntimeException("listview click at position " + position);
                }
            }
        });
        itemsListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        itemsAdapter.selectedRepeat = 0;
        itemsAdapter.selectedPat = 0;
    }

    public void editRepeat(int position, boolean add) {
        DialogEditTrackRepeat dlgEdit = new DialogEditTrackRepeat();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(frag, Keys.TARGETEDITREPEAT);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        int index = track.getItemRepeatPosition(position);
        b.putInt(Keys.EDITINDEX, index);
        b.putInt(Keys.EDITSIZE, track.repeats.size());
        b.putBoolean(Track.KEYMULTI, track.multi);

        JSONArray patsArray = new JSONArray();
        for (int i = 0; i < trackData.pats.size(); i++) {
            patsArray.put(trackData.pats.get(i).toJson());
        }
        b.putString(TrackData.KEYPATS, patsArray.toString());

        Repeat repeat;
        if (add) {
            repeat = new Repeat();
        } else {
            repeat = track.repeats.get(index);
        }
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackRepeat.TAG);
    }

    public void updateRepeat(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sRepeat = intent.getStringExtra(Track.KEYREPEATS);
        try {
            Repeat repeat = new Repeat();
            repeat.fromJson(new JSONObject(sRepeat));
            if (actionAdd) {
                track.repeats.add(index, repeat);
            } else {
                track.repeats.set(index, repeat);
            }
            itemsAdapter.selectedRepeat = index;
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            throw new RuntimeException("updateRepeat json exception");
        }
        trackData.saveData("updateRepeat", false);
        frag.setData();
        itemsAdapter.notifyDataSetChanged();
    }

    public void confirmDeleteRepeat(int position) {
        if (track.repeats.size() == 1) {
            return;
        }
        int index = track.getItemRepeatPosition(position);
        indexDelRepeat = index;
        itemsAdapter.selectedRepeat = indexDelRepeat;

        AlertDialog.Builder builder = new AlertDialog.Builder(frag.getActivity());
        Repeat repeat = track.repeats.get(indexDelRepeat);
        String sPat = trackData.pats.get(repeat.indexPattern).display(h, repeat.indexPattern, true);
        String pInfo = repeat.display(h, index, sPat, true);
        builder.setMessage(h.getString(R.string.list_confirm_delete_item) + " " + pInfo)
                .setCancelable(false)
                .setPositiveButton(h.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteItemRepeat();
                            }
                        })
                .setNegativeButton(h.getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteItemRepeat() {
        track.repeats.remove(indexDelRepeat);
        if (indexDelRepeat >= track.repeats.size() - 1) {
            itemsAdapter.selectedRepeat = track.repeats.size() - 1;
        }
        trackData.saveData("deleteRepeat", false);
        frag.setData();
        itemsAdapter.notifyDataSetChanged();
    }

    public void editPattern(final int position, boolean add) {
        itemsAdapter.selectedPat = track.getItemPatPosition(position);
        DialogEditTrackPattern dlgEdit = new DialogEditTrackPattern();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(frag, Keys.TARGETEDITPATTERN);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        int index = track.getItemPatPosition(position);
        b.putInt(Keys.EDITINDEX, index);
        b.putInt(Keys.EDITSIZE, trackData.pats.size());

        Pat pat;
        if (add) {
            pat = new Pat(h);
        } else {
            pat = trackData.pats.get(index);
        }
        b.putString(TrackData.KEYPATS, pat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackPattern.TAG);
    }

    public void updatePattern(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sPat = intent.getStringExtra(TrackData.KEYPATS);
        try {
            Pat pat = new Pat(h);
            pat.fromJson(new JSONObject(sPat));
            if (actionAdd) {
                trackData.pats.add(index, pat);
            } else {
                trackData.pats.set(index, pat);
            }
            itemsAdapter.selectedPat = index;
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            throw new RuntimeException("updatePattern json exception");
        }
        trackData.saveData("updatePattern", false);
        frag.setData();
        itemsAdapter.notifyDataSetChanged();
    }

    public void confirmDeletePattern(int position) {
        if (trackData.pats.size() == 1) {
            return;
        }
        int index = track.getItemPatPosition(position);
        indexDelPattern = index;
        itemsAdapter.selectedPat = indexDelPattern;
        Pat pat = trackData.pats.get(indexDelPattern);
        // verwijderen niet mogelijk indien nog in gebruik.!!
        if (pat.checkInUse(trackData, h)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(frag.getActivity());
        String pInfo = pat.display(h, indexDelPattern, true);
        builder.setMessage(h.getString(R.string.list_confirm_delete_item) + " " + pInfo)
                .setCancelable(false)
                .setPositiveButton(h.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteItemPattern();
                            }
                        })
                .setNegativeButton(h.getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteItemPattern() {
        trackData.pats.remove(indexDelPattern);
        if (indexDelPattern >= trackData.pats.size() - 1) {
            itemsAdapter.selectedPat = trackData.pats.size() - 1;
        }
        trackData.saveData("deletePattern", false);
        frag.setData();
        itemsAdapter.notifyDataSetChanged();
    }

    public void editTap() {
        DialogEditTrackTap dlgEdit = new DialogEditTrackTap();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(frag, Keys.TARGETEDITTAP);

        Bundle b = new Bundle();
        int index = itemsAdapter.selectedRepeat;
        b.putInt(Keys.EDITINDEX, index);
        Repeat repeat = track.repeats.get(index);
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackTap.TAG);
    }

    public void editSpeedStudy() {
        DialogEditTrackStudy dlgEdit = new DialogEditTrackStudy();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(frag, Keys.TARGETEDITSTUDY);

        Bundle b = new Bundle();
        Study study = track.study;
        b.putString(Track.KEYSTUDY, study.toJson().toString());
        b.putInt(Repeat.KEYTEMPO, track.repeats.get(track.repeatSelected).tempo);

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackStudy.TAG);
    }
}
