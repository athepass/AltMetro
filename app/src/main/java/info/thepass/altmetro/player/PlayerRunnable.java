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

public class PlayerRunnable implements Runnable {
    public final static String TAG = "trak:PlayerRunnable";
    // parent
    public HelperMetro h;
    public BarManager bm;
    public PlayerData pd;
    // runnable management
    public Object mPauseLock;
    // sound management
    public int soundLength;
    public SoundCollection sc;
    public AudioTrack audioTrack;
    private boolean mFinished;
    private boolean mPaused;

    public PlayerRunnable(HelperMetro hh, BarManager bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        Log.d(TAG, "constructor");
        mPauseLock = new Object();
        mPaused = true;
        mFinished = false;

        initAudio();
    }

    public void run() {
        initRun();

        while (!mFinished) {
            if (!mPaused) {
                doStep();
            }
            doWait();
        }
        finishRun();
    }

    private void doStep() {
        initPlay();
        for (int irep = 0; irep < pd.bmTrack.repeats.size(); irep++) {
            pd.bmRepeat = pd.bmTrack.repeats.get(irep);
            pd.bmPat = pd.bmTrack.pats.get(pd.bmTrack.patSelected);
            playRepeat();

        }
        finishPlay();
    }

    private void doWait() {
        synchronized (mPauseLock) {
            while (mPaused) {
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

    private void initPlay() {
        pd.berekenPatternDisplay();
        pd.timeStart2 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.layoutUpdater);
        pd.timeStart3 = h.getNanoTime();
        h.logD(TAG, "initPlay t=" + h.deltaTime(pd.timeStart1, pd.timeStart2)
                + ".." + h.deltaTime(pd.timeStart1, pd.timeStart3));
    }

    private void finishPlay() {
        doDrawBeatClean();
        Log.d(TAG, "finish Play");
        pd.timeStop1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.stopper);
    }

    private void playRepeat() {
        int iRepeat = 0;
        int step = 0;
        while (!mPaused && iRepeat < pd.bmRepeat.barCount) {
            pd.iBeatList = 0;
            while (!mPaused && pd.iBeatList < pd.bmRepeat.beatList.size()) {
                pd.bmBeat = pd.bmRepeat.beatList.get(pd.iBeatList);
                pd.currentBeat = pd.bmBeat.beatIndex + 1;
                String logInfo = "beat[Sound] " + pd.iBeatList + " info:"
                        + pd.bmRepeat.beatList.get(pd.iBeatList).display(pd.iBeatList, pd.subs);
                pd.timeBeat1 = h.getNanoTime();
                step = getNextStep();

                playSoundList(pd.bmBeat);

                logInfo += " draw:" + h.deltaTime(pd.timeLayout1, pd.timeLayout2);
                Log.d(TAG, logInfo);

                if (pd.iBeatList == pd.bmBeat.beats - 1) { // bar counter ophogen
                    pd.repeatBarcounter++;
                }

                pd.iBeatList += step;
                if (pd.iBeatList >= pd.bmRepeat.beatList.size()) {
                    Log.d(TAG, "beatSound ready");
                }
            }

            if (!pd.bmRepeat.noEnd) {
                iRepeat++;
            }
        }
    }

    private void doDrawBeat() {
        pd.timeLayout1 = h.getNanoTime();
        if (pd.videoStarted && pd.svwReady) {
            Canvas canvas = null;
            try {
                canvas = bm.sh.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                    for (int i = 0; i < pd.bmPat.patBeats; i++) {
                        Paint paint;
                        switch (pd.bmPat.patBeatState[i]) {
                            case Keys.SOUNDHIGH:
                                paint = pd.paintHigh;
                                break;
                            case Keys.SOUNDLOW:
                                paint = pd.paintLow;
                                break;
                            case Keys.SOUNDNONE:
                                paint = pd.paintNone;
                                break;
                            default:
                                throw new RuntimeException("invalid beatstate " + pd.bmPat.patBeatState[i]);
                        }
                        int cx = pd.svwAfstandH + +pd.svwRadius + (pd.svwAfstandH + 2 * pd.svwRadius) * i;
                        int cy = pd.svwAfstandV + pd.svwRadius;
                        int radiusB = (int) Math.round(pd.svwRadius * 0.75);
                        int cxi = -1;
                        canvas.drawCircle(cx, cy, pd.svwRadius, paint);
                        if (pd.bmBeat.beatIndex == i) {
                            cxi = pd.svwAfstandH + +pd.svwRadius + (pd.svwAfstandH + 2 * pd.svwRadius) * i;
                            canvas.drawCircle(cxi, cy, radiusB, pd.paintBeat);
                            canvas.drawText("" + (i + 1), cxi - radiusB / 3, cy + radiusB / 3, pd.paintText);
                        }
                    }
                }
            } finally {
                if (canvas != null) {
                    bm.sh.unlockCanvasAndPost(canvas);
                }
            }
        }
        pd.timeLayout2 = h.getNanoTime();
    }

    private void doDrawBeatClean() {
        if (pd.videoStarted && pd.svwReady) {
            Canvas canvas = null;
            try {
                canvas = bm.sh.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);
                }
            } finally {
                if (canvas != null) {
                    bm.sh.unlockCanvasAndPost(canvas);
                }
            }
        }
        pd.timeLayout2 = h.getNanoTime();
    }

    private int getNextStep() {
        int step = 0;
        if (pd.iBeatList < pd.bmBeat.beats - 1) { // niet op de laatste beat: volgend beat
            step = 1;
        } else {    // laatste beat
            if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                step = 1 - pd.bmBeat.beats;
            } else {
                if (pd.repeatBarcounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                    step = 1;
                } else { // naar 1 voor afspelen volgende bar
                    step = 1 - pd.bmBeat.beats;
                }
            }
        }
        return step;
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
            if (iSound == 0) {
                doDrawBeat();
            }
        }
    }

    private void writeSound(byte[] soundBytes, int duration) {
        int playDuration = duration * 2;
        while (!mPaused && playDuration > 0) {
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

    private void initAudio() {
        sc = new SoundCollection(h, TAG);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void initRun() {
        Log.d(TAG, "start Runnable " + bm.audioThread.getPriority());
    }

    private void finishRun() {
        Log.i(TAG, "finish Runnable");
    }
}