package info.thepass.altmetro.aaaUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.TrackListAdapter;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.abDialog.DialogEditTrackInfo;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackListFragment extends ListFragment {
    public final static String TAG = "TrakListFragment";
    private HelperMetro h = null;
    private LinearLayout llList;
    private LinearLayout llAddItem;
    private TrackListAdapter trackListAdapter = null;
    private TrackData trackData;
    private View mainView;
    private int indexDel;

    private ImageButton btn_addadd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_tracklist, container,
                false);
        llAddItem = (LinearLayout) inflater.inflate(
                R.layout.fragment_tracklist_additem, null, false);
        return mainView;
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
        llAddItem = (LinearLayout) getActivity().findViewById(R.id.ll_tracklist_additem_track);

        initData();
        initButtons();
        getActivity().setTitle(h.getString(R.string.list_select_track));
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        this.updateTrackFragment();
        trackData.saveData("tracklist detach", false);
        super.onDetach();
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
            case R.id.action_tracklist_settings:
                doPrefs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        setPosition(position, true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            h.logD(TAG, "onActivityResult OK=" + resultCode + " req=" + requestCode);
            switch (requestCode) {
                case Keys.TARGETEDITTRACK:
                    updateTrackList(intent);
                    return;
            }
        }
    }

    /********************************************************************/
    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        trackListAdapter = new TrackListAdapter(getActivity(),
                R.layout.fragment_tracklist_row, trackData, h);
        trackListAdapter.frag = this;
        trackListAdapter.lv = getListView();
        setListAdapter(this.trackListAdapter);
        setPosition(trackData.trackSelected, false);
    }

    private void initButtons() {
        btn_addadd = (ImageButton) llAddItem.findViewById(R.id.imb_tracklistadd_add);
        btn_addadd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editTrackList(trackData.tracks.size(), true);
            }
        });
    }

    private void initFooter() {
        getListView().addFooterView(llAddItem);
        OnClickListener addItemClick = new OnClickListener() {
            // @Override
            public void onClick(View v) {
                editTrackList(0, true);
            }
        };
        llAddItem = (LinearLayout) getActivity().findViewById(
                R.id.ll_tracklist_additem_track);
//        llAddItem.setOnClickListener(addItemClick);
    }

    /***************************************************************************************/
    private void setPosition(int position, boolean updateData) {
        // werk d1e user interface bij
        trackListAdapter.selectedItem = position;
        trackListAdapter.positionToolbar = position;
        getListView().setItemChecked(position, true);
        trackListAdapter.notifyDataSetChanged();
        if (updateData) {
            trackData.trackSelected = position;
            updateTrackFragment();
        }
    }

    private void updateTrackFragment() {
        trackData.saveData(TAG, false);
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETTRACKFRAGMENT, Activity.RESULT_OK, intent);
    }

    private void doPrefs() {
        PrefsFragment frag = new PrefsFragment();
        frag.setTargetFragment(this, Keys.TARGETPREF);
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /***************************************************************************************/
//    public void editTrackListItem(View v) {
//        int position = getListView().getPositionForView(v);
//        setPosition(position, false);
//        editTrackList(position, false);
//    }
//
    public void editTrackList(int position, boolean add) {

        DialogEditTrackInfo dlgEdit = new DialogEditTrackInfo();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITTRACK);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        b.putInt(Keys.EDITINDEX, position);
        b.putInt(Keys.EDITSIZE, trackData.tracks.size());
        Track track;
        if (add) {
            track = new Track(h, trackData);
        } else {
            track = trackData.tracks.get(position);
        }
        String sTrack = track.toJson().toString();
        b.putString(TrackData.KEYTRACKS, sTrack);

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackInfo.TAG);
    }

    private void updateTrackList(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sTrack = intent.getStringExtra(TrackData.KEYTRACKS);
        Track track = new Track(h, trackData);

        try {
            if (actionAdd) {
                trackData.tracks.add(track);
                setPosition(trackData.tracks.size() - 1, true);
            } else {
                track.fromJson(new JSONObject(sTrack), h);
                trackData.tracks.set(index, track);
                setPosition(index, true);
            }
        } catch (Exception e) {
            throw new RuntimeException("updateTrackList json exception");
        }

        trackData.clean();
        getListView().setItemChecked(trackListAdapter.selectedItem, true);
        trackListAdapter.notifyDataSetChanged();

        trackData.saveData("updateTrackList", false);
    }

    /***************************************************************************************/
    public void confirmDeleteItem(int index) {
        if (trackData.tracks.size() == 1) {
            return;
        }
        indexDel = index;
        trackListAdapter.selectedItem = indexDel;
        getListView().setItemChecked(trackListAdapter.selectedItem, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Track track = trackData.tracks.get(indexDel);
        String pInfo = track.display(h, indexDel);
        builder.setMessage(h.getString(R.string.list_confirm_delete_item) + " " + pInfo)
                .setCancelable(false)
                .setPositiveButton(h.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteItem();
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

    private void deleteItem() {
        trackData.tracks.remove(indexDel);
        if (indexDel >= trackData.tracks.size() - 1) {
            setPosition(trackData.tracks.size() - 1, true);
        }
        trackData.saveData("deleteRow", false);
        updateTrackFragment();
        trackListAdapter.notifyDataSetChanged();
    }
}