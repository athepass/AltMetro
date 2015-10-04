package info.thepass.altmetro.player;

import android.graphics.Color;
import android.graphics.Paint;

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

    public boolean videoStarted = false;

    public Track bmTrack;
    public Repeat bmRepeat;
    public Beat bmBeat;
    public Pat bmPat;

    public int currentBeat;
    public int repeatBarcounter;
    public int trackBarcounter;
    public int iBeatList;

    public String[] subs;
    public Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);


    public PlayerData(HelperMetro h) {
        subs = h.getStringArray(R.array.sub_pattern);
        initPaint();
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
        paintText.setTextSize(30);
    }
}
