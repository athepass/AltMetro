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
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.adapter.ItemsListViewManager;
import info.thepass.altmetro.data.MetronomeData;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.player.BarManager;
import info.thepass.altmetro.player.PlayerData;
import info.thepass.altmetro.player.PlayerView;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackFragment extends Fragment {
    public final static String TAG = "TrakFragment";
    public MetronomeData metronomeData;
    public boolean starting = true;
    // Listview
    public ItemsListViewManager lvManager;
    // views Study
    public TextView tvTap;
    public TextView tvStudy;
    public RadioGroup rg_practice;
    public RadioButton rb_prac50;
    public RadioButton rb_prac70;
    public RadioButton rb_prac80;
    public RadioButton rb_prac90;
    public RadioButton rb_prac95;
    public RadioButton rb_prac100;
    public TextView tvInfo;
    public View layout;
    private HelperMetro h;
    private LayoutInflater myInflater;
    private MenuItem menuItemStart;
    private MenuItem menuItemStop;
    private MenuItem menuItemSettings;
    private MenuItem menuItemTrackList;
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
    private BarManager bm;

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


        starting = true;

        initData();

        initBeatManager();

        initItemsListViewManager();
        lvManager.initListView();

        initListeners();
        initSeekBar();
        initTempo();
        initIncDec();
        initStudy();

        initViews();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);
        menuItemStart = menu.findItem(R.id.action_track_start);
        menuItemStop = menu.findItem(R.id.action_track_stop);
        menuItemSettings = menu.findItem(R.id.action_track_settings);
        menuItemTrackList = menu.findItem(R.id.action_track_tracklist);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        h.logD(TAG, "onPrepareOptions " + bm.isPlaying());
        menuItemStart.setVisible(!bm.isPlaying());
        menuItemStop.setVisible(bm.isPlaying());
        if (bm.isPlaying()) {
            menuItemSettings.setIcon(R.mipmap.ic_none);
            menuItemTrackList.setIcon(R.mipmap.ic_none);
        } else {
            menuItemSettings.setIcon(R.mipmap.ic_action_settings);
            menuItemTrackList.setIcon(R.mipmap.icon_list);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_track_start:
                doStartPlayer();
                return true;
            case R.id.action_track_stop:
                doStopPlayer();
                return true;
            case R.id.action_track_settings:
                if (!bm.isPlaying()) {
                    doPrefs();
                }
                return true;
            case R.id.action_track_tracklist:
                if (!bm.isPlaying()) {
                    doTrackList();
                }
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
                    doBuild();
                    return;
                case Keys.TARGETEDITREPEAT:
                case Keys.TARGETEDITTAP:
                    lvManager.updateRepeat(intent);
                    doBuild();
                    return;
                case Keys.TARGETEDITSTUDY:
                    setStudy(intent);
                    doBuild();
                    return;
                case Keys.TARGETPREF:
                    doPref();
                    return;
                case Keys.TARGETBEATMANAGERINIT:
                    h.logD(TAG, "beat manager init ready, setting data");
                    starting = false;
//                    bm.playerView.initSurfaceHolder();
                    setData();
                    return;
                case Keys.TARGETBEATMANAGERSTOP:
                    doStopPlayer();
                    return;
                default:
                    throw new RuntimeException("Requestcode unknown "
                            + requestCode);
            }
        }
    }

    public void doStartPlayer() {
        if (!bm.pd.bmTrack.trackPlayable(h)) {
            return;
        }

        if (bm.pd.building) {
            String msg = h.getString(R.string.error_building);
            h.logD(TAG, msg);
            h.showToast(msg);
            return;
        }

        h.logD(TAG, "doStart " + bm.isPlaying());
//        dumpThread();
        if (!bm.isPlaying()) {
            bm.startPlayer();
        }
    }

    public void doStopPlayer() {
        h.logD(TAG, "doStop " + bm.isPlaying());
        if (bm.isPlaying()) {
            bm.stopPlayer();
        }
        updateLayout();
    }

    private void initData() {
        maxTempo = Integer.parseInt(h.prefs.getString(Keys.PREFMAXTEMPO,
                Keys.MAXTEMPODEFAULT));

        ActivityTrack act = (ActivityTrack) getActivity();
        this.metronomeData = act.metronomeData;
        Log.d(TAG, "TRACK:" + metronomeData.trackSelected);
//        bm.pd.bmTrack = metronomeData.tracks.get(metronomeData.trackSelected);
    }

    private void initItemsListViewManager() {
        lvManager = new ItemsListViewManager();
        lvManager.frag = this;
        lvManager.h = this.h;
        lvManager.metronomeData = this.metronomeData;
        lvManager.track = bm.pd.bmTrack;
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
        tvStudy = (TextView) getActivity().findViewById(R.id.tv_track_study_study);
        tvStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvManager.editSpeedStudy();
            }
        });

        rg_practice = (RadioGroup) getActivity().findViewById(R.id.rg_track_practice);
        rg_practice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int newPractice;
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
                    default:
                        throw new RuntimeException("invalid checkedid"
                                + checkedId);
                }
                bm.pd.bmTrack.study.practice = newPractice;
                metronomeData.saveData("Practice changed", false);
                setData();
            }
        });

        switch (bm.pd.bmTrack.study.practice) {
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
            default:
                throw new RuntimeException("invalid percentage"
                        + bm.pd.bmTrack.study.practice);
        }
    }

    private void initViews() {
        tvInfo = (TextView) getActivity().findViewById(R.id.tv_track_info);
    }

    private void initBeatManager() {
        bm = (BarManager) getFragmentManager()
                .findFragmentByTag(BarManager.TAG);
        if (bm == null) {
            bm = new BarManager();
            FragmentTransaction fragmentTransaction = getFragmentManager()
                    .beginTransaction();
            fragmentTransaction.add(bm, BarManager.TAG);
            fragmentTransaction.commit();
        }
        bm.setTargetFragment(this, Keys.TARGETBEATMANAGERSTOP);
        bm.trackFragment = this;
        bm.pd = new PlayerData(h);
        bm.pd.bmTrack = metronomeData.tracks.get(metronomeData.trackSelected);

        h.logD(TAG, "init playerview");
        bm.playerView = (PlayerView) getActivity().findViewById(R.id.playerview);
        bm.playerView.h = h;
        bm.playerView.bm = bm;
        bm.sh = bm.playerView.getHolder();
        bm.sh.addCallback(bm.playerView);
    }

    public void setData() {
        if (starting) {
            return;
        }

        bm.pd.playStatus = Keys.PLAYSTOP;

        bm.pd.bmTrack = metronomeData.tracks.get(metronomeData.trackSelected);
        setTitle();

        lvManager.itemsAdapter.notifyDataSetChanged();
        tvTempo.setText(String.valueOf(bm.pd.bmTrack.repeatList.get(
                bm.pd.bmTrack.repeatSelected).tempo));

        setRepeat(bm.pd.bmTrack.repeatSelected);
        setStudy(null);

        updateLayout();
        doBuild();
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

    private void doBuild() {
        if (bm.pd.bmTrack.trackPlayable(h)) {
            bm.buildBeat(bm.pd.bmTrack);
        }
    }

    public void setInfo(String info) {
        tvInfo.setText(info);
    }

    public void updateLayout() {
        Log.d(TAG, "updateLayout");
        setTitle();

//        bm.evmPlayer.setEmphasisVisible(bm.pd.mPlaying);

        if (bm.isPlaying()) {
            tvStudy.setVisibility(View.GONE);
        } else {
            boolean showStudy = (bm.pd.bmTrack.multi)
                    ? false : h.prefs.getBoolean(Keys.PREFSHOWSTUDY, true);
            tvStudy.setVisibility((showStudy) ? View.VISIBLE : View.GONE);
        }
        tvInfo.setVisibility((!bm.isPlaying()) ? View.INVISIBLE : View.VISIBLE);
        tvStudy.setVisibility(View.GONE);
        tvTap.setVisibility((bm.isPlaying()) ? View.INVISIBLE : View.VISIBLE);
        lvManager.itemsListView.setVisibility((bm.isPlaying())
                ? View.GONE : View.VISIBLE);

        getActivity().invalidateOptionsMenu();
    }

    public void setStudy(Intent intent) {
        if (intent != null) {
            String sStudy = intent.getStringExtra(Track.KEYSTUDY);
            try {
                Study newStudy = new Study();
                newStudy.fromJson(new JSONObject(sStudy));
                bm.pd.bmTrack.study = newStudy;
            } catch (Exception e) {
                throw new RuntimeException("setStudy json exception");
            }
            metronomeData.saveData("setStudy", false);
            setData();
        }
        // study textview onzichtbaar i.g.v. multi. Gebruik anders preference
        boolean showStudy = (bm.pd.bmTrack.multi)
                ? false : h.prefs.getBoolean(Keys.PREFSHOWSTUDY, true);
        tvStudy.setVisibility((showStudy) ? View.VISIBLE : View.GONE);
        tvStudy.setText(bm.pd.bmTrack.study.display(h));

        boolean showPractice = h.prefs.getBoolean(Keys.PREFSHOWPRACTICE, true);
        if (!showPractice) {
            bm.pd.bmTrack.study.practice = 100;
            rg_practice.setVisibility(View.GONE);
        } else {
            if (bm.pd.bmTrack.study.used) {
                bm.pd.bmTrack.study.practice = 100;
                rg_practice.setVisibility(View.INVISIBLE);
            } else {
                rg_practice.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setRepeat(int index) {
        Repeat repeat = bm.pd.bmTrack.repeatList.get(index);
        tempoTV = repeat.tempo;
        changeTempo(0);
        Pat pat = bm.pd.bmTrack.patList.get(repeat.patSelected);
    }

    private void setTempo(int newTempo) {
        tempoTV = h.validatedTempo(newTempo);
        bm.pd.bmTrack.setTempo(tempoTV);
        displayTempo();
    }

    private void changeTempo(int iDelta) {
        setTempo(tempoTV + iDelta);
    }

    private void displayTempo() {
        tvTempo.setText("" + tempoTV);
        int tempoPractice = h.validatedTempo(
                Math.round(tempoTV * bm.pd.bmTrack.study.practice / 100f));
        tvTempoPractice.setText("" + tempoPractice);

        int indexSB = getProgressIndex(tempoTV);
        if (indexSB != sbTempo.getProgress()) {
            sbTempo.setProgress(indexSB);
        }
    }

    private void setTitle() {
        String sPlay = "";
        sPlay = (bm.isPlaying()) ? " (P)" : "";
        String sTrack = bm.pd.bmTrack.getTitle(
                metronomeData, metronomeData.trackSelected);
        getActivity().setTitle((sTrack.length() == 0
                ? h.getString(R.string.app_name) : h.getString(R.string.label_track) + sTrack) + sPlay);
    }

    private int getProgressTempo(int tempoIndex) {
        return tempoIndex + Keys.MINTEMPO;
    }

    private int getProgressIndex(int tempo) {
        return tempo - Keys.MINTEMPO;
    }

    private void doPref() {
        h.logD(TAG, "doPref");
        metronomeData.saveData("pref", false);
        ActivityTrack act = (ActivityTrack) getActivity();
        act.doRestart();
    }
}
