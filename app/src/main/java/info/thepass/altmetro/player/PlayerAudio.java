package info.thepass.altmetro.player;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class PlayerAudio implements Runnable {
    public final static String TAG = "trak:PlayerAudio";
    private final Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    // parent
    public HelperMetro h;
    public Player bm;
    public PlayerData pd;
    // runnable management
    public Object mPauseLock;
    public boolean mFinished;
    // sound management
    public int soundLength;
    public SoundCollection sc;
    public AudioTrack audioTrack;

    public Canvas canvas;
    public int currentBeat;

    public PlayerAudio(HelperMetro hh, Player bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        Log.d(TAG, "constructor");
        mPauseLock = new Object();
        pd.mPaused = true;
        pd.mPlaying = false;
        mFinished = false;

        initAudio();
        initPaint();
        pd.shConstructed = false;
    }

    public void run() {

        while (!mFinished) {
            if (initCanvas()) {
                if (pd.mPlaying) {
                    doStep();
                }
                doWait();

            } else {
                try {
                    wait(2000);
                } catch (Exception e) {
                }
            }
        }
    }

    private boolean initCanvas() {
        if (pd.sh != null & !pd.shConstructed) {
            Log.d(TAG, "initCanvas");
            pd.shConstructed = true;
            try {
                wait(200);
            } catch (Exception e) {
            }
            canvas = pd.sh.lockCanvas();
            Log.d(TAG, "canvas==null");
            canvas.drawColor(Color.BLACK);
            canvas.drawCircle(20 + 10, 20, 10, paintHigh);
            pd.sh.unlockCanvasAndPost(canvas);
        }
        return pd.shConstructed;
    }

    private void doStep() {
        runInit();
        h.logD(TAG, "Run metronome t=" + h.deltaTime(pd.timeStart1, pd.timeStart2)
                + ".." + h.deltaTime(pd.timeStart1, pd.timeStart3));
        for (int irep = 0; irep < pd.bmTrack.repeats.size(); irep++) {
            pd.bmRepeat = pd.bmTrack.repeats.get(irep);
            pd.bmPat = pd.bmTrack.pats.get(pd.bmTrack.patSelected);
            playRepeat();
        }
        Log.d(TAG, "Run metronome finished");
        pd.timeStop1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.stopper);
    }

    private void doWait() {
        synchronized (mPauseLock) {
            while (pd.mPaused) {
                Log.d(TAG, "pauselock");
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        synchronized (mPauseLock) {
            pd.mPaused = true;
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        synchronized (mPauseLock) {
            pd.mPaused = false;
            mPauseLock.notify();
        }
    }

    private void runInit() {
        Log.d(TAG, "runInit");
        pd.timeStart2 = h.getNanoTime();
        pd.timeLayout1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.layoutUpdater);
        canvas = pd.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        pd.sh.unlockCanvasAndPost(canvas);
        pd.timeStart3 = h.getNanoTime();
    }

    private void playRepeat() {
        int iRepeat = 0;
        int step = 0;
        int barCounter = 0;
        while (pd.mPlaying && iRepeat < pd.bmRepeat.barCount) {
            pd.iBeatList = 0;
            while (pd.mPlaying && pd.iBeatList < pd.bmRepeat.beatList.size()) {
                Beat beat = pd.bmRepeat.beatList.get(pd.iBeatList);
                currentBeat = beat.beatIndex + 1;
                Log.d(TAG, "beat[Sound] " + pd.iBeatList + " info:" + pd.bmRepeat.beatList.get(pd.iBeatList).display(pd.iBeatList, pd.subs));
                pd.timeBeat1 = h.getNanoTime();
                if (pd.iBeatList < beat.beats - 1) { // niet op de laatste beat: volgend beat
                    step = 1;
                } else {    // laatste beat
                    if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                        step = 1 - beat.beats;
                    } else {
                        if (barCounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                            step = 1;
                        } else { // naar 1 voor afspelen volgende bar
                            step = 1 - beat.beats;
                        }
                    }
                }
                playSoundList(beat);

                if (pd.iBeatList == beat.beats - 1) { // bar counter ophogen
                    barCounter++;
                }

                pd.iBeatList += step;
                if (pd.iBeatList >= pd.bmRepeat.beatList.size()) {
                    Log.d(TAG, "beatSound ready");
                    pd.mPlaying = false;
                }
            }

            if (!pd.bmRepeat.noEnd) {
                iRepeat++;
            }
        }
    }

    private void playSoundList(Beat beat) {
        for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
//                long nu = h.getNanoTime();
//                Log.d(TAG,"sound"+iSound + " time:"+deltaTime(timeBeat1,nu));
            Sound sound = beat.soundList.get(iSound);
            doDraw();
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
        int playDuration = duration * 2;
        while (pd.mPlaying && playDuration > 0) {
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

    private void doDraw() {
        Log.d(TAG,"doDraw");
        canvas = pd.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(20 + currentBeat * 10, 20, 20, paintHigh);
        canvas.drawText("#" + currentBeat, 22 + currentBeat * 10, 20, paintText);
        pd.sh.unlockCanvasAndPost(canvas);
        Log.d(TAG, "doDraw");
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