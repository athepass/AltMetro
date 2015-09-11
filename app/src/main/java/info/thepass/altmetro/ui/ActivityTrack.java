package info.thepass.altmetro.ui;

import android.app.Activity;
import android.os.Bundle;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.MetroData;
import info.thepass.altmetro.tools.HelperMetro;

public class ActivityTrack extends Activity implements TrackListFragment.OnTrackListListener {
    public final static String TAG = "ActivityTrack";
    public MetroData data;
    private HelperMetro h = null;
    private TrackFragment metroFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        h = new HelperMetro(this);
        h.logD(TAG, "onCreate");
        initMetroData();
        setContentView(R.layout.activity_metro);
        getActionBar().setLogo(R.mipmap.ic_launcher);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            metroFragment = (TrackFragment) getFragmentManager().findFragmentByTag(TrackFragment.TAG);
            if (metroFragment == null) {
                metroFragment = new TrackFragment();
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, metroFragment, TrackFragment.TAG).commit();
        }
    }

    private void initMetroData() {
        h.logD(TAG, "InitMetroData start");
        data = new MetroData(h);
    }

    public void onTrackSelected(int itemSelected) {
        h.logD(TAG, "sel=" + itemSelected);
        data.trackSelected = itemSelected;
        if (metroFragment != null) {
            metroFragment.setSelected(itemSelected);
        }
    }
}
