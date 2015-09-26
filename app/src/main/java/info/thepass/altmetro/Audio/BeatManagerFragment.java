package info.thepass.altmetro.Audio;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class BeatManagerFragment extends Fragment {
    public final static String TAG = "trak:BeatMgr";
    public final static int DOORGAANSTART = 2;
    public final static int DOORGAANSTOP = 0;
    public LinearLayout llRoot;
    public TextView tvInfo;
    public Track track;
    public TrackData trackData;
    public ArrayList<Beat> beatList;
    public int barCounter;
    String[] subs;
    private HelperMetro h;
    private Handler mHandler;
    private MetronomeAsyncTask metroTask;
    private SoundCollection sc;
    private AudioTrack audioTrack;
    private int playDuration;
    private int soundLength;
    private boolean doorgaanSound;
    private boolean doorgaanUI;
    private boolean soundFirstBeat;
    private int iBeatSound;
    private int iBeatUI;
    /*****************************************************************/
    Runnable runUI = new Runnable() {
        public void run() {
            int uiDelay = 25;
            if (gaDoor()) {
                Log.d(TAG, "beat[UI] " + iBeatUI + " info:" + beatList.get(iBeatUI).display(iBeatUI, subs));
                iBeatUI += beatList.get(iBeatUI).barNext;
                if (iBeatUI < beatList.size()) {
                    // TODO bereken doorlooptijd synced
                    // TODO update user interface
                    mHandler.postDelayed(runUI, uiDelay);
                } else {
                    Log.d(TAG, "beatUI ready");
                    doorgaanUI = false;
                    if (!gaDoor()) {
                        stopPlaying();
                    }
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
            track.buildBeat(this, h);
        }
//        dumpBeatList(-1);
    }

    private boolean gaDoor() {
        if (!doorgaanUI && !doorgaanSound) {
            Log.d(TAG, "ga niet door ");
            return false;
        } else {
            return true;
        }
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
        soundFirstBeat = h.prefs.getBoolean(Keys.PREFFIRSTBEAT, false);
        build(track);
        for (int i = 0; i < beatList.size(); i++) {
            beatList.get(i).buildSound();
            dumpBeatList(i);
        }
        doorgaanUI = true;
        doorgaanSound = true;
        metroTask.execute();
    }

    public void stopPlayer() {
        Log.d(TAG, "stopPlayer");
        metroTask = new MetronomeAsyncTask();
        Runtime.getRuntime().gc();
        doorgaanUI = false;
        doorgaanSound = false;
    }

    private void stopPlaying() {
        Log.d(TAG, "stopPlaying");
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETBEATMANAGER, Activity.RESULT_OK, intent);
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
        while (gaDoor() && iBeatSound < beatList.size()) {
            Beat beat = beatList.get(iBeatSound);
            Log.d(TAG, "beat[Sound] " + iBeatSound + " info:" + beatList.get(iBeatSound).display(iBeatSound, subs));
//            tvInfo.setText(beat.info);
            playSoundList(beat);
            iBeatSound += beatList.get(iBeatSound).barNext;
            if (iBeatSound >= beatList.size()) {
                Log.d(TAG, "beatSound ready");
                doorgaanSound = false;
            }
        }
    }

    private void playSoundList(Beat beat) {
        for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
            Sound sound = beat.soundList.get(iSound);
            switch (sound.soundType) {
                case Keys.SOUNDFIRST:
                    writeSound(sc.soundFirst, sound.duration);
                    break;
                case Keys.SOUNDHIGH:
                    writeSound(sc.soundHigh, sound.duration);
                    break;
                case Keys.SOUNDLOW:
                    writeSound(sc.soundLow, sound.duration);
                    break;
                case Keys.SOUNDNONE:
                    writeSound(sc.soundSilence, sound.duration);
                    break;
                case Keys.SOUNDSUB:
                    writeSound(sc.soundSub, sound.duration);
                    break;
                case Keys.SOUNDSILENCE:
                    writeSound(sc.soundSilence, sound.duration);
                    break;
                default:
                    throw new RuntimeException("playBeat invalid soundtype " + sound.soundType);
            }
        }
    }

    private void writeSound(byte[] soundBytes, int duration) {
        playDuration = duration * 2;
        while (gaDoor() && playDuration > 0) {
            if (playDuration > SoundCollection.SOUNDLENGTH) {
                soundLength = SoundCollection.SOUNDLENGTH;
                playDuration -= SoundCollection.SOUNDLENGTH;
            } else {
                soundLength = playDuration;
                playDuration = 0;
            }
            audioTrack.write(soundBytes, 0, soundLength);
        }
    }


    long getTimeMillis() {
        return System.nanoTime() / 1000000;
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

        public MetronomeAsyncTask() {
            h.logD(TAG, "MetronomeAsyncTask constructor");
        }

        @Override
        protected String doInBackground(Void... params) {
            h.logD(TAG, "doInBackground");
            iBeatUI = 0;
            mHandler.post(runUI);
            playBeatList();
            Log.d(TAG, "playBeatList ready");
            return "playBeatList ready";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute");
            stopPlaying();
            return;
        }
    }
}
