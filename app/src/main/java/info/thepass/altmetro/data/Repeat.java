package info.thepass.altmetro.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.Audio.Beat;
import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;

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
        s += (showTempo) ? h.getString(R.string.label_tempo) + ",  " + tempo : "";
        s += ((noEnd) ? h.getString(R.string.label_noend): h.getString1(R.string.label_repeatTimes, String.valueOf(count)));
        s += ", " + patDisplay;
        return s;
    }

    public void buildBeatList(ArrayList<Beat> beatList) {
    }

}