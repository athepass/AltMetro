package info.thepass.altmetro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.TrackItemsAdapter;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.dialog.DialogEditTrackPattern;
import info.thepass.altmetro.dialog.DialogEditTrackRepeat;
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
    private int indexDelRepeat;
    private int indexDelPattern;
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
        initEmphasis();
        initSeekBar();
        initTempo();
        initIncDec();
        setData();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track_settings:
                doPrefs();
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
                case Keys.TARGETTRACKFRAGMENT:
                    setData();
                    return;
                case Keys.TARGETEDITPATTERN:
                    updatePattern(intent);
                    return;
                case Keys.TARGETEDITREPEAT:
                case Keys.TARGETEDITTAP:
                    updateRepeat(intent);
                    return;
                case Keys.TARGETEDITSTUDY:
                    updateStudy(intent);
                    return;
                case Keys.TARGETPREF:
                    itemsAdapter.notifyDataSetChanged();
                    return;
            }
        }
    }

    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        track = trackData.tracks.get(trackData.trackSelected);
    }

    private void initListView() {

        lvItems = (ListView) getActivity().findViewById(R.id.track_listView);
        itemsAdapter = new TrackItemsAdapter(getActivity(), R.layout.fragment_tracklist_row, lvItems,
                track, trackData, h, this);
        lvItems.setAdapter(itemsAdapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (itemsAdapter.getItemViewType(position)) {
                    case TrackItemsAdapter.ROWTYPEREPEAT:
                        itemsAdapter.selectedRepeat = track.getItemRepeatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEREPEATADD:
                        editRepeat(0, true);
                        break;
                    case TrackItemsAdapter.ROWTYPEPAT:
                        itemsAdapter.selectedPat = track.getItemPatPosition(position);
                        itemsAdapter.notifyDataSetChanged();
                        break;
                    case TrackItemsAdapter.ROWTYPEPATADD:
                        editPattern(0, true);
                        break;
                    default:
                        throw new RuntimeException("listview click at position " + position);
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
    }

    private void initSeekBar() {
        sbTempo = (SeekBar) getActivity().findViewById(R.id.sb_tempo);
        sbTempo.setMax(maxTempo - Keys.MINTEMPO);
        sbTempo.setOnSeekBarChangeListener(tempoListener);
    }

    private void initIncDec() {
        buttonM1 = (Button) getActivity().findViewById(R.id.btn_track_m1);
        buttonM1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-1);
            }
        });

        buttonM5 = (Button) getActivity().findViewById(R.id.btn_track_m5);
        buttonM5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-5);
            }
        });
        buttonM20 = (Button) getActivity().findViewById(R.id.btn_track_m20);
        buttonM20.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-20);
            }
        });
        buttonP1 = (Button) getActivity().findViewById(R.id.btn_track_p1);
        buttonP1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(1);
            }
        });
        buttonP5 = (Button) getActivity().findViewById(R.id.btn_track_p5);
        buttonP5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(5);
            }
        });
        buttonP20 = (Button) getActivity().findViewById(R.id.btn_track_p20);
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

    private void setData() {
        track = trackData.tracks.get(trackData.trackSelected);

        track.syncItems(trackData.pats);
        String s = track.getTitle(trackData, trackData.trackSelected);
        getActivity().setTitle(s.length() == 0 ? h.getString(R.string.app_name) : h.getString(R.string.label_track) + s);

        itemsAdapter.notifyDataSetChanged();
        tvTempo.setText(String.valueOf(track.repeats.get(track.repeatSelected).tempo));
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

    private void doTrackList() {
        TrackListFragment frag = new TrackListFragment();
        frag.setTargetFragment(this, Keys.TARGETTRACKFRAGMENT);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void doPlay(int position) {
        h.showToast("PLAY under development");
    }
    public void editRepeat(int position, boolean add) {
        DialogEditTrackRepeat dlgEdit = new DialogEditTrackRepeat();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITREPEAT);

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
            repeat = new Repeat(h);
        } else {
            repeat = track.repeats.get(index);
        }
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackRepeat.TAG);
    }

    public void updateRepeat(Intent intent) {
        boolean actionAdd = intent.getBooleanExtra(Keys.EDITACTION, false);
        int index = intent.getIntExtra(Keys.EDITINDEX, -1);
        String sRepeat = intent.getStringExtra(Track.KEYREPEATS);
        try {
            Repeat repeat = new Repeat(h);
            repeat.fromJson(new JSONObject(sRepeat));
            if (actionAdd) {
                Log.d(TAG, "update repeat add index=" + index);
                track.repeats.add(index, repeat);
            } else {
                track.repeats.set(index, repeat);
            }
            itemsAdapter.selectedRepeat = index;
            track.syncItems(trackData.pats);
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            h.logE(TAG, "updateRepeat json exception", e);
            throw new RuntimeException("updateRepeat json exception");
        }
        trackData.saveData("updatePattern", false);
        itemsAdapter.notifyDataSetChanged();
    }

    public void confirmDeleteRepeat(int position) {
        if (track.repeats.size() == 1) {
            return;
        }
        int index = track.getItemRepeatPosition(position);
        Log.d(TAG, "confirmDeleteRepeat " + index);
        indexDelRepeat = index;
        itemsAdapter.selectedRepeat = indexDelRepeat;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Repeat repeat = track.repeats.get(indexDelRepeat);
        String sPat = trackData.pats.get(repeat.indexPattern).display(h, repeat.indexPattern, true);
        String pInfo = repeat.display(h, index, sPat);
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
        itemsAdapter.notifyDataSetChanged();
    }

    public void editPattern(final int position, boolean add) {
        itemsAdapter.selectedPat = track.getItemPatPosition(position);
        DialogEditTrackPattern dlgEdit = new DialogEditTrackPattern();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITPATTERN);

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
        dlgEdit.show(getFragmentManager(), DialogEditTrackPattern.TAG);
    }

    private void updatePattern(Intent intent) {
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
            track.syncItems(trackData.pats);
            itemsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            h.logE(TAG, "updatePattern json exception", e);
            throw new RuntimeException("updatePattern json exception");
        }
        trackData.saveData("updatePattern", false);
        itemsAdapter.notifyDataSetChanged();
    }

    public void confirmDeletePattern(int position) {
        if (trackData.pats.size() == 1) {
            return;
        }
        int index = track.getItemPatPosition(position);
        indexDelPattern = index;
        itemsAdapter.selectedPat = indexDelPattern;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Pat pat = trackData.pats.get(indexDelPattern);
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
        itemsAdapter.notifyDataSetChanged();
    }


    public void editTap() {
        DialogEditTrackTap dlgEdit = new DialogEditTrackTap();
        dlgEdit.h = h;
        dlgEdit.setTargetFragment(this, Keys.TARGETEDITTAP);

        Bundle b = new Bundle();
        int position = itemsAdapter.selectedRepeat;
        int index = itemsAdapter.selectedRepeat;
        b.putInt(Keys.EDITINDEX, index);
        Repeat repeat = track.repeats.get(position);
        b.putString(Track.KEYREPEATS, repeat.toJson().toString());

        dlgEdit.setArguments(b);
        dlgEdit.show(getFragmentManager(), DialogEditTrackTap.TAG);
    }

    public void editSpeedStudy() {
//        DialogEditTrackStudy dlgEdit = new DialogEditTrackStudy();
//        dlgEdit.h = h;
//        dlgEdit.setTargetFragment(this, Keys.TARGETEDITSTUDY);
//
//        Bundle b = new Bundle();
//        Study study = track.study;
//        b.putString(TrackData.KEYPATS, study.toJson().toString());
//
//        dlgEdit.setArguments(b);
//        dlgEdit.show(getFragmentManager(), DialogEditTrackStudy.TAG);
        track.study.used = !track.study.used;
        itemsAdapter.notifyDataSetChanged();
    }

    public void updateStudy(Intent intent) {
        String sStudy = intent.getStringExtra(Track.KEYREPEATS);
        try {
            Study newStudy = new Study();
            newStudy.fromJson(new JSONObject(sStudy));
            track.study = newStudy;
        } catch (Exception e) {
            throw new RuntimeException("updateStudy json exception");
        }
        trackData.saveData("updateStudy", false);
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
