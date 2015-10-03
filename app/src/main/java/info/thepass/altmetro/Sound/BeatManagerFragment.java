package info.thepass.altmetro.Sound;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import info.thepass.altmetro.R;
import info.thepass.altmetro.aaaUI.TrackFragment;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class BeatManagerFragment extends Fragment {
    public final static String TAGF = "trak:BeatMgr";
    public final static int playingSTART = 2;
    public final static int playingSTOP = 0;
    // Objecten
    public TrackFragment trackFragment;
    public HelperMetro h;
    public boolean building;
    public Metronome metronome;

    private BeatManagerFragment thisFrag;
    // declaraties
    private String[] subs;
    private Thread metroThread;

    private LayoutUpdater layoutUpdater;
    private Stopper stopper;
    private SoundBuilder soundBuilder;
    public int buildCounter;

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
        subs = h.getStringArray(R.array.sub_pattern);
        metronome.playing = false;
        initRunnables();
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETBEATMANAGERINIT, Activity.RESULT_OK, intent);
    }

    public void buildBeat(Track newTrack) {
        metronome.timeBuild1 = h.getNanoTime();
        metronome.bmTrack = newTrack;
        trackFragment.track = newTrack;
        buildCounter++;
        if (!building) {
            Thread t = new Thread(soundBuilder);
            t.start();
        }
    }

    public void startPlayer() {
        Log.d(TAGF, "startPlayer");
        metronome.timeStart1 = h.getNanoTime();
        metronome.playing = true;
        metronome.onResume();
    }

    public void stopPlayer() {
        Log.d(TAGF, "stopPlayer");
        metronome.timeStop1 = h.getNanoTime();
        metronome.playing = false;
        metronome.onPause();
    }

    private void initRunnables() {
        metronome = new Metronome(h);
        metroThread = new Thread(metronome);
        metroThread.start();

        layoutUpdater = new LayoutUpdater();
        stopper = new Stopper();
        soundBuilder = new SoundBuilder();
    }

    public class LayoutUpdater implements Runnable {
        public void run() {
            long timeLayout2 = h.getNanoTime();
            Log.d(TAGF, "LayoutUpdater " + h.deltaTime(metronome.timeLayout1, timeLayout2)
                    + "/" + h.deltaTime(metronome.timeStart1, timeLayout2));
            trackFragment.updateLayout();
        }
    }

    public class Stopper implements Runnable {
        public void run() {
            long timeStop2 = h.getNanoTime();
            trackFragment.doStopPlayer();
            long timeStop3 = h.getNanoTime();
            Log.d(TAGF, "Stopper time:" + h.deltaTime(metronome.timeStop1, timeStop2) + "|" + h.deltaTime(timeStop2, timeStop3));
        }
    }

    public class SoundBuilder implements Runnable {
        int thisBuildCounter;
        long timeBuild2;
        long timeBuild3;
        long timeBuild4;

        public void run() {
            timeBuild2 = h.getNanoTime();
            building = true;
            do {
                thisBuildCounter = buildCounter;
                metronome.bmTrack.buildBeat(thisFrag, h);
                timeBuild3 = h.getNanoTime();

            } while (thisBuildCounter < buildCounter);

            building = false;
            timeBuild4 = h.getNanoTime();
            Log.d(TAGF, "SoundBuilder: finished building beat and sound: " + buildCounter
                    + " time:" + h.deltaTime(metronome.timeBuild1, timeBuild2)
                    + "|" + h.deltaTime(timeBuild2, timeBuild3)
                    + "|" + h.deltaTime(timeBuild3, timeBuild4));
//            bmTrack.soundDump(thisFrag);
        }
    }
}