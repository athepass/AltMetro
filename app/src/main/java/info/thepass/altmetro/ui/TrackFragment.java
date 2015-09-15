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
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.dialog.DialogEditTrackRepeat;
import info.thepass.altmetro.dialog.DialogEditTrackPattern;
import info.thepass.altmetro.dialog.DialogEditTrackPractice;
import info.thepass.altmetro.dialog.DialogEditTrackStudy;
import info.thepass.altmetro.dialog.DialogEditTrackTap;
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
                h.showToast("SETTINGS under development");
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
                case Keys.TARGETDELETEPATTERN:
                    deletePattern(intent);
                    return;
                case Keys.TARGETEDITREPEAT:
                case Keys.TARGETEDITTAP:
                    updateRepeat(intent);
                    return;
                case Keys.TARGETDELETEREPEAT:
                    deleteRepeat(intent);
                    return;
                case Keys.TARGETEDITSTUDY:
                case Keys.TARGETEDITPRACTICE:
                    updateStudy(intent);
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
        String s = track.getTitle(trackData, trackData.trackSelected);
        getActivity().setTitle(s.length() == 0 ? h.getString(R.string.app_name) : h.getString(R.string.label_track) + s);
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
                Log.d(TAG, "listclick" + position);
                switch (itemsAdapter.getItemViewType(position)) {
                    case TrackItemsAdapter.TYPEREPEAT:
                        itemsAdapter.selectedRepeat = track.getItemRepeatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
//                        Log.d(TAG, "listclick repeat" + position + " sel" + itemsAdapter.selectedRepeat);
                        break;
                    case TrackItemsAdapter.TYPEREPEATADD:
                        editRepeat(0, true);
                        break;
                    case TrackItemsAdapter.TYPEPAT:
                        itemsAdapter.selectedPat = track.getItemPatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
//                        Log.d(TAG, "list click pat " + position + " sel" + itemsAdapter.selectedPat);
                        break;
                    case TrackItemsAdapter.TYPEPATADD:
                        editPattern(0, true);
                        break;
                    default:
                        h.showToast("listview click at position " + position + " id:" + id);
                }
            }
        });
        lvItems.setChoiceMode(ListView.CHOICE_MODE_NONE);
        itemsAdapter.selectedRepeat = 0;
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
        tvTempo.setText(String.valueOf(track.repeats.get(track.repeatSelected).tempo));
    }

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

    private void doTrackList() {
        TrackListFragment frag = new TrackListFragment();
        frag.setTargetFragment(this, Keys.TARGETTRACK);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void editRepeatView(View v) {
        int pos = lvItems.getPositionForView(v);
        int index = track.getItemRepeatPosition(pos);
        Log.d(TAG, "editRepeatView " + pos + " index=" + index);
        editRepeat(pos, false);
    }

    public void editRepeat(int position, boolean add) {
        DialogEditTrackRepeat dlgEdit = new DialogEditTrackRepeat();
        dlgEdit.h = h;
        dlgEdit.track = this.track;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITREPEAT);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        b.putInt(Keys.EDITPOSITION, position);
        int index = track.getItemRepeatPosition(position);
        b.putInt(Keys.EDITINDEX, index);
        b.putBoolean(Track.KEYMULTI, track.multi);

        Repeat repeat;
        if (add) {
            repeat = new Repeat(h);
        } else {
            repeat = track.repeats.get(index);
        }
        Log.d(TAG, "item " + position + ":" + repeat.toString());
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackRepeat.TAG);
    }

    public void editPatternView(View v) {
        int pos = lvItems.getPositionForView(v);
        int index = track.getItemPatPosition(pos);
        Log.d(TAG, "editPatternView " + pos + " index=" + index);
        editPattern(pos, false);
    }

    public void updateRepeat(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int position = intent.getIntExtra(Keys.EDITPOSITION, -1);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sRepeat = intent.getStringExtra(Track.KEYREPEATS);
        try {
            Repeat repeat = new Repeat(h);
            repeat.fromJson(new JSONObject(sRepeat));
            Log.d(TAG, "updateRepeat " + repeat.toString() + " index=index");
            if (actionAdd) {
                track.repeats.add(repeat);
                itemsAdapter.selectedRepeat = track.repeats.size() - 1;
            } else {
                track.repeats.remove(index);
                track.repeats.add(index, repeat);
                itemsAdapter.selectedRepeat = index;
            }
            track.syncItems();
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            h.logE(TAG, "updateRepeat json exception", e);
            throw new RuntimeException("updateRepeat json exception");
        }
        trackData.save("updatePattern");
        h.dumpToLog(TAG, trackData.toString());
    }

    private void deleteRepeat(Intent intent) {
        h.showToast("delete repeat nog niet af");
    }

    public void editPattern(final int position, boolean add) {
        itemsAdapter.selectedPat = track.getItemPatPosition(position);
        Log.d(TAG, "editPattern pos=" + position + " selPat=" + itemsAdapter.selectedPat);
        DialogEditTrackPattern dlgEdit = new DialogEditTrackPattern();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITPATTERN);

        Bundle b = new Bundle();
        b.putBoolean(Keys.EDITACTION, add);
        b.putInt(Keys.EDITPOSITION, position);
        int index = track.getItemPatPosition(position);
        b.putInt(Keys.EDITINDEX, index);

        Pat pat;
        if (add) {
            pat = new Pat(h);
        } else {
            pat = track.pats.get(index);
        }
        Log.d(TAG, "item " + position + ":" + pat.toString());
        b.putString(Track.KEYPATS, pat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackPattern.TAG);
    }

    private void updatePattern(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int position = intent.getIntExtra(Keys.EDITPOSITION, -1);
        int index = track.getItemPatPosition(position);
        String sPat = intent.getStringExtra(Track.KEYPATS);
        try {
            Pat pat = new Pat(h);
            pat.fromJson(new JSONObject(sPat));
            Log.d(TAG, "updatePattern " + pat.toString());
            if (actionAdd) {
                track.pats.add(pat);
                itemsAdapter.selectedPat = track.pats.size() - 1;
            } else {
                track.pats.remove(index);
                track.pats.add(index, pat);
                itemsAdapter.selectedPat = index;
            }
            track.syncItems();
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            h.logE(TAG, "updatePattern json exception", e);
            throw new RuntimeException("updatePattern json exception");
        }
        trackData.save("updatePattern");
        h.dumpToLog(TAG, trackData.toString());
    }

    private void deletePattern(Intent intent) {
        h.showToast("delete pattern nog niet af");
    }

    public void editTap() {
        DialogEditTrackTap dlgEdit = new DialogEditTrackTap();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITTAP);

        Bundle b = new Bundle();
        int position = itemsAdapter.selectedRepeat;
        b.putInt(Keys.EDITPOSITION, position);
        int index = itemsAdapter.selectedRepeat;
        b.putInt(Keys.EDITINDEX, index);
        Repeat repeat = track.repeats.get(position);
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackTap.TAG);
    }

    public void editPractice() {
        DialogEditTrackPractice dlgEdit = new DialogEditTrackPractice();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITPRACTICE);

        Bundle b = new Bundle();
        Study study = track.study;
        b.putString(Track.KEYSTUDY, study.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackPractice.TAG);
    }

    public void editSpeedStudy() {
        DialogEditTrackStudy dlgEdit = new DialogEditTrackStudy();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITSTUDY);

        Bundle b = new Bundle();
        Study study = track.study;
        b.putString(Track.KEYPATS, study.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackStudy.TAG);
    }

    public void updateStudy(Intent intent) {
        String sStudy = intent.getStringExtra(Track.KEYREPEATS);
        try {
            Study newStudy = new Study();
            newStudy.fromJson(new JSONObject(sStudy));
            Log.d(TAG, "updateRepeat " + newStudy.toString());
            track.study = newStudy;
        } catch (Exception e) {
            h.logE(TAG, "updateStudy json exception", e);
            throw new RuntimeException("updateStudy json exception");
        }
        trackData.save("updateStudy");
        h.dumpToLog(TAG, trackData.toString());
    }

    private void wijzigTempo(int iDelta) {
        String s = tvTempo.getText().toString();
        int newTempo = Integer.parseInt(s) + iDelta;
        newTempo = (newTempo < Keys.MINTEMPO) ? Keys.MINTEMPO : newTempo;
        newTempo = (newTempo >= maxTempo) ? maxTempo : newTempo;
//        displayTempo(newTempo);
//        doPatternEdited("wijzigTempo");
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
}
