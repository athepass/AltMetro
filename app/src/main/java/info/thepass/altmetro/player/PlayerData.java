package info.thepass.altmetro.player;

import android.view.SurfaceHolder;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

/**
 * Created by nl03192 on 4-10-2015.
 */
public class PlayerData {
    public boolean mPaused;
    public boolean mPlaying;

    public SurfaceHolder sh = null;
    public boolean shConstructed = false;

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

    //
    public Track bmTrack;
    public Repeat bmRepeat;
    public Pat bmPat;
    public int barCounter;
    public String[] subs;
    public int iBeatList;

    public PlayerData(HelperMetro h) {
        subs = h.getStringArray(R.array.sub_pattern);
    }
}
