package info.thepass.altmetro.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.TrackItemsAdapter;
import info.thepass.altmetro.data.Order;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.dialog.DialogEditTrackOrder;
import info.thepass.altmetro.dialog.DialogEditTrackPattern;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackFragment extends Fragment {
    public final static String TAG = "TrakFragment";
    private HelperMetro h;
    private TrackData trackData;
    private Track track;

    private ListView lvItems;
    private TrackItemsAdapter itemsAdapter;
    private TextView tvTempo;
    //    private TextView tvInfo;
    private TextView tvTitle;
    private int maxTempo;

//    private EmphasisViewManager evPlayer;

    private SeekBar sbTempo;
    private SeekBar.OnSeekBarChangeListener tempoListener;
    private Button buttonM1;
    private Button buttonM5;
    private Button buttonM20;
    private Button buttonP1;
    private Button buttonP5;
    private Button buttonP20;

    private View layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        h = new HelperMetro(getActivity());
        h.logD(TAG, "activityCreated");
        setHasOptionsMenu(true);
        initData();
        initListView();
        initListeners();
//        initEmphasis();
        initSeekBar();
        initTempo();
        initIncDec();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track_play:
                h.showToast("PLAY under development");
                return true;
            case R.id.action_track_settings:
                h.showToast("settings under development");
                return true;
            case R.id.action_track_tracklist:
                doTrackList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult OK=" + resultCode + " req=" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Keys.TARGETTRACK:
                    initData();
                    return;
                case Keys.TARGETEDITPATTERN:
                    updatePattern(intent);
                    return;
            }
        }
    }

    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        Log.d(TAG, "initData sel" + trackData.trackSelected);
        track = trackData.tracks.get(trackData.trackSelected);
        track.syncItems();
        getActivity().setTitle(h.getString(R.string.app_name) + " " + track.getTitle(trackData, trackData.trackSelected));
    }

    private void initListView() {
        Log.d(TAG, "initListView " + track.items.size());
        itemsAdapter = new TrackItemsAdapter(getActivity(), R.layout.fragment_tracklist_row, track, h, this);

        lvItems = (ListView) getActivity().findViewById(R.id.track_listView);
        lvItems.setAdapter(itemsAdapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (itemsAdapter.getItemViewType(position)) {
                    case TrackItemsAdapter.TYPEPAT:
                        break;
                    case TrackItemsAdapter.TYPEORDER:
                        break;
                    case TrackItemsAdapter.TYPEPATADD:
                        editPattern(0, true);
                        break;
                    case TrackItemsAdapter.TYPEORDERADD:
                        editOrder(0, true);
                        break;
                    default:
                        h.showToast("listview click at position " + position + " id:" + id);
                }
            }
        });
        lvItems.setChoiceMode(ListView.CHOICE_MODE_NONE);
        itemsAdapter.selectedOrder = 0;
        itemsAdapter.selectedPat = 0;
    }

    private void initListeners() {
        tempoListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
//                displayTempo(getProgressTempo(sbTempo.getProgress()));
//                if (!settingMetroData)
//                    doPatternEdited("tempoListener onProgressChanged");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    private void initTempo() {
        tvTempo = (TextView) getActivity().findViewById(R.id.tv_editor_tempo);
    }

    //    public void updateBeat(Bundle b) {
