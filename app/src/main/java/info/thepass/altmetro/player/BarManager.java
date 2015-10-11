package info.thepass.altmetro.player;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;

import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;
import info.thepass.altmetro.ui.TrackFragment;

public class BarManager extends Fragment {
    public final static String TAG = "trak:BarManager";

    public TrackFragment trackFragment;
    public HelperMetro h;
    public PlayerData pd;

    public BarManager thisFrag;
    public PlayerView playerView;

    public SoundBuilder soundBuilder;
    public int buildCounter;

    public Thread audioThread = null;
    public PlayerAudio playerAudio = null;
    public SurfaceHolder sh = null;

    public LayoutUpdater layoutUpdater;
    public InfoUpdater infoUpdater;
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
        h.logD(TAG,"onActivityCreated");
//        pd = new PlayerData(h);
        initRunnables();
        bootPlayer();
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETBEATMANAGERINIT, Activity.RESULT_OK, intent);
    }

    public void bootPlayer() {
        playerAudio = new PlayerAudio(h, this);
        audioThread = new Thread(playerAudio);
        audioThread.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
        audioThread.start();
    }

    public void buildBeat(Track newTrack) {
        pd.timeBuild1 = h.getNanoTime();
        pd.bmTrack = newTrack;
        buildCounter++;
        if (!pd.building) {
            Thread soundBuilderThread = new Thread(soundBuilder);
            soundBuilderThread.setPriority(Thread.NORM_PRIORITY - 1);
            soundBuilderThread.start();
        }
    }

    public void startPlayer() {
        h.logD(TAG, "startPlayer");
        pd.timeStartPlay = h.getNanoTime();
        playerAudio.onResume();
        getActivity().runOnUiThread(this.layoutUpdater);
    }

    public void stopPlayer() {
        h.logD(TAG, "stopPlayer");
        pd.timeStop1 = h.getNanoTime();
        pd.playStatus = Keys.PLAYEND;
        playerAudio.onPause();
    }

    private void initRunnables() {
        layoutUpdater = new LayoutUpdater();
        infoUpdater = new InfoUpdater();
        stopper = new Stopper();
        soundBuilder = new SoundBuilder();
    }

    public boolean isPlaying() {
        switch (pd.playStatus) {
            case Keys.PLAYSTOP:
            case Keys.PLAYEND:
//                Log.d(TAG, "isPlaying false " + pd.playStatus);
                return false;
            case Keys.PLAYSTART:
            case Keys.PLAYPLAY:
//                Log.d(TAG, "isPlaying true " + pd.playStatus);
                return true;
            default:
                throw new RuntimeException("isPlaying status ongeldig " + pd.playStatus);
        }
    }

    public class LayoutUpdater implements Runnable {
        public void run() {

            long timeLayout2 = h.getNanoTime();
            h.logD(TAG, "LayoutUpdater " + h.deltaTime(pd.timeLayout1, timeLayout2)
                    + "/" + h.deltaTime(pd.timeStartPlay, timeLayout2));
            trackFragment.updateLayout();
        }
    }

    public class InfoUpdater implements Runnable {
        public void run() {

            trackFragment.setInfo(pd.playerInfo);
        }
    }

    public class Stopper implements Runnable {
        public void run() {
            pd.timeStop2 = h.getNanoTime();
            trackFragment.doStopPlayer();
            pd.timeStop3 = h.getNanoTime();
            h.logD(TAG, "Stopper time:" + h.deltaTime(pd.timeStop1, pd.timeStop2) + "|" + h.deltaTime(pd.timeStop2, pd.timeStop3));
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
            h.logD(TAG, "SoundBuilder: finished building beat and sound: " + buildCounter
                    + " time:" + h.deltaTime(pd.timeBuild1, pd.timeBuild2)
                    + "|" + h.deltaTime(pd.timeBuild2, pd.timeBuild3)
                    + "|" + h.deltaTime(pd.timeBuild3, pd.timeBuild4));
//            bmTrack.soundDump(thisFrag);
        }
    }
}