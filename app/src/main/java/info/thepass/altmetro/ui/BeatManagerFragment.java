package info.thepass.altmetro.ui;

import android.app.Fragment;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;

import info.thepass.altmetro.Audio.Beat;
import info.thepass.altmetro.Audio.SoundCollection;
import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;

public class BeatManagerFragment extends Fragment {
    public final static String TAG = "BeatManagerFragment";
    public final static int DOORGAANSTART = 2;
    public final static int DOORGAANSTOP = 0;
    public ArrayList<Beat> beatList;
    public int barCounter;
    public Track track;
    public TrackData trackData;
    public LinearLayout llRoot;
    String[] subs;
    private HelperMetro h;
    private AudioTrack audioTrack;
    private SoundCollection sc;
    private MetronomeAsyncTask metroTask;
    private Handler mHandler;
    private int doorgaan;
    private int iBeatSound;
    private int iBeatUI;
    /*****************************************************************/
    Runnable runUI = new Runnable() {
        public void run() {
            int uiDelay = 200;
            if (doorgaan>DOORGAANSTOP ) {
                Log.d(TAG, "beatUI " + iBeatUI + " info:" + beatList.get(iBeatUI).display(iBeatUI, subs));
                iBeatUI+=beatList.get(iBeatUI).barNext;
                if (iBeatUI < beatList.size()) {
                    // TODO bereken doorlooptijd synced
                    // TODO update user interface
                    mHandler.postDelayed(runUI, uiDelay);
                } else {
                    Log.d(TAG,"beatUI ready");
                    doorgaan--;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fragment zonder UI
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        h = new HelperMetro(getActivity());
        subs = h.getStringArray(R.array.sub_pattern);
        beatList = new ArrayList<Beat>();
        mHandler = new Handler();
        initSound();
    }

    public void build(Track track) {
        beatList.clear();
        barCounter = 0;
        if (track.trackPlayable(h)) {
            track.buildBeatList(this);
        }
//        dumpBeatList(-1);
    }

    public void dumpBeatList(int rondom) {
        String s = "====== Dump beats =====";
        int int0 = (rondom < 0) ? 0 : (rondom > 0) ? rondom - 1 : 0;
        int int1 = (rondom < 0) ? beatList.size() :
                (rondom < beatList.size()) ? rondom + 1 : beatList.size();
        for (int i = int0; i < int1; i++) {
            s += "\n" + beatList.get(i).display(i, subs)
                    + "\n" + beatList.get(i).displayBeat();
        }
        h.logD(TAG, s);
    }

    public void startPlayer() {
        Log.d(TAG, "startPlayer");
        build(track);
        for (int i = 0; i < beatList.size(); i++) {
            beatList.get(i).buildSound();
            dumpBeatList(i);
        }
        doorgaan = DOORGAANSTART;
        metroTask.execute();
    }

    public void stopPlayer() {
        Log.d(TAG,"stopPlayer");
        doorgaan = DOORGAANSTOP;
    }

    private void initSound() {
        sc = new SoundCollection(h, TAG);
        metroTask = new MetronomeAsyncTask();
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void playBeatList() {
        iBeatSound = 0;
        while (doorgaan>DOORGAANSTOP
                && iBeatSound < beatList.size()) {
            Log.d(TAG, "beatSound " + iBeatSound + " info:" + beatList.get(iBeatSound).display(iBeatSound, subs));
            iBeatSound+=beatList.get(iBeatSound).barNext;
            if (iBeatSound>=beatList.size()) {
                Log.d(TAG,"beatSound ready");
                // TODO play sound
                doorgaan--;
            }
        }
    }

    long getTimeMillis() {
        return System.nanoTime() / 1000000;
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

        public MetronomeAsyncTask() {
            h.logD(TAG, "MetronomeAsyncTask constructor");
        }

        protected String doInBackground(Void... params) {
            h.logD(TAG, "doInBackground");
            iBeatUI = 0;
            mHandler.post(runUI);
            playBeatList();
            return null;
        }
    }
}
