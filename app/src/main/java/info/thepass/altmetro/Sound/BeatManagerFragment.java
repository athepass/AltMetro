package info.thepass.altmetro.Sound;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

import info.thepass.altmetro.R;
import info.thepass.altmetro.aaUI.TrackFragment;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class BeatManagerFragment extends Fragment {
    public final static String TAG = "trak:BeatMgr";
    public final static int playingSTART = 2;
    public final static int playingSTOP = 0;
    // Objecten
    public TrackFragment trackFragment;
    public HelperMetro h;
    public boolean playing;
    public int barCounter;
    public int buildCounter;
    public boolean building;
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
    private long timeStart1;
    private long timeStop1;
    private long timeBeat1;
    private long timeLayout1;
    private long timeBuild1;
    private Metronome metronome;
    private LayoutUpdater layoutUpdater;
    private BeatUpdater beatUpdater;
    private Stopper stopper;
    private SoundBuilder soundBuilder;

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
        Log.d(TAG, "startPlayer");


        Thread threadMetro = new Thread(metronome);
        threadMetro.start();
    }

    public void stopPlayer() {
        Log.d(TAG, "stopPlayer");
        timeStop1 = getNanoTime();
        playing = false;
    }

    private void initSound() {
        metronome = new Metronome();
        layoutUpdater = new LayoutUpdater();
        beatUpdater = new BeatUpdater();
        stopper = new Stopper();
        soundBuilder = new SoundBuilder();

        sc = new SoundCollection(h, TAG);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public long getNanoTime() {
        return System.nanoTime();
    }

    public String deltaTime(long time1, long time2) {
        return String.format("%.3f", (float) (time2 - time1) / 1000000f) + "ms";
    }

    public class Metronome implements Runnable {

        public void run() {
            long timeStart2 = getNanoTime();
            timeLayout1 = getNanoTime();

            getActivity().runOnUiThread(layoutUpdater);

            long timeStart3 = getNanoTime();
            h.logD(TAG, "Run metronome t=" + deltaTime(timeStart1, timeStart2)
                    + ".." + deltaTime(timeStart2, timeStart3));

            for (int irep = 0; irep < bmTrack.repeats.size(); irep++) {
                bmRepeat = bmTrack.repeats.get(irep);
                playRepeat();
            }

            Log.d(TAG, "Run metronome finished");
            timeStop1 = getNanoTime();
            getActivity().runOnUiThread(stopper);
        }

        private void playRepeat() {
            int iRepeat = 0;
            int step = 0;
            int barCounter = 0;
            while (playing && iRepeat < bmRepeat.barCount) {
                iBeatList = 0;
                while (playing && iBeatList < bmRepeat.beatList.size()) {
                    Beat beat = bmRepeat.beatList.get(iBeatList);
                    Log.d(TAG, "beat[Sound] " + iBeatList + " info:" + bmRepeat.beatList.get(iBeatList).display(iBeatList, subs));
                    timeBeat1 = getNanoTime();
                    getActivity().runOnUiThread(beatUpdater);
                    if (iBeatList < beat.beats - 1) { // niet op de laatste beat: volgend beat
                        step = 1;
                    } else {    // laatste beat
                        if (bmRepeat.noEnd) {   // noend: altijd naar 1
                            step = 1 - beat.beats;
                        } else {
                            if (barCounter == bmRepeat.barCount - 1) { // laatste bar binnen repeat
                                step = 1;
                            } else { // naar 1 voor afspelen volgende bar
                                step = 1 - beat.beats;
                            }
                        }
                    }
                    Log.d(TAG,"play rep:"+iRepeat + " bar:"+barCounter + " beat"+ iBeatList);
                    playSoundList(beat);

                    if (iBeatList == beat.beats - 1) { // bar counter ophogen
                        barCounter++;
                    }

                    iBeatList += step;
                    if (iBeatList >= bmRepeat.beatList.size()) {
                        Log.d(TAG, "beatSound ready");
                        playing = false;
                    }
                }

                if (!bmRepeat.noEnd) {
                    iRepeat++;
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
            while (playing && playDuration > 0) {
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

    }

    public class LayoutUpdater implements Runnable {
        public void run() {
            long timeLayout2 = getNanoTime();
            Log.d(TAG, "LayoutUpdater " + deltaTime(timeLayout1, timeLayout2)
                    + "/" + deltaTime(timeStart1, timeLayout2));
            trackFragment.updateLayout();
        }
    }

    public class BeatUpdater implements Runnable {
        public void run() {
            long timeBeat2 = getNanoTime();
            if (iBeatList < bmRepeat.beatList.size()) {
                Beat beat = bmRepeat.beatList.get(iBeatList);
                trackFragment.tvInfo.setText("" + bmRepeat.beatList.get(iBeatList).info);
//            TODO UPDATE EMPHASIS
            }
        }
    }

    public class Stopper implements Runnable {
        public void run() {
            long timeStop2 = getNanoTime();
            trackFragment.doStopPlayer();
            long timeStop3 = getNanoTime();
            Log.d(TAG, "stopper time:" + deltaTime(timeStop1, timeStop2) + "|" + deltaTime(timeStop2, timeStop3));
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
            Log.d(TAG, "finished building beat bar:" + barCounter
                    + " time:" + deltaTime(timeBuild1, timeBuild2)
                    + "|" + deltaTime(timeBuild2, timeBuild3)
                    + "|" + deltaTime(timeBuild3, timeBuild4));
            bmTrack.soundDump(h,thisFrag);
        }
    }
}