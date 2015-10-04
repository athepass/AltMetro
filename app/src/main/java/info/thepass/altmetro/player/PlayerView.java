package info.thepass.altmetro.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PlayerView extends SurfaceView
        implements SurfaceHolder.Callback {
    public final static String TAG = "trak:PlayerView";
    public BarManager bm;


    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "constructor");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        bm.pd.videoStarted = true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        bm.pd.svwWidth = width;
        bm.pd.svwHeight = height;
        bm.pd.svwFormat = format;
        bm.pd.berekenPatternDisplay();
        Log.d(TAG, "surfaceChanged w h f r"
                + width + " " + height + " " + format + " " + bm.pd.svwRadius);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        bm.pd.videoStarted = false;
        Log.d(TAG, "surfaceDestroyed");
    }
}
