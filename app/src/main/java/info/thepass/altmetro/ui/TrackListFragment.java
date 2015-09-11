package info.thepass.altmetro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.TrackListAdapter;
import info.thepass.altmetro.data.MetroData;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackListFragment extends ListFragment {
    public final static String TAG = "TrackListFragment";
    private HelperMetro h = null;
    private LinearLayout llList;
    private Button buttonAddItem;
    private int positionDelete;
    private TrackListAdapter trackAdapter = null;
    private MetroData data;

    private OnTrackListListener mCallback;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTrackListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTrackListListener");
        }
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

        initData();
        getActivity().setTitle(h.getString(R.string.list_select_track));
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
                if (trackAdapter.getCount() > 1) {
                    positionDelete = trackAdapter.selectedItem;
                    doConfirmDelete();
                }
            case R.id.action_tracklist_edit:
                doEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        doSetPosition(position, true);
    }

    private void doSetPosition(int position, boolean updateData) {
        // werk de user interface bij
        trackAdapter.selectedItem = position;
        getListView().setItemChecked(position, true);
        trackAdapter.notifyDataSetChanged();
        if (updateData) {
            mCallback.onTrackSelected(position);
        }
    }

    private void doEdit() {
        h.showToast("under development");
    }

    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.data = act.data;
        trackAdapter = new TrackListAdapter(getActivity(),
                R.layout.fragment_tracklist_row, data.tracks, h);
        setListAdapter(this.trackAdapter);
        doSetPosition(data.trackSelected, false);
    }

    private void doConfirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Track track = data.tracks.get(positionDelete);
        String pInfo = "[" + (positionDelete + 1) + "] " + track.toString();
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
        data.tracks.remove(positionDelete);
        trackAdapter.notifyDataSetChanged();
    }

    private void initFooter() {
        getListView().addFooterView(buttonAddItem);
        OnClickListener addItemClick = new OnClickListener() {
            // @Override
            public void onClick(View v) {
                Track track = new Track(h);
                data.tracks.add(track);
                data.trackSelected = data.tracks.size() - 1;
                trackAdapter.notifyDataSetChanged();
                h.showToast(h.getString(R.string.list_item_added));
                data.save("addTrack", true);
            }
        };
        buttonAddItem = (Button) getActivity().findViewById(
                R.id.button_additem_track);
        buttonAddItem.setOnClickListener(addItemClick);
    }

    public interface OnTrackListListener {
        public void onTrackSelected(int itemSelected);
    }

}