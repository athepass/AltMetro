package info.thepass.altmetro.Sound;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.SurfaceHolder;

import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class Metronome implements Runnable {
    public final static String TAG = "trak:Metronome";
    private final Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    public int delayCounter;
    public int delaySum;
    public long timeStart1;
    public long timeBeat1;
    public long timeStop1;
    public long timeLayout1;
    public long timeBuild1;
    public Track bmTrack;
    public int barCounter;
    public boolean playing;

    private HelperMetro h;
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private Repeat bmRepeat;
    private int iBeatList;
    private int playDuration;
    private int soundLength;
    private SoundCollection sc;
    private AudioTrack audioTrack;
    private SurfaceHolder sh;

    public Metronome(HelperMetro hh) {
        h = hh;
        Log.d(TAG, "constructor");
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        initAudio();
        initPaint();
    }

    public void run() {
        long timeStart2 = h.getNanoTime();
        timeLayout1 = h.getNanoTime();
        getActivity().runOnUiThread(layoutUpdater);
        Canvas canvas = sh.lockCanvas(null);
        canvas.drawColor(Color.BLACK);
        sh.unlockCanvasAndPost(canvas);
        long timeStart3 = h.getNanoTime();


        h.logD(TAG, "Run metronome t=" + h.deltaTime(timeStart1, timeStart2) + ".." + deltaTime(timeStart2, timeStart3));
        for (int irep = 0; irep < bmTrack.repeats.size(); irep++) {
            bmRepeat = bmTrack.repeats.get(irep);
            Pat pat = bmTrack.pats.get(bmTrack.patSelected);
            playRepeat();
        }

        Log.d(TAG, "Run metronome finished");
        timeStop1 = h.getNanoTime();
        getActivity().runOnUiThread(stopper);
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notify();
        }
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
                timeBeat1 = h.getNanoTime();
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
//                long nu = h.getNanoTime();
//                Log.d(TAG,"sound"+iSound + " time:"+deltaTime(timeBeat1,nu));
            Sound sound = beat.soundList.get(iSound);

//                if (sound.playBeat) {
//                    getActivity().runOnUiThread(beatUpdater);
//                }

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

    private void doDraw(Canvas canvas) {
//            canvas.restore();
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(20 + barCounter * 10, 20, 50, paintHigh);
        canvas.drawText("counter=" + barCounter, 20, 20, paintText);
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

    private void initAudio() {
        sc = new SoundCollection(h, TAG);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }
}