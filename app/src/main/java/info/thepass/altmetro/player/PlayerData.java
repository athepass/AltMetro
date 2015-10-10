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
    private final static String TAG = "Trak:PlayerData";
    private HelperMetro h;

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
    public long timeLayout2;
    public long timeBuild1;
    public long timeBuild2;
    public long timeBuild3;
    public long timeBuild4;

    public boolean videoStarted = false;

    public Track bmTrack = null;
    public Repeat bmRepeat = null;
    public Beat bmBeat = null;
    public Pat bmPat = null;

    public int currentBeat;
    public int nextBeat;
    public int repeatCounter;
    public int repeatBarCounter;
    public int trackBarCounter;
    public int beatListCounter;

    public String playerInfo;

    public int beatDelay = 200 * 8;
    public boolean svwReady = false;
    public boolean svwEersteKeer;
    public int svwWidth;
    public int svwHeight;
    public int svwFormat;
    public int svwAfstandV = 10;
    public int svwAfstandH = 10;
    public int svwRadius = -1;
    public int svwPatHash = -1;

    public String[] subs;
    public Paint paintBeat = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintHigh = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintLow = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintNone = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);


    public PlayerData(HelperMetro h) {
        this.h = h;
        h.logD(TAG, "constructor");
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

        paintBeat.setColor(Color.GREEN);
        paintBeat.setStyle(Paint.Style.FILL);

        paintText.setColor(Color.BLUE);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(50);
    }

    public void berekenPatternDisplay() {
        svwReady = false;
        // pattern nog niet bekend.
        if (bmPat == null)
            return;
        boolean plus10 = (bmPat.patBeats > 10);
        int maxH = -1;
        int maxW = -1;
        if (plus10) {
            maxH = (int) ((svwHeight - 6) / 2) / 2;
            maxW = (int) ((svwWidth - (11 * svwAfstandH)) / bmPat.patBeats) / 2;
        } else {
            maxH = (int) (svwHeight - 4) / 2;
            maxW = (int) ((svwWidth - ((bmPat.patBeats + 1) * svwAfstandH)) / bmPat.patBeats) / 2;
        }
        svwRadius = (maxH < maxW) ? maxH : maxW;
        svwReady = true;
        svwEersteKeer = true;
        h.logD(TAG, "berekenPattern r v h " + svwRadius + " " + svwAfstandH + "." + svwAfstandV);
    }
}
