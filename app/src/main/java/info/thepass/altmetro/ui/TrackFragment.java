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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackFragment extends Fragment {
    public final static String TAG = "TrakFragment";
    private HelperMetro h;
    private TrackData trackData;
    private Track track;

    private TextView tvTempo;
//    private TextView tvInfo;
    private TextView tvTitle;
    private int maxTempo;

//    private EmphasisViewManager evEditor;
//    private EmphasisViewManager evPlayer;

    private Spinner spBeat;
    private ArrayAdapter<String> mBeatAdapter;
    private AdapterView.OnItemSelectedListener beatListener;

    private Spinner spTime;
    private ArrayAdapter<String> mTimeAdapter;
    private AdapterView.OnItemSelectedListener timeListener;

    private Spinner spSub;
    private ArrayAdapter<String> mSubAdapter;
    private AdapterView.OnItemSelectedListener subListener;

    private SeekBar sbTempo;
    private SeekBar.OnSeekBarChangeListener tempoListener;
    private Button buttonM1;
    private Button buttonM5;
    private Button buttonM20;
    private Button buttonP1;
    private Button buttonP5;
    private Button buttonP20;

    private View layout;
    private Button buttonTitle;
    private TextView tvCurrentBeat;

    private Button buttonPractice;
    private View.OnClickListener practiceListener;

    private Button buttonTempoTap;
    private View.OnClickListener tempoTapListener;

    private Button buttonStudy;
    private View.OnClickListener studyListener;
    private ListView listview;

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
        initListeners();
//        initEmphasis();
        initSpinner();
        initSeekBar();
        initTempo();
        initTitle();
        initIncDec();
        initPractice();
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
            }
        }
    }

    private void initData() {
        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        Log.d(TAG, "initData sel" + trackData.trackSelected);
        track = trackData.tracks.get(trackData.trackSelected);
        getActivity().setTitle(h.getString(R.string.app_name) + " " + track.getTitle(trackData, trackData.trackSelected));
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

        beatListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
//                if (spBeat.getSelectedItemPosition() != getbarBeatsIndex()) {
//                    p.barBeats = Integer.parseInt(spBeat.getSelectedItem()
//                            .toString());
//                    p.initBeatStates();
//                    if (!settingMetroData)
//                        doPatternEdited("beatListener");
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };

        timeListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
//                if (spTime.getSelectedItemPosition() != getbarTimeIndex()) {
//                    p.barTime = Integer.parseInt(spTime.getSelectedItem()
//                            .toString());
//                    p.initBeatStates();
//                    if (!settingMetroData)
//                        doPatternEdited("timeListener");
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };

        subListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
//                if (spSub.getSelectedItemPosition() != h.getSubIndex(p.subs)) {
//                    p.subs = h.subValue[pos];
//                    if (!settingMetroData)
//                        doPatternEdited("subListener");
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
    }

    private void initTempo() {
        tvTempo = (TextView) getActivity().findViewById(R.id.tv_editor_tempo);
    }

    private void initTitle() {
        buttonTitle = (Button) getActivity().findViewById(
                R.id.tv_editor_buttonTitle);
        buttonTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                editBarTitle();
            }
        });

//        tvInfo = (TextView) getActivity()
//                .findViewById(R.id.tv_editor_studyinfo);
        tvTitle = (TextView) getActivity().findViewById(R.id.tv_editor_title);
        tvCurrentBeat = (TextView) getActivity().findViewById(
                R.id.tv_editor_currentbeat);
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
//        switch (p.beatState[currentBeat - 1]) {
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

    private void initPractice() {
        buttonPractice = (Button) getActivity().findViewById(
                R.id.buttonPractice);
        practiceListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                editPractice();
            }
        };
        buttonPractice.setOnClickListener(practiceListener);

        buttonTempoTap = (Button) getActivity().findViewById(
                R.id.buttonTempoTap);
        tempoTapListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                editTempoTap();
            }

        };
        buttonTempoTap.setOnClickListener(tempoTapListener);

        buttonStudy = (Button) getActivity().findViewById(R.id.buttonStudy);
        studyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSpeedStudy();
            }
        };
        buttonStudy.setOnClickListener(studyListener);
    }

    private void initEmphasis() {
//        evEditor = new EmphasisViewManager("ed_editor", h,
//                (MetroActivity) getActivity(), false, layout);
//        evEditor.useLow = true;
//        evPlayer = new EmphasisViewManager("ed_player", h,
//                (MetroActivity) getActivity(), true, layout);
//        evPlayer.useLow = true;
    }

    private void initSpinner() {
        String[] mBeatOpties = {"1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20"};
        mBeatAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, mBeatOpties);
        spBeat = (Spinner) getActivity().findViewById(R.id.spinnerBeat);
        spBeat.setAdapter(mBeatAdapter);
//        spBeat.setSelection(getbarBeatsIndex());
        spBeat.setSelection(0);
        spBeat.setOnItemSelectedListener(beatListener);

        String[] mTimeOpties = {"1", "2", "4", "8"};
        mTimeAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, mTimeOpties);
        spTime = (Spinner) getActivity().findViewById(R.id.spinnerTime);
        spTime.setAdapter(mTimeAdapter);
        spTime.setSelection(0);
//        spTime.setSelection(getbarTimeIndex());
        spTime.setOnItemSelectedListener(timeListener);

        mSubAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, h.subPattern);
        spSub = (Spinner) getActivity().findViewById(R.id.spinnerSub);
        spSub.setAdapter(mSubAdapter);
        spSub.setSelection(0);
        spSub.setOnItemSelectedListener(subListener);

    }

    private void wijzigTempo(int iDelta) {
        String s =tvTempo.getText().toString();
        int newTempo = Integer.parseInt(s) + iDelta;
        newTempo = (newTempo < Keys.MINTEMPO) ? Keys.MINTEMPO : newTempo;
        newTempo = (newTempo >= maxTempo) ? maxTempo : newTempo;
//        displayTempo(newTempo);
//        doPatternEdited("wijzigTempo");
    }

    private void editPractice() {
        h.showToast("under development");
//        Bundle b = new Bundle();
//        b.putInt(Keys.KEYPRACTICE, data.practicePercentage);
//
//        FragmentManager fm = getFragmentManager();
//        DialogPracticeFragment editNameDialog = new DialogPracticeFragment();
//        editNameDialog.h = h;
//        editNameDialog.setArguments(b);
//        editNameDialog.show(fm, DialogPracticeFragment.TAG);
    }

    private void editTempoTap() {
        h.showToast("under development");
//        FragmentManager fm = getFragmentManager();
//        DialogTempoTapFragment editNameDialog = new DialogTempoTapFragment();
//        editNameDialog.h = h;
//        editNameDialog.show(fm, DialogTempoTapFragment.TAG);
    }

    private void editSpeedStudy() {
        h.showToast("under development");
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
