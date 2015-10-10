package info.thepass.altmetro.player;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class PlayerVideo implements Runnable {
    public final static String TAG = "trak:PlayerVdieo";
    // parent
    public HelperMetro h;
    public BarManager bm;
    public PlayerData pd;
    // runnable management
    public Object mPauseLock;
    private boolean mFinished;
    private boolean mPaused;

    public PlayerVideo(HelperMetro hh, BarManager bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        h.logD(TAG, "constructor");
        mPauseLock = new Object();
        mPaused = true;
        mFinished = false;
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
        h.logD(TAG, "onPause");
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        h.logD(TAG, "onResume");
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notify();
        }
    }

    private void doStep() {
    }

    private void doDrawBeat() {
        pd.timeLayout1 = h.getNanoTime();
        checkPat();
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

    private void checkPat() {
        if (pd.bmPat.patHash != pd.svwPatHash) {
            pd.berekenPatternDisplay();
            pd.bmPat.patHash = pd.svwPatHash;
        }
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

    private int getNextBeat() {
        int nextBeat = 0;
        if (pd.beatListCounter < pd.bmBeat.beats - 1) { // niet op de laatste beat: volgend beat
            nextBeat = 1;
        } else {    // laatste beat
            if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                nextBeat = 1 - pd.bmBeat.beats;
            } else {
                if (pd.repeatBarCounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                    nextBeat = 1;
                } else { // naar 1 voor afspelen volgende bar
                    nextBeat = 1 - pd.bmBeat.beats;
                }
            }
        }
        return nextBeat;
    }

   private void initRun() {
        h.logD(TAG, "start Runnable " + bm.audioThread.getPriority());
    }

    private void finishRun() {
        h.logI(TAG, "finish Runnable");
    }
}