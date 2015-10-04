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

    // algemene data
    private HelperMetro h;
    private Player bm;
    private PlayerData pd;
    // video data
    private Canvas canvas;
    public boolean shConstructed = false;
    // thread management
    private Object mPauseLock;
    private boolean mFinished;

    public PlayerVideo(HelperMetro hh, Player bm) {
        h = hh;
        this.bm = bm;
        pd = bm.pd;
        Log.d(TAG, "constructor");
        initPaint();
        shConstructed = false;
    }

    public void run() {
        initRun();
        while (!mFinished) {
            doDraw();
            try {
                wait(500);
            } catch (Exception e) {}
        }
        finishRun();
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

    private void bootCanvas() {
        canvas = bm.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        bm.sh.unlockCanvasAndPost(canvas);
    }

    private boolean initCanvas() {
        if (bm.sh != null & !shConstructed) {
            Log.d(TAG, "initCanvas");
            shConstructed = true;
            try {
                wait(200);
            } catch (Exception e) {
            }
            canvas = bm.sh.lockCanvas();
            Log.d(TAG, "canvas==null");
            canvas.drawColor(Color.BLACK);
            canvas.drawCircle(20 + 10, 20, 10, paintHigh);
            bm.sh.unlockCanvasAndPost(canvas);
        }
        return shConstructed;
    }

    private void doDraw() {
        Log.d(TAG, "doDraw");
        canvas = bm.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(20 + pd.bmBeat * 10, 20, 20, paintHigh);
        canvas.drawText("#" + pd.bmBeat , 22 + pd.bmBeat * 10, 20, paintText);
        bm.sh.unlockCanvasAndPost(canvas);
        Log.d(TAG, "doDraw");
    }

    private void initRun() {
        Log.d(TAG, "start Runnable");
    }

    private void finishRun() {
        Log.i(TAG, "finish Runnable");
    }
}