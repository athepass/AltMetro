package info.thepass.altmetro.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.ui.TrackFragment;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.MetronomeData;
import info.thepass.altmetro.dialogs.DialogEditTrackPattern;
import info.thepass.altmetro.dialogs.DialogEditTrackRepeat;
import info.thepass.altmetro.dialogs.DialogEditTrackStudy;
import info.thepass.altmetro.dialogs.DialogEditTrackTap;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

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
    public MetronomeData metronomeData;
    public LinearLayout llRoot;

    private int indexDelRepeat;
    private int indexDelPattern;

    public void initListView() {
        llRoot= (LinearLayout) h.getActivity().findViewById(R.id.ll_track_root);

        itemsListView = (ListView) h.getActivity().findViewById(R.id.track_listView);
        itemsAdapter = new TrackItemsAdapter(h.getActivity(), R.layout.fragment_tracklist_row, itemsListView,
                track, metronomeData, h, frag);
        itemsListView.setAdapter(itemsAdapter);
        itemsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                h.logD(TAG, "itemClickListener " + position);
                switch (itemsAdapter.getItemViewType(position)) {
                    case TrackItemsAdapter.ROWTYPEREPEAT:
                        itemsAdapter.selectedRepeat = track.getItemRepeatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEREPEATADD:
                        editRepeat(track.repeatList.size(), true);
                        break;
                    case TrackItemsAdapter.ROWTYPEPAT:
                        itemsAdapter.selectedPat = track.getItemPatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEPATADD:
                        frag.lvManager.editPattern(track.patList.size(), true);
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
        b.putInt(Keys.EDITSIZE, track.repeatList.size());
        b.putBoolean(Track.KEYMULTI, track.multi);

        JSONArray patsArray = new JSONArray();
        for (int i = 0; i < track.patList.size(); i++) {
            patsArray.put(track.patList.get(i).toJson());
        }
        b.putString(Track.KEYPATS, patsArray.toString());

        Repeat repeat;
        if (add) {
            repeat = new Repeat();
        } else {
            repeat = track.repeatList.get(index);
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
                track.repeatList.add(index, repeat);
            } else {
                track.repeatList.set(index, repeat);
            }
        } catch (Exception e) {
            throw new RuntimeException("updateRepeat json exception");
        }

        track.multi = intent.getBooleanExtra(Track.KEYMULTI,false);
        itemsAdapter.selectedRepeat = index;
        track.clean();
        metronomeData.saveData("updateRepeat "+ track.multi + " S:"+track.items.size(), false);
        itemsAdapter.notifyDataSetChanged();
        frag.setData();
    }

    public void confirmDeleteRepeat(int position) {
        if (track.repeatList.size() == 1) {
            return;
        }
        int index = track.getItemRepeatPosition(position);
        indexDelRepeat = index;
        itemsAdapter.selectedRepeat = indexDelRepeat;

        AlertDialog.Builder builder = new AlertDialog.Builder(frag.getActivity());
        Repeat repeat = track.repeatList.get(indexDelRepeat);
        String sPat = track.patList.get(repeat.patSelected).display(h, repeat.patSelected, true);
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
        track.repeatList.remove(indexDelRepeat);
        if (indexDelRepeat >= track.repeatList.size() - 1) {
            itemsAdapter.selectedRepeat = track.repeatList.size() - 1;
        }

        track.clean();
        itemsAdapter.notifyDataSetChanged();
        metronomeData.saveData("deleteRepeat", false);
        frag.setData();
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
        b.putInt(Keys.EDITSIZE, track.patList.size());

        Pat pat;
        if (add) {
            pat = new Pat(h);
        } else {
            pat = track.patList.get(index);
        }
        b.putString(Track.KEYPATS, pat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackPattern.TAG);
    }

    public void updatePattern(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sPat = intent.getStringExtra(Track.KEYPATS);
        try {
            Pat pat = new Pat(h);
            pat.fromJson(new JSONObject(sPat));
            if (actionAdd) {
                track.patList.add(index, pat);
            } else {
                track.patList.set(index, pat);
            }
        } catch (Exception e) {
            throw new RuntimeException("updatePattern json exception");
        }
        itemsAdapter.selectedPat = index;

        track.clean();
        itemsAdapter.notifyDataSetChanged();
        metronomeData.saveData("updatePattern", false);
        frag.setData();
    }

    public void confirmDeletePattern(int position) {
        if (track.patList.size() == 1) {
            return;
        }
        int index = track.getItemPatPosition(position);
        indexDelPattern = index;
        itemsAdapter.selectedPat = indexDelPattern;
        Pat pat = track.patList.get(indexDelPattern);
        // verwijderen niet mogelijk indien nog in gebruik.!!
        if (pat.checkInUse(metronomeData, h)) {
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
        track.patList.remove(indexDelPattern);
        if (indexDelPattern >= track.patList.size() - 1) {
            itemsAdapter.selectedPat = track.patList.size() - 1;
        }

        track.clean();
        itemsAdapter.notifyDataSetChanged();
        metronomeData.saveData("deletePattern", false);
        frag.setData();
    }

    public void editTap() {
        DialogEditTrackTap dlgEdit = new DialogEditTrackTap();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(frag, Keys.TARGETEDITTAP);

        Bundle b = new Bundle();
        int index = itemsAdapter.selectedRepeat;
        b.putInt(Keys.EDITINDEX, index);
        Repeat repeat = track.repeatList.get(index);
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
        b.putInt(Repeat.KEYTEMPO, track.repeatList.get(track.repeatSelected).tempo);

        dlgEdit.setArguments(b);
        dlgEdit.show(frag.getFragmentManager(), DialogEditTrackStudy.TAG);
    }
}
