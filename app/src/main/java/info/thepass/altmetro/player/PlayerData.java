package info.thepass.altmetro.player;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

/**
 * Created by nl03192 on 4-10-2015.
 */
public class PlayerData {
    public int playing;
    public boolean building;
    // klok performance
    public long timeStart1;
    public long timeStart2;
    public long timeStart3;
    public long timeBeat1;
    public long timeStop1;
    public long timeStop2;
    public long timeStop3;
    public long timeLayout1;
    public long timeBuild1;
    public long timeBuild2;
    public long timeBuild3;
    public long timeBuild4;

    public Track bmTrack;
    public Repeat bmRepeat;
    public Beat bmBeat;
    public Pat bmPat;

    public int currentBeat;
    public int repeatBarcounter;
    public int trackBarcounter;
    public int iBeatList;

    public String[] subs;

    public PlayerData(HelperMetro h) {
        subs = h.getStringArray(R.array.sub_pattern);
    }
}
