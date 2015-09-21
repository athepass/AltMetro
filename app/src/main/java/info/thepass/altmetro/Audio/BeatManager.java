package info.thepass.altmetro.Audio;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class BeatManager {
    public final static String TAG = "Trak:BeatManager";
    private HelperMetro h;
    String[] subs;
    public ArrayList<Beat> beatList;

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

    public ArrayList<String> display() {
        ArrayList<String> rows = new ArrayList<String >();
        for (int i=0;i<beatList.size();i++) {
            Beat beat = beatList.get(i);
            rows.add(beat.display(i+1,subs));
        }        return rows;
    }
}
