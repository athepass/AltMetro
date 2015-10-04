package info.thepass.altmetro.player;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import info.thepass.altmetro.tools.HelperMetro;

public class PlayerVideo implements Runnable {
    public final static String TAG = "trak:PlayerVideo";
    private final Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mPaused;
    private int testCounter;
    private boolean testLoop = true;
    // algemene data
    private HelperMetro h;
    private Player bm;
    private PlayerData pd;
    // thread management
    private Object mPauseLock;
    private boolean mFinished;

    public PlayerVideo(HelperMetro hh, Player bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        mPauseLock = new Object();
        mFinished = false;
        mPaused = true;
        Log.d(TAG, "constructor");
        initPaint();
    }

    public void run() {
        initRun();
//        while (!mFinished) {
//            Log.d(TAG, "loop run");
////            if (!mPaused) {
//            doStep();
////                doWait(500, 0);
////            } else {
//                doWait(0, 0);
////            }
//        }
        while (!mFinished) {
            if (mPaused) {
                doWait(0, 0);
            } else {
                for (testCounter = 1; testCounter <= 8; testCounter++) {
                    Canvas canvas = bm.sh.lockCanvas();
                    canvas.drawColor(Color.BLACK);
                    canvas.drawText("" + testCounter, 10, 10, paintText);
                    canvas.drawCircle(30 + 20 * testCounter, 30, 20, paintHigh);
                    bm.sh.unlockCanvasAndPost(canvas);
                    doWait(1000, 0);
                }
                finishRun();
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


    private void doStep() {
        if (pd.bmBeat == null) {
            Log.d(TAG, "doStep: pd.bmBeat null");
            return;
        }
        Log.d(TAG, "doStep:" + pd.bmBeat.beatIndex);
        Canvas c = null;
        try {
            c = bm.sh.lockCanvas();
            doDraw(c);
        } finally {
            if (c != null) {
                bm.sh.unlockCanvasAndPost(c);
            }
        }
        mPaused = true;
    }

    private void doWait(long pauzeM, int pauzeN) {
        synchronized (mPauseLock) {
            while (mPaused) {
                try {
                    if (pauzeM > 0) {
                        Log.d(TAG, "pauze " + pauzeM);
                        mPauseLock.wait(pauzeM, pauzeN);
                    } else {
                        Log.d(TAG, "slaap");
                        mPauseLock.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }

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
        paintText.setTextSize(30);
    }

    private void bootCanvas() {
        Canvas canvas = bm.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        bm.sh.unlockCanvasAndPost(canvas);
    }

    private void doDraw(Canvas canvas) {
        int beat = pd.currentBeat;
        Log.d(TAG, "doDraw " + beat);
        Paint paintNu;
        switch (pd.bmBeat.beatState) {
            case 0:
                paintNu = paintHigh;
                break;
            default:
                paintNu = paintLow;
                break;
        }
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(20 + beat * 30, 20, 20, paintNu);
//        canvas.drawText("#" + beat, 22 + beat * 30, 20, paintText);
    }

    private void initRun() {
        Log.d(TAG, "start Runnable " + bm.videoThread.getPriority());
        bootCanvas();
    }

    private void finishRun() {
        Log.i(TAG, "finish Runnable");
    }
}