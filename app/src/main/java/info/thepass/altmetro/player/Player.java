package info.thepass.altmetro.player;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import info.thepass.altmetro.aaaUI.TrackFragment;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class Player extends Fragment {
    public final static String TAG = "trak:Player";

    public TrackFragment trackFragment;
    public HelperMetro h;
    public PlayerData pd;

    public Player thisFrag;
    public PlayerView playerView;

    public SoundBuilder soundBuilder;
    public int buildCounter;

    public Thread audioThread = null;
    public PlayerAudio playerAudio = null;
    public Thread videoThread = null;
    public PlayerVideo playerVideo = null;
    public SurfaceHolder sh = null;

    public LayoutUpdater layoutUpdater;
    public Stopper stopper;

    /*****************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fragment zonder UI
        setRetainInstance(true);
        thisFrag = this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        h = new HelperMetro(getActivity());
        pd = new PlayerData(h);
        initRunnables();
        bootAudio();
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETBEATMANAGERINIT, Activity.RESULT_OK, intent);
    }

    public void bootAudio() {
        playerAudio = new PlayerAudio(h, this);
        audioThread = new Thread(playerAudio);
        audioThread.setPriority(Thread.MIN_PRIORITY);
        audioThread.start();
    }

    public void bootVideo() {
        if (!pd.videoStarted) {
            pd.videoStarted = true;

//            audioThread.setPriority(Thread.MIN_PRIORITY);

////            playerVideo = new PlayerVideo(h, this);
//            videoThread = new Thread(playerVideo);
//            Log.d(TAG,"bootVideo start thread");
//            videoThread.setPriority(Thread.MIN_PRIORITY + 1);
//            videoThread.start();
        }
    }

    public void buildBeat(Track newTrack) {
        pd.timeBuild1 = h.getNanoTime();
        pd.bmTrack = newTrack;
        trackFragment.track = newTrack;
        buildCounter++;
        if (!pd.building) {
            Thread t = new Thread(soundBuilder);
            t.start();
        }
    }

    public void startPlayer() {
        Log.d(TAG, "startPlayer");
        pd.timeStart1 = h.getNanoTime();
        pd.playing = Keys.PLAYGO;
        playerAudio.onResume();
//        playerVideo.onResume();
    }

    public void stopPlayer() {
        Log.d(TAG, "stopPlayer");
        pd.timeStop1 = h.getNanoTime();
        pd.playing = Keys.PLAYPAUSED;
        playerAudio.onPause();
//        playerVideo.onPause();
    }

    private void initRunnables() {
        layoutUpdater = new LayoutUpdater();
        stopper = new Stopper();
        soundBuilder = new SoundBuilder();
    }

    public boolean isPlaying() {
        return (pd.playing == Keys.PLAYGO);
    }

    public class LayoutUpdater implements Runnable {
        public void run() {
            long timeLayout2 = h.getNanoTime();
            Log.d(TAG, "LayoutUpdater " + h.deltaTime(pd.timeLayout1, timeLayout2)
                    + "/" + h.deltaTime(pd.timeStart1, timeLayout2));
            trackFragment.updateLayout();
        }
    }

    public class Stopper implements Runnable {
        public void run() {
            pd.timeStop2 = h.getNanoTime();
            trackFragment.doStopPlayer();
            pd.timeStop3 = h.getNanoTime();
            Log.d(TAG, "Stopper time:" + h.deltaTime(pd.timeStop1, pd.timeStop2) + "|" + h.deltaTime(pd.timeStop2, pd.timeStop3));
        }
    }

    public class SoundBuilder implements Runnable {
        int thisBuildCounter;

        public void run() {
            pd.timeBuild2 = h.getNanoTime();
            pd.building = true;
            do {
                thisBuildCounter = buildCounter;
                pd.bmTrack.buildBeat(thisFrag, h);
                pd.timeBuild3 = h.getNanoTime();

            } while (thisBuildCounter < buildCounter);

            pd.building = false;
            pd.timeBuild4 = h.getNanoTime();
            Log.d(TAG, "SoundBuilder: finished building beat and sound: " + buildCounter
                    + " time:" + h.deltaTime(pd.timeBuild1, pd.timeBuild2)
                    + "|" + h.deltaTime(pd.timeBuild2, pd.timeBuild3)
                    + "|" + h.deltaTime(pd.timeBuild3, pd.timeBuild4));
//            bmTrack.soundDump(thisFrag);
        }
    }
}