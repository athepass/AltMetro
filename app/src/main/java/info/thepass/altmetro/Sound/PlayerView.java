package info.thepass.altmetro.Sound;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PlayerView extends SurfaceView
        implements SurfaceHolder.Callback {
    public final static String TAG = "trak:SurfaceView";
    private SurfaceHolder sh;

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "constructor");
        sh = getHolder();
        sh.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d(TAG, "surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }
}
