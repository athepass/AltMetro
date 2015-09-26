package info.thepass.altmetro.data;

import org.json.JSONException;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.Sound.Beat;
import info.thepass.altmetro.Sound.BeatManagerFragment;
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
    public int count;
    public boolean noEnd;

    public int iRepeat;
    public int iBeat;
    public int repeatCounter;

    public Repeat() {
        indexPattern = 0;
        hashPattern = NOHASH;
        tempo = 90;
        count = 1;
        noEnd = true;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYINDEX, indexPattern);
            json.put(KEYHASH, hashPattern);
            json.put(KEYTEMPO, tempo);
            json.put(KEYCOUNT, count);
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
            count = json.getInt(KEYCOUNT);
            noEnd = json.getBoolean(KEYNOEND);
        } catch (JSONException e) {
            throw new RuntimeException("fromJson exception" + e.getMessage());
        }
    }

    public String display(HelperMetro h, int index, String patDisplay, boolean showTempo) {
        String s = "";
        s += (index >= 0) ? "r" + (index + 1) + " " : "";
        s += (showTempo) ? h.getString(R.string.label_tempo) + " " + tempo + ", " : "";
        s += ((noEnd) ? h.getString(R.string.label_noend) : h.getString1(R.string.label_repeatTimes, String.valueOf(count)));
        s += (patDisplay.length() > 0) ? ", " + patDisplay : "";
        return s;
    }

    public void buildBeat(BeatManagerFragment bm, HelperMetro h) {
        repeatCounter = 0;
        if (noEnd) {
            iRepeat = 0;
            buildBeatBar(bm, h);
        } else {
            for (iRepeat = 0; iRepeat < count; iRepeat++) {
                bm.barCounter++;
                repeatCounter++;
                buildBeatBar(bm, h);
            }
        }
    }

    private void buildBeatBar(BeatManagerFragment bm, HelperMetro h) {
        boolean soundFirstBeat = h.prefs.getBoolean(Keys.PREFFIRSTBEAT, false);
        Pat pat = bm.track.pats.get(this.indexPattern);
        for (iBeat = 0; iBeat < pat.patBeats; iBeat++) {
            Beat beat = new Beat(soundFirstBeat);
            bm.beatList.add(beat);
            beat.repeatCount = (noEnd) ? 0 : count;
            beat.repeatIndex = repeatCounter;
            beat.barIndex = bm.barCounter;
            if (iBeat == pat.patBeats - 1) {
                beat.barNext = (noEnd) ? 1 - pat.patBeats : 1;
            } else {
                beat.barNext = 1;
            }
            beat.beats = pat.patBeats;
            beat.beatIndex = iBeat + 1;
            beat.beatState = pat.patBeatState[iBeat];
            beat.sub = pat.patSubs;
            beat.tempo = this.tempo;
            beat.practice = bm.track.study.practice;
            beat.tempoCalc = Math.round((beat.tempo * 100f) / beat.practice);
        }
    }
}