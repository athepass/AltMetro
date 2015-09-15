package info.thepass.altmetro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.TrackListAdapter;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.dialog.DialogEditTrackInfo;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackListFragment extends ListFragment {
    public final static String TAG = "TrakListFragment";
    private HelperMetro h = null;
    private LinearLayout llList;
    private Button buttonAddItem;
    private int positionDelete;
    private TrackListAdapter trackListAdapter = null;
    private TrackData trackData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tracklist, container,
                false);
        buttonAddItem = (Button) inflater.inflate(
                R.layout.fragment_tracklist_additem, null, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        h = new HelperMetro(getActivity());
        h.logD(TAG, "onActivityCreated");

        initFooter();
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View v,
                                           int position, long id) {
                return true;
            }
        });

        llList = (LinearLayout) getActivity().findViewById(R.id.ll_tracklist);
        buttonAddItem = (Button) getActivity().findViewById(R.id.button_additem_track);

        initData();
        getActivity().setTitle(h.getString(R.string.list_select_track));
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tracklist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tracklist_play:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_tracklist_delete:
                if (trackListAdapter.getCount() > 1) {
                    positionDelete = trackListAdapter.selectedItem;
                    doConfirmDelete();
                }
                return true;
            case R.id.action_tracklist_edit:
                doEdit();
                return true;
            case R.id.action_tracklist_copy:
                h.showToast("copy under development");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        doSetPosition(position, true);
    }

     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG,"onActivityResult OK="+ resultCode+ " req="+requestCode);
            switch (requestCode) {
                case Keys.TARGETTRACKLIST:
                    try {
                        Track track = new Track(h);
                        track.fromJson(new JSONObject(intent.getStringExtra(TrackData.KEYTRACKS)),h);
                        trackData.updateTrack(track);
                        trackListAdapter.notifyDataSetChanged();
                        updateTrackFragment();
                    } catch (Exception e) {
                        h.logE(TAG, "fromJson", e);
                    }
                    return;
            }
        }
    }

    private void doEdit() {
        DialogEditTrackInfo dlgEdit = new DialogEditTrackInfo();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETTRACKLIST);

        Bundle b = new Bundle();
        Track track = trackData.tracks.get(trackListAdapter.selectedItem);
        Log.d(TAG, "item " + trackListAdapter.selectedItem + " i=" + track.toString());
        b.putString(TrackData.KEYTRACKS, track.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackInfo.TAG);
    }

    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        trackListAdapter = new TrackListAdapter(getActivity(),
                R.layout.fragment_tracklist_row, trackData, h);
        setListAdapter(this.trackListAdapter);
        doSetPosition(trackData.trackSelected, false);
    }

    private void doConfirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Track track = trackData.tracks.get(positionDelete);
        String pInfo = "[" + (positionDelete + 1) + "] " + track.getTitle(trackData, positionDelete);
        builder.setMessage(
                h.getString1(R.string.list_confirm_delete_item,
                        h.alfaNum(positionDelete) + ": " + pInfo))
                .setCancelable(false)
                .setPositiveButton(h.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                doDeleteRow();
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

    private void doDeleteRow() {
        trackData.tracks.remove(positionDelete);
        if (trackData.trackSelected>positionDelete) {
            trackData.trackSelected--;
            doSetPosition(trackData.trackSelected, false);
        }
        trackData.save("deleteRow");
        updateTrackFragment();
        trackListAdapter.notifyDataSetChanged();
    }

    private void initFooter() {
        getListView().addFooterView(buttonAddItem);
        OnClickListener addItemClick = new OnClickListener() {
            // @Override
            public void onClick(View v) {
                Track track = new Track(h);
                trackData.tracks.add(track);
                trackData.trackSelected = trackData.tracks.size() - 1;
                trackListAdapter.notifyDataSetChanged();
                h.showToast(h.getString(R.string.list_item_added));
                trackData.save("addTrack");
            }
        };
        buttonAddItem = (Button) getActivity().findViewById(
                R.id.button_additem_track);
        buttonAddItem.setOnClickListener(addItemClick);
    }

    private void doSetPosition(int position, boolean updateData) {
        // werk de user interface bij
        trackListAdapter.selectedItem = position;
        getListView().setItemChecked(position, true);
        trackListAdapter.notifyDataSetChanged();
        if (updateData) {
            trackData.trackSelected = position;
            updateTrackFragment();
        }
    }

    private void updateTrackFragment() {
        trackData.save(TAG);
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETTRACK, Activity.RESULT_OK, intent);
    }
}