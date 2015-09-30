package info.thepass.altmetro.aaaUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.Sound.BeatManagerFragment;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.tools.Keys;

public class EmphasisView extends View {
    private final static String TAG = "trak:EmphasisView";
    public BeatManagerFragment bm;
    public Pat pat;
    public int beat;
    ArrayList<Point> points;
    Paint paintHigh;
    Paint paintLow;
    Paint paintNone;
    Paint paintBackground;
    Paint paintNu;
    Paint paintText;
    int radius;
    float widthDp;
    float heightDp;
    private Context context;
    private DisplayMetrics metrics;

    public EmphasisView(Context context) {
        super(context);
        this.context = context;
        initPaint();
        pat = null;
    }

    public static float dpFromPx(DisplayMetrics metrics, final float px) {
        return px / metrics.density;
    }

    public static float pxFromDp(DisplayMetrics metrics, final float dp) {
        return dp * metrics.density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // blank scherm.
        canvas.drawPaint(paintBackground);

        if (pat != null) {
            // vul de standaard punten
            for (int i = 0; i < pat.patBeats; i++) {
                Point p = points.get(i);
                switch (pat.patBeatState[i]) {
                    case Keys.SOUNDHIGH:
                        paintNu = paintHigh;
                        break;
                    case Keys.SOUNDLOW:
                        paintNu = paintHigh;
                        break;
                    case Keys.SOUNDNONE:
                        paintNu = paintHigh;
                        break;
                    default:
                        throw new RuntimeException("beatstate onjuist " + pat.patBeatState[i]);
                }
                canvas.drawCircle(p.x, p.y, radius, paintNu);
            }

            Point p = points.get(beat);
            canvas.drawCircle(p.x, p.y, radius, paintText);
            canvas.drawText("" + (beat + 1), p.x - 10, p.y - 10, paintHigh);
            Long timeDraw = bm.getNanoTime();
            bm.delayCounter++;
            bm.delaySum += (timeDraw - bm.timeBeat1) / 1000000f;
            Log.d(TAG, "onDraw beat:" + beat + " x:" + p.x + " y:" + p.y + " r" + radius
                    + " time:" + bm.deltaTime(bm.timeBeat1, timeDraw)
                    + "|" + Math.round(bm.delaySum / bm.delayCounter));
        }
    }

    private void initPaint() {
        paintBackground = new Paint();
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setColor(Color.TRANSPARENT);

        paintHigh = new Paint();
        paintHigh.setStyle(Paint.Style.FILL);
        paintHigh.setColor(context.getResources().getColor(R.color.color_emphasis_high));
        paintLow = new Paint();
        paintLow.setStyle(Paint.Style.FILL);
        paintLow.setColor(context.getResources().getColor(R.color.color_emphasis_low));
        paintNone = new Paint();
        paintNone.setStyle(Paint.Style.FILL);
        paintNone.setColor(context.getResources().getColor(R.color.color_emphasis_none));

        paintText = new Paint();
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.WHITE);

    }

    public void getInfo() {
        metrics = context.getResources().getDisplayMetrics();
        String info = "====== Screen metrics =======" +
                "\nscreen width: " + metrics.widthPixels + " - " + dpFromPx(metrics, metrics.widthPixels);
        info += "\nscreen height: " + metrics.heightPixels + " - " + dpFromPx(metrics, metrics.heightPixels);
        widthDp = dpFromPx(metrics, this.getWidth());
        heightDp = dpFromPx(metrics, this.getHeight());
        info += "\nview width: " + this.getWidth() + " - " + dpFromPx(metrics, widthDp);
        info += "\nview height: " + this.getHeight() + " - " + dpFromPx(metrics, heightDp);
        Log.d(TAG, info);
    }

    public void initPoint(Pat pat) {
        this.pat = pat;

        if (widthDp == 0) {
            getInfo();
        }
        points = new ArrayList<Point>();
        int factorDP = Math.round(pxFromDp(metrics, 30f));
        radius = Math.round(factorDP * 0.45f);
        for (int i = 0; i < pat.patBeats; i++) {
            Point p = new Point();
            points.add(p);
            if (pat.patBeats > 10) {
                p.x = factorDP + i * factorDP;
                p.y = (int) getHeight() / 2;
            } else {
                if (i <= 10) {
                    if (pat.patBeats <= 10) {
                        p.x = factorDP + i * factorDP;
                        p.y = (int) getHeight() / 3;
                    } else {
                        p.y = (int) 2 * getHeight() / 3;
                    }
                }
            }
        }
    }
}
