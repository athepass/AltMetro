package info.thepass.altmetro.aaaUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;

public class ActivityTrack extends Activity {
    public final static String TAG = "ActivityTrak";
    public TrackData trackData;
    private HelperMetro h = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        h = new HelperMetro(this);
        h.logD(TAG, "onCreate");
        initMetroData();
        setContentView(R.layout.activity_track);
        getActionBar().setLogo(R.mipmap.ic_launcher);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            TrackFragment trackFragment = (TrackFragment) getFragmentManager().findFragmentByTag(TrackFragment.TAG);
            if (trackFragment == null) {
                trackFragment = new TrackFragment();
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, trackFragment, TrackFragment.TAG).commit();
        }
    }

    private void initMetroData() {
        trackData = new TrackData(h);
        h.logD(TAG, "InitMetroData start");
    }

    public void doRestart() {
        h.logD(TAG,"doRestart");
        Intent intent = new Intent();
        intent.setClass(this, ActivityTrack.class);
        HelperMetro h = new HelperMetro(this);
        h.logI(TAG,h.getString(R.string.error_restarting));
        startActivity(intent);
        finish();
    }
}
