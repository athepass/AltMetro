package info.thepass.altmetro.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONObject;

import info.thepass.altmetro.Audio.BeatManager;
import info.thepass.altmetro.Audio.SoundFragment;
import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.ItemsListViewManager;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackFragment extends Fragment {
    public final static String TAG = "TrakFragment";
    public TrackData trackData;
    public Track track;
    // views Study
    public TextView tvTap;
    public TextView tv_study;
    public RadioGroup rg_practice;
    public RadioButton rb_prac50;
    public RadioButton rb_prac70;
    public RadioButton rb_prac80;
    public RadioButton rb_prac90;
    public RadioButton rb_prac95;
    public RadioButton rb_prac100;
    private HelperMetro h;
    private EmphasisViewManager evPlayer;
    public ItemsListViewManager lvManager;
    private LayoutInflater myInflater;
    private View layout;
    private SoundFragment soundFragment;
    // views tempo
    private int maxTempo;
    private TextView tvTempo;
    private TextView tvTempoPractice;
    private int tempoTV;
    private SeekBar sbTempo;
    private SeekBar.OnSeekBarChangeListener tempoListener;
    private Button buttonM1;
    private Button buttonM5;
    private Button buttonM20;
    private Button buttonP1;
    private Button buttonP5;
    private Button buttonP20;

    private boolean isPlaying;
    private BeatManager bm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myInflater = inflater;
        layout = inflater.inflate(R.layout.fragment_track, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        h = new HelperMetro(getActivity());
        h.logD(TAG, "activityCreated");
        h.initToastAlert(myInflater);
        setHasOptionsMenu(true);
        initData();
        initItemsListViewManager();
        lvManager.initListView();
        initListeners();
        initEmphasis();
        initSeekBar();
        initTempo();
        initIncDec();
        initStudy();
        initSoundFragment();
        bm = new BeatManager(h);
        setData();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track_play:
                doPlay(trackData.trackSelected);
                return true;
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
        h.logD(TAG, "onActivityResult OK=" + resultCode + " req=" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Keys.TARGETTRACKFRAGMENT:
                    setData();
                    return;
                case Keys.TARGETEDITPATTERN:
                    lvManager.updatePattern(intent);
                    return;
                case Keys.TARGETEDITREPEAT:
                case Keys.TARGETEDITTAP:
                    lvManager.updateRepeat(intent);
                    return;
                case Keys.TARGETEDITSTUDY:
                    setStudy(intent);
                    return;
                case Keys.TARGETPREF:
                    lvManager.itemsAdapter.notifyDataSetChanged();
                    return;
            }
        }
    }

    private void initData() {
        maxTempo = Integer.parseInt(h.prefs.getString(Keys.PREFMAXTEMPO, "400"));

        ActivityTrack act = (ActivityTrack) getActivity();
        this.trackData = act.trackData;
        track = trackData.tracks.get(trackData.trackSelected);
        isPlaying = false;
    }

    private void initItemsListViewManager() {
        lvManager = new ItemsListViewManager();
        lvManager.frag = this;
        lvManager.h = this.h;
        lvManager.trackData = this.trackData;
        lvManager.track = this.track;
    }

    private void initListeners() {
        tempoListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setTempo(getProgressTempo(sbTempo.getProgress()));
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
        tvTempoPractice = (TextView) getActivity().findViewById(R.id.tv_editor_tempopractice);
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
                changeTempo(-1);
            }
        });

        buttonM5 = (Button) getActivity().findViewById(R.id.btn_track_m5);
        buttonM5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeTempo(-5);
            }
        });
        buttonM20 = (Button) getActivity().findViewById(R.id.btn_track_m20);
        buttonM20.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeTempo(-20);
            }
        });
        buttonP1 = (Button) getActivity().findViewById(R.id.btn_track_p1);
        buttonP1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeTempo(1);
            }
        });
        buttonP5 = (Button) getActivity().findViewById(R.id.btn_track_p5);
        buttonP5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeTempo(5);
            }
        });
        buttonP20 = (Button) getActivity().findViewById(R.id.btn_track_p20);
        buttonP20.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeTempo(20);
            }
        });
    }

    private void initStudy() {
        tvTap = (TextView) getActivity().findViewById(R.id.tv_track_tap);
        tvTap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lvManager.editTap();
            }
        });
        tv_study = (TextView) getActivity().findViewById(R.id.tv_track_study_study);
        tv_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvManager.editSpeedStudy();
            }
        });

        rg_practice = (RadioGroup) getActivity().findViewById(R.id.rg_track_practice);
        rg_practice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int newPractice = 0;
                switch (checkedId) {
                    case R.id.rb_track_prac50:
                        newPractice = 50;
                        break;
                    case R.id.rb_track_prac70:
                        newPractice = 70;
                        break;
                    case R.id.rb_track_prac80:
                        newPractice = 80;
                        break;
                    case R.id.rb_track_prac90:
                        newPractice = 90;
                        break;
                    case R.id.rb_track_prac95:
                        newPractice = 95;
                        break;
                    case R.id.rb_track_prac100:
                        newPractice = 100;
                        break;
                }
                track.study.practice = newPractice;
                trackData.saveData("Practice changed", false);
                setData();
            }
        });

        switch (track.study.practice) {
            case 50:
                rb_prac50 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac50);
                rb_prac50.setChecked(true);
                break;
            case 70:
                rb_prac70 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac70);
                rb_prac70.setChecked(true);
                break;
            case 80:
                rb_prac80 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac80);
                rb_prac80.setChecked(true);
                break;
            case 90:
                rb_prac90 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac90);
                rb_prac90.setChecked(true);
                break;
            case 95:
                rb_prac95 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac95);
                rb_prac95.setChecked(true);
                break;
            case 100:
                rb_prac100 = (RadioButton) getActivity().findViewById(R.id.rb_track_prac100);
                rb_prac100.setChecked(true);
                break;
        }
    }

    private void initEmphasis() {
        evPlayer = new EmphasisViewManager("player", Keys.EVMPLAYER, layout, h);
        evPlayer.useLow = true;
    }

    private void initSoundFragment() {
        soundFragment = (SoundFragment) getFragmentManager()
                .findFragmentByTag(SoundFragment.TAG);
        if (soundFragment == null) {
            soundFragment = new SoundFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager()
                    .beginTransaction();
            fragmentTransaction.add(soundFragment, SoundFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    public void setData() {
        track = trackData.tracks.get(trackData.trackSelected);

        String s = track.getTitle(trackData, trackData.trackSelected);
        getActivity().setTitle(s.length() == 0 ? h.getString(R.string.app_name) : h.getString(R.string.label_track) + s);

        lvManager.itemsAdapter.notifyDataSetChanged();
        tvTempo.setText(String.valueOf(track.repeats.get(track.repeatSelected).tempo));

        setRepeat(track.repeatSelected);
        setStudy(null);
        evPlayer.setEmphasisVisible(isPlaying);
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
        // toggle
        isPlaying = !isPlaying;
        evPlayer.setEmphasisVisible(isPlaying);
        lvManager.itemsListView.setVisibility((isPlaying) ? View.GONE : View.VISIBLE);
        tv_study.setVisibility((isPlaying) ? View.GONE : View.VISIBLE);
        tvTap.setVisibility((isPlaying) ? View.INVISIBLE : View.VISIBLE);

        if (isPlaying) {
            bm.trackData = trackData;
            bm.track = track;
            bm.llRoot = lvManager.llRoot;
            bm.startPlayer();
        } else {
            bm.stopPlayer();
        }
    }

    public void setStudy(Intent intent) {
        if (intent != null) {
            String sStudy = intent.getStringExtra(Track.KEYSTUDY);
            try {
                Study newStudy = new Study();
                newStudy.fromJson(new JSONObject(sStudy));
                track.study = newStudy;
            } catch (Exception e) {
                throw new RuntimeException("setStudy json exception");
            }
            trackData.saveData("setStudy", false);
            setData();
        }
        // study textview onzichtbaar i.g.v. single. Gebruik anders preference
        boolean showStudy = (track.multi) ? false : h.prefs.getBoolean(Keys.PREFSHOWSTUDY, true);
        tv_study.setVisibility((showStudy) ? View.VISIBLE : View.GONE);
        tv_study.setText(track.study.display(h));

        boolean showPractice = h.prefs.getBoolean(Keys.PREFSHOWPRACTICE, true);
        if (!showPractice) {
            track.study.practice = 100;
            rg_practice.setVisibility(View.GONE);
        } else {
            rg_practice.setVisibility((track.study.used || (!showPractice)) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public void setRepeat(int index) {
        Repeat repeat = track.repeats.get(index);
        tempoTV = repeat.tempo;
        changeTempo(0);
        Pat pat = trackData.pats.get(repeat.indexPattern);
        evPlayer.data = trackData;
        evPlayer.setPattern(pat, isPlaying);
    }

    private void setTempo(int newTempo) {
        tempoTV = h.validatedTempo(newTempo);
        track.setTempo(tempoTV);
        displayTempo();
    }
    private void changeTempo(int iDelta) {
        setTempo(tempoTV + iDelta);
    }
    
    private void displayTempo() {
        tvTempo.setText("" + tempoTV);
        int tempoPractice = h.validatedTempo(tempoTV * track.study.practice / 100);
        tvTempoPractice.setText("" + tempoPractice);

        int indexSB = getProgressIndex(tempoTV);
        if (indexSB != sbTempo.getProgress()) {
            sbTempo.setProgress(indexSB);
        }
    }

    private int getProgressTempo(int tempoIndex) {
        return tempoIndex + Keys.MINTEMPO;
    }

    private int getProgressIndex(int tempo) {
        return tempo - Keys.MINTEMPO;
    }
}
