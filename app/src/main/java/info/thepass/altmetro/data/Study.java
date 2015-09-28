package info.thepass.altmetro.data;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;

public class Study {
    private final static String TAG = "TrakStudy";
    private final static String KEYTEMPOFROM = "STtpf";
    private final static String KEYTEMPOTO = "STtpt";
    private final static String KEYTEMPOINCREMENT = "STtpi";
    private final static String KEYROUNDS = "STrnd";
    private final static String KEYUSED = "STuse";
    private final static String KEYPRACTICE = "STprac";
    public int percentageFrom;
    public int percentageTo;
    public int percentageIncr;
    public int times;
    public int practice;
    public boolean used;

    private ArrayList<Repeat> repeats;

    public Study() {
        used = false;
        percentageFrom = 75;
        percentageTo = 100;
        percentageIncr = 5;
        times = 4;
        practice = 100;
        repeats = new ArrayList<Repeat> ();
    }

    public Study(Bundle b) {
        percentageFrom = b.getInt(KEYTEMPOFROM);
        percentageTo = b.getInt(KEYTEMPOTO);
        percentageIncr = b.getInt(KEYTEMPOINCREMENT);
        times = b.getInt(KEYROUNDS);
        used = b.getBoolean(KEYUSED);
        practice = b.getInt(KEYPRACTICE, 100);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(KEYTEMPOFROM, percentageFrom);
        b.putInt(KEYTEMPOTO, percentageTo);
        b.putInt(KEYTEMPOINCREMENT, percentageIncr);
        b.putInt(KEYROUNDS, times);
        b.putBoolean(KEYUSED, used);
        b.putInt(KEYPRACTICE, practice);
        return b;
    }

    public boolean isChanged(Study newSps) {
        return newSps.percentageFrom != percentageFrom || newSps.percentageTo != percentageTo
                || newSps.percentageIncr != percentageIncr
                || newSps.times != times || newSps.used != used;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYTEMPOFROM, percentageFrom);
            json.put(KEYTEMPOTO, percentageTo);
            json.put(KEYTEMPOINCREMENT, percentageIncr);
            json.put(KEYROUNDS, times);
            json.put(KEYUSED, used);
            json.put(KEYPRACTICE, practice);
        } catch (JSONException e) {
            throw new RuntimeException("FromJson exception" + e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            percentageFrom = json.getInt(KEYTEMPOFROM);
            percentageTo = json.getInt(KEYTEMPOTO);
            percentageIncr = json.getInt(KEYTEMPOINCREMENT);
            times = json.getInt(KEYROUNDS);
            used = json.getBoolean(KEYUSED);
            if (json.has(KEYPRACTICE))
                practice = json.getInt(KEYPRACTICE);
        } catch (JSONException e) {
            throw new RuntimeException("toJson exception" + e.getMessage());
        }
    }

    @Override
    public String toString() {
        String s = toStringKort() + ((used) ? "used" : "not used");
        return s;
    }

    public String display(HelperMetro h) {
        if (!used) {
            return h.getString(R.string.label_study_off);
        } else {
            String s = h.getString(R.string.label_study_on) +": ";
            s += percentageFrom + "% ";
            s += h.getString(R.string.label_tempo_to) + " " + percentageTo + "% ";
            s += h.getString(R.string.label_tempo_increment) + " " + percentageIncr + "% ";
            s += h.getString1(R.string.label_repeatTimes, ""+ times);
            s = s.toLowerCase();
            return s;
        }
    }

    public String toStringKort() {
        String s = percentageFrom + ".." + percentageTo;
        s += "[+" + percentageIncr;
        s += "]" + times + "*";
        return s;
    }
}
