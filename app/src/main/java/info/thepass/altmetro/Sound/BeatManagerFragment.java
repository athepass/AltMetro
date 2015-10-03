package info.thepass.altmetro.Sound;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.aaaUI.TrackFragment;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class BeatManagerFragment extends Fragment {
    public final static String TAGF = "trak:BeatMgr";
    public final static String TAGV = "trak:SurfaceView";
    public final static String TAGM = "trak:Metronome";
    public final static int playingSTART = 2;
    public final static int playingSTOP = 0;
    // Objecten
    public TrackFragment trackFragment;
    public HelperMetro h;
    public boolean playing;
    public int barCounter;
    public int buildCounter;
    public boolean building;
    //    public EmphasisViewManager evmPlayer;
    private long timeStart1;
    private long timeBeat1;
    public int delayCounter;
    public int delaySum;
    private BeatManagerFragment thisFrag;
    private SoundCollection sc;
    private AudioTrack audioTrack;
    // declaraties
    private String[] subs;
    private Track bmTrack;
    private Repeat bmRepeat;
    private int iBeatList;
    private int playDuration;
    private int soundLength;
    private long timeStop1;
    private long timeLayout1;
    private long timeBuild1;

    private Metronome metronome;
    private Thread metroThread;
    private SurfaceHolder sh;
    private PlayerView pv;

    private LayoutUpdater layoutUpdater;
//    private BeatUpdater beatUpdater;
    private Stopper stopper;
    private SoundBuilder soundBuilder;
    private final Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);

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
        playing = false;
        initSound();
        initPaint();
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETBEATMANAGERINIT, Activity.RESULT_OK, intent);
    }

    public void buildBeat(Track newTrack) {
        timeBuild1 = getNanoTime();
        bmTrack = newTrack;
        trackFragment.track = newTrack;
        buildCounter++;
        if (!building) {
            Thread t = new Thread(soundBuilder);
            t.start();
        }
    }

    public void startPlayer() {
        playing = true;
        timeStart1 = getNanoTime();
        Log.d(TAGF, "startPlayer");
        metronome.onResume();
    }

    public void stopPlayer() {
        Log.d(TAGF, "stopPlayer");
        timeStop1 = getNanoTime();
        playing = false;
    }

    private void initSound() {
        // start Metronome runnable in non-stop thread
        metronome = new Metronome();
        metroThread = new Thread(metronome);
        metroThread.start();
        // construct worker runnables
        layoutUpdater = new LayoutUpdater();
//        beatUpdater = new BeatUpdater();
        stopper = new Stopper();
        soundBuilder = new SoundBuilder();
        // initialise audio
        sc = new SoundCollection(h, TAGF);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void initPaint() {
        paintHigh.setColor(Color.RED);
        paintHigh.setStyle(Paint.Style.FILL);

        paintLow.setColor(Color.YELLOW);
        paintLow.setStyle(Paint.Style.FILL);

        paintNone.setColor(Color.BLUE);
        paintNone.setStyle(Paint.Style.FILL);

        paintText.setColor(Color.GREEN);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(20);
    }

    public long getNanoTime() {
        return System.nanoTime();
    }

    public String deltaTime(long time1, long time2) {
        return String.format("%.3f", (float) (time2 - time1) / 1000000f) + "ms";
    }

    public class PlayerView extends SurfaceView
            implements SurfaceHolder.Callback {

        private SurfaceHolder sh;
        private PlayerView pv;
        private Thread thread;
        private Metronome metronome;
        private Context ctx;
        private int counter = 0;

        public PlayerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            Log.d(TAGV, "constructor");
            ctx = context;
            pv = this;
            sh = getHolder();
            sh.addCallback(this);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAGV, "surfaceCreated");
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.d(TAGV, "surfaceChanged");
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAGV, "surfaceDestroyed");
        }

    }

    public class LayoutUpdater implements Runnable {
        public void run() {
            long timeLayout2 = getNanoTime();
            Log.d(TAGM, "LayoutUpdater " + deltaTime(timeLayout1, timeLayout2)
                    + "/" + deltaTime(timeStart1, timeLayout2));
            trackFragment.updateLayout();
        }
    }

//    public class BeatUpdater implements Runnable {
//        public void run() {
//            long timeBeat2 = getNanoTime();
//            if (iBeatList < bmRepeat.beatList.size()) {
//                Beat beat = bmRepeat.beatList.get(iBeatList);
//
//                // TODO update emphasis
////                trackFragment.emphasisView.beat = beat.beatNext;
////                trackFragment.emphasisView.invalidate();
//                long timeBeat3 = getNanoTime();
//                Log.d(TAG, "beatUpdater " + beat.beatNext + " time:" + deltaTime(timeBeat1, timeBeat2)
//                        + "/" + deltaTime(timeBeat1, timeBeat3));
//            }
//        }
//    }
//
    public class Stopper implements Runnable {
        public void run() {
            long timeStop2 = getNanoTime();
            trackFragment.doStopPlayer();
            long timeStop3 = getNanoTime();
            Log.d(TAGM, "stopper time:" + deltaTime(timeStop1, timeStop2) + "|" + deltaTime(timeStop2, timeStop3));
        }
    }

    public class SoundBuilder implements Runnable {
        int thisBuildCounter;
        long timeBuild2;
        long timeBuild3;
        long timeBuild4;

        public void run() {
            timeBuild2 = getNanoTime();
            building = true;
            do {
                thisBuildCounter = buildCounter;
                bmTrack.buildBeat(thisFrag, h);
                timeBuild3 = getNanoTime();

            } while (thisBuildCounter < buildCounter);

            building = false;
            timeBuild4 = getNanoTime();
            Log.d(TAGM, "finished building beat and sound: " + buildCounter
                    + " time:" + deltaTime(timeBuild1, timeBuild2)
                    + "|" + deltaTime(timeBuild2, timeBuild3)
                    + "|" + deltaTime(timeBuild3, timeBuild4));
//            bmTrack.soundDump(thisFrag);
        }
    }
}