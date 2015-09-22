package info.thepass.altmetro.Audio;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.ui.ArrayBrowserListFragment;

public class BeatManager {
    public final static String TAG = "Trak:BeatManager";
    private HelperMetro h;
    String[] subs;
    public ArrayList<Beat> beatList;
    public Track track;
    public TrackData trackData;
    public LinearLayout llRoot;

    public BeatManager(HelperMetro hh) {
        h = hh;
        subs = h.getStringArray(R.array.sub_pattern);
        beatList = new ArrayList<Beat>();
    }

    public void build(Track track, ArrayList<Pat> pats) {
        beatList.clear();
        for (int iRep = 0;  iRep<track.repeats.size();iRep++) {
            Repeat repeat = track.repeats.get(iRep);
        }
    }

    public void startPlayer() {

    }

    public void stopPlayer() {
        build(track, trackData.pats);
        ArrayList<String> rows = display();

        ArrayBrowserListFragment beatFragment = new ArrayBrowserListFragment();

        Bundle b = new Bundle();
        b.putString(ArrayBrowserListFragment.TITEL, "beatManager lijst");
        b.putStringArrayList(ArrayBrowserListFragment.ROW, rows);
        String s = "====== Dump beats =====";
        for (int i = 0; i < rows.size(); i++) {
            s += "\n" + rows.get(i);
        }
        h.logD(TAG, s);
//        beatFragment.setArguments(b);
//
//        FragmentTransaction transaction = getFragmentManager()
//                .beginTransaction();
//        transaction.replace(R.id.fragment_container, beatFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();

    }
    public ArrayList<String> display() {
        ArrayList<String> rows = new ArrayList<String >();
        for (int i=0;i<beatList.size();i++) {
            Beat beat = beatList.get(i);
            rows.add(beat.display(i+1,subs));
        }        return rows;
    }
}
