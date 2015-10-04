package info.thepass.altmetro.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.player.BarManager;
import info.thepass.altmetro.player.Beat;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class Repeat {
    public final static String TAG = "TrakRepeat";
    public final static int NOHASH = -1;
    public final static String KEYINDEX = "RPidx";
    public final static String KEYHASH = "RPhpat";
    public final static String KEYTEMPO = "RPtmp";
    public final static String KEYCOUNT = "RPcnt";
    public final static String KEYNOEND = "RPnen";
    public int indexPattern;
    public int hashPattern;
    public int tempo;
    public int barCount;
    public boolean noEnd;

    public ArrayList<Beat> beatList;

    public int iBar;
    public int iBeat;
    public int repeatCounter;

    public Repeat() {
        indexPattern = 0;
        hashPattern = NOHASH;
        tempo = 90;
        barCount = 1;
        noEnd = true;

        beatList = new ArrayList<Beat>();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYINDEX, indexPattern);
            json.put(KEYHASH, hashPattern);
            json.put(KEYTEMPO, tempo);
            json.put(KEYCOUNT, barCount);
            json.put(KEYNOEND, noEnd);

        } catch (JSONException e) {
            throw new RuntimeException("toJson exception" + e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            indexPattern = json.getInt(KEYINDEX);
            hashPattern = json.getInt(KEYHASH);
            tempo = json.getInt(KEYTEMPO);
            barCount = json.getInt(KEYCOUNT);
            noEnd = json.getBoolean(KEYNOEND);
        } catch (JSONException e) {
            throw new RuntimeException("fromJson exception" + e.getMessage());
        }
    }

    public String display(HelperMetro h, int index, String patDisplay, boolean showTempo) {
        String s = "";
        s += (index >= 0) ? "r" + (index + 1) + " " : "";
        s += (showTempo) ? h.getString(R.string.label_tempo) + " " + tempo + ", " : "";
        s += ((noEnd) ? h.getString(R.string.label_noend) : h.getString1(R.string.label_repeatTimes, String.valueOf(barCount)));
        s += (patDisplay.length() > 0) ? ", " + patDisplay : "";
        return s;
    }

    public void buildBeatList(BarManager bm, int indexRepeat, HelperMetro h) {
        repeatCounter = 0;
        beatList.clear();
        buildBeatBar(bm, indexRepeat, h);
    }

    private void buildBeatBar(BarManager bm, int idxRepeat, HelperMetro h) {
        boolean soundFirstBeat = h.prefs.getBoolean(Keys.PREFFIRSTBEAT, false);
        Pat pat = bm.trackFragment.track.pats.get(this.indexPattern);
        for (iBeat = 0; iBeat < pat.patBeats; iBeat++) {
            Beat beat = new Beat(soundFirstBeat);
            beatList.add(beat);
            beat.repeatCount = (noEnd) ? 0 : barCount;
            beat.barIndex = bm.pd.repeatBarcounter;
            beat.beats = pat.patBeats;
            beat.beatIndex = iBeat;
            beat.beatNext = (iBeat < pat.patBeats - 1) ? iBeat + 1 : 0;
            beat.beatState = pat.patBeatState[iBeat];
            beat.beatStateNext = (iBeat < pat.patBeats - 1) ? pat.patBeatState[iBeat + 1] : pat.patBeatState[0];
            beat.sub = pat.patSubs;
            beat.tempo = this.tempo;
            beat.percentage = bm.trackFragment.track.study.practice;
            beat.tempoCalc = Math.round((beat.tempo * beat.percentage) / 100f);
            beat.info = "r" + (beat.repeatIndex + 1)
                    + " bar " + (beat.repeatBar + 1)
                    + " beat " + (beat.beatIndex + 1)
                    + " tempo " + beat.tempo
                    + ((beat.percentage == 100) ? "" : " " + beat.percentage + "%=" + beat.tempoCalc);
        }
    }

    public void buildSound(BarManager bm) {
        for (int i = 0; i < beatList.size(); i++) {
            Beat beat = beatList.get(i);
            beat.buildSound();
        }
        bm.trackFragment.track.soundDump(bm);
    }
}