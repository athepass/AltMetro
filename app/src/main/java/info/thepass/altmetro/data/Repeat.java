package info.thepass.altmetro.data;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;

public class Repeat {
    public final static String TAG = "TrakRepeat";
    public final static String KEYINDEX = "RPidx";
    public final static String KEYHASH = "RPhpat";
    public final static String KEYTEMPO = "RPtmp";
    public final static String KEYCOUNT = "RPcnt";
    public int indexPattern;
    public int hashPattern;
    public int tempo;
    public int count;

    public Repeat(HelperMetro h) {
        indexPattern = 0;
        hashPattern = h.getHash();
        tempo = 90;
        count = 0;
    }

    public Repeat(Bundle b) {
        indexPattern = b.getInt(KEYINDEX);
        hashPattern = b.getInt(KEYHASH);
        tempo = b.getInt(KEYTEMPO);
        count = b.getInt(KEYCOUNT);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYINDEX, indexPattern);
            json.put(KEYHASH, hashPattern);
            json.put(KEYTEMPO, tempo);
            json.put(KEYCOUNT, count);

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
        } catch (JSONException e) {
            throw new RuntimeException("fromJson exception" + e.getMessage());
        }
    }

    public String toString() {
        return "i:" + indexPattern + ",h:" + hashPattern + ",c:" + count;
    }

    public String toString2(boolean showTempo, HelperMetro h) {
        String s = h.getString(R.string.label_count) + ": " + count;
        s += ((showTempo) ? " " + h.getString(R.string.label_tempo) + ": " + tempo : "");
        return s;
    }

    public String display(HelperMetro h, int index, String patDisplay, boolean showTempo) {
        String s = "";
        s += (index >= 0) ? "r" + (index + 1) + " " : "";
        s += (showTempo) ? h.getString(R.string.label_tempo) + ",  " + tempo : "";
        s += ((count == 0) ? h.getString(R.string.label_noend): h.getString1(R.string.label_repeats, String.valueOf(count)));
        s += ", " + patDisplay;
        return s;
    }
}