//        tvTempo.setText(String.valueOf(b.getInt(Keys.KEYTEMPO)));
////        tvInfo.setText(b.getString(Keys.KEYBARINFO));
//        currentBeat = b.getInt(Keys.KEYBEAT);
//        h.logD(TAG, "updateBeat " + currentBeat);
//        updateViewCurrentBeat();
//        evPlayer.updateEmphasisView(currentBeat);
//    }
//
//    private void updateViewCurrentBeat() {
//        switch (p.patBeatState[currentBeat - 1]) {
//            case 0:
//                tvCurrentBeat.setBackgroundColor(getResources().getColor(
//                        R.color.color_emphasis_high));
//                break;
//            case 1:
//                tvCurrentBeat.setBackgroundColor(getResources().getColor(
//                        R.color.color_emphasis_low));
//                break;
//            case 2:
//                tvCurrentBeat.setBackgroundColor(getResources().getColor(
//                        R.color.color_emphasis_none));
//                break;
//        }
//        tvCurrentBeat.setText("" + currentBeat);
//    }
//
    private void initSeekBar() {
        sbTempo = (SeekBar) getActivity().findViewById(R.id.sb_tempo);
        sbTempo.setMax(maxTempo - Keys.MINTEMPO);
        sbTempo.setOnSeekBarChangeListener(tempoListener);
    }

    private void initIncDec() {
        buttonM1 = (Button) getActivity().findViewById(R.id.button_m1);
        buttonM1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-1);
            }
        });

        buttonM5 = (Button) getActivity().findViewById(R.id.button_m5);
        buttonM5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-5);
            }
        });
        buttonM20 = (Button) getActivity().findViewById(R.id.button_m20);
        buttonM20.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-20);
            }
        });
        buttonP1 = (Button) getActivity().findViewById(R.id.button_p1);
        buttonP1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(1);
            }
        });
        buttonP5 = (Button) getActivity().findViewById(R.id.button_p5);
        buttonP5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(5);
            }
        });
        buttonP20 = (Button) getActivity().findViewById(R.id.button_p20);
        buttonP20.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(20);
            }
        });
    }

    private void initEmphasis() {
//        evPlayer = new EmphasisViewManager("ed_player", h,
//                (MetroActivity) getActivity(), true, layout);
//        evPlayer.useLow = true;
    }


    private void wijzigTempo(int iDelta) {
        String s = tvTempo.getText().toString();
        int newTempo = Integer.parseInt(s) + iDelta;
        newTempo = (newTempo < Keys.MINTEMPO) ? Keys.MINTEMPO : newTempo;
        newTempo = (newTempo >= maxTempo) ? maxTempo : newTempo;
//        displayTempo(newTempo);
//        doPatternEdited("wijzigTempo");
    }

    public void editPractice() {
        h.showToast("EditPractice under development");
//        Bundle b = new Bundle();
//        b.putInt(Keys.KEYPRACTICE, data.practicePercentage);
//
//        FragmentManager fm = getFragmentManager();
//        DialogPracticeFragment editNameDialog = new DialogPracticeFragment();
//        editNameDialog.h = h;
//        editNameDialog.setArguments(b);
//        editNameDialog.show(fm, DialogPracticeFragment.TAG);
    }

    public void editTempoTap() {
        h.showToast("EditTap under development");
//        FragmentManager fm = getFragmentManager();
//        DialogTempoTapFragment editNameDialog = new DialogTempoTapFragment();
//        editNameDialog.h = h;
//        editNameDialog.show(fm, DialogTempoTapFragment.TAG);
    }

    public void editSpeedStudy() {
        h.showToast("Edit Speed under development");
//        if (!settingMetroData) {
//            Bundle b = new Bundle();
//            b.putBundle(Keys.KEYSPEEDSTUDY, data.sps.toBundle());
//            b.putInt(Keys.KEYTEMPO, data.getBarPatternSelected().tempo);
//
//            FragmentManager fm = getFragmentManager();
//            DialogSpeedStudyFragment editNameDialog = new DialogSpeedStudyFragment();
//            editNameDialog.h = h;
//            editNameDialog.setArguments(b);
//            editNameDialog.show(fm, DialogPracticeFragment.TAG);
//        }
    }

    public void editOrder(int position, boolean add) {
        DialogEditTrackOrder dlgEdit = new DialogEditTrackOrder();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITORDER);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        b.putInt(Keys.EDITPOSITION, itemsAdapter.selectedOrder);
        Order order;
        b.putBoolean(Track.KEYMULTI, track.multi);

        if (add) {
            order = new Order(h);
        } else {
            order = track.orders.get(itemsAdapter.selectedOrder);
        }
        Log.d(TAG, "item " + position + ":" + order.toString());
        b.putString(Track.KEYPATS, order.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackOrder.TAG);
    }

    public void editPattern(final int position, boolean add) {
        DialogEditTrackPattern dlgEdit = new DialogEditTrackPattern();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITPATTERN);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        b.putInt(Keys.EDITPOSITION, position);
        Pat pat;
        if (add) {
            pat = new Pat(h);
        } else {
            pat = track.pats.get(itemsAdapter.selectedPat);
        }
        Log.d(TAG, "item " + position + ":" + pat.toString());
        b.putString(Track.KEYPATS, pat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackPattern.TAG);
    }

    private void updatePattern(Intent intent) {

        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        String sPat = intent.getStringExtra(Track.KEYPATS);
        try {
            Pat pat = new Pat(h);
            pat.fromJson(new JSONObject(sPat));
            Log.d(TAG, "updatePattern " + pat.getPatTitle());
            if (actionAdd) {
                track.pats.add(pat);
            } else {
                track.pats.remove(track.patSelected);
                track.pats.add(track.patSelected, pat);
            }
            track.syncItems();
            itemsAdapter.notifyDataSetChanged();
            trackData.save("updatePattern");
        } catch (Exception e) {
            h.logE(TAG, "updatePattern json exception", e);
        }
        h.showToast("update pattern nog niet af");
    }

    private void doTrackList() {
        TrackListFragment frag = new TrackListFragment();
        frag.setTargetFragment(this, Keys.TARGETTRACK);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
