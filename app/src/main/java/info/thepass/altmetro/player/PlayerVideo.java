package info.thepass.altmetro.player;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class PlayerVideo implements Runnable {
    public final static String TAG = "trak:PlayerVideo";
    // runnable management
    public final Object mPauseLock;
    // parent
    public HelperMetro h;
    public BarManager bm;
    public PlayerData pd;
    private boolean mFinished;
    private boolean mPaused;

    private int msgCounter = 0;

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

    private void doWaitTime(long time) {
        synchronized (mPauseLock) {
            try {
                int nano = (int) time % 1000000;
                long ms = (long) (time - nano) / 1000000;
//                Log.d(TAG, "time " + ms + "." + nano);
                if (ms > 0 && nano > 0)
                    mPauseLock.wait(ms, nano);
            } catch (InterruptedException e) {
            }
        }
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
            pd.timeVideoResume = h.getNanoTime();
        }
    }

    private void doStep() {
        pd.timeVideoDoStep = h.getNanoTime();

        long delay = pd.timeBeatVideo - pd.timeVideoDoStep;
        String msg = "doStep: t2: " + h.deltaTime(pd.timeInitPlay, pd.timeVideoDoStep);
        msg += " t1:" + h.deltaTime(pd.timeInitPlay, pd.timeBeatVideo);
        msg += " delay:" + h.getMillisFormatted(delay);

        this.doWaitTime(delay);
        pd.timeVideoDraw = h.getNanoTime();
        doDrawBeat();
        pd.timeVideoDrawed = h.getNanoTime();
        msg += " drawtime:" + h.deltaTime(pd.timeVideoResume, pd.timeVideoDraw);
        msg += " lag:" + h.deltaTime(pd.timeVideoDraw, pd.timeVideoDrawed);

        if (delay > 0) {
            msgCounter=0;
        } else {
            msgCounter++;
        }
        if (msgCounter<2)
            h.logD(TAG, msg);

        if (mPaused)
            cleanCanvas();
    }

    private void doDrawBeat() {
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
                        canvas.drawCircle(cx, cy, pd.svwRadius, paint);
                        if (pd.bmBeat.beatIndex == i) {
                            int cxi = pd.svwAfstandH + +pd.svwRadius + (pd.svwAfstandH + 2 * pd.svwRadius) * i;
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
    }

    private void cleanCanvas() {
        Log.d(TAG, "cleanCanvas");
        if (pd.svwReady) {
            Canvas canvas = bm.sh.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
                bm.sh.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void checkPat() {
        if (pd.bmPat.patHash != pd.svwPatHash) {
            pd.berekenPatternDisplay();
            pd.bmPat.patHash = pd.svwPatHash;
        }
    }

    private void initRun() {
        h.logD(TAG, "start Runnable " + bm.audioThread.getPriority());
    }

    private void finishRun() {
        h.logI(TAG, "finish Runnable");
    }
}