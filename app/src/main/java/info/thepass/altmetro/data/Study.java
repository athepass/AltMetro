package info.thepass.altmetro.data;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

public class Study {
    private final static String TAG = "TrakStudy";
    private final static String KEYTEMPOFROM = "STtpf";
    private final static String KEYTEMPOTO = "STtpt";
    private final static String KEYTEMPOINCREMENT = "STtpi";
    private final static String KEYROUNDS = "STrnd";
    private final static String KEYUSED = "STuse";
    public int tempoFrom;
    public int tempoTo;
    public int tempoIncrement;
    public int rounds;
    public boolean used;

    public Study() {
        used = false;
        tempoIncrement = 5;
        rounds = 4;
    }

    public Study(Bundle b) {
        tempoFrom = b.getInt(KEYTEMPOFROM);
        tempoTo = b.getInt(KEYTEMPOTO);
        tempoIncrement = b.getInt(KEYTEMPOINCREMENT);
        rounds = b.getInt(KEYROUNDS);
        used = b.getBoolean(KEYUSED);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(KEYTEMPOFROM, tempoFrom);
        b.putInt(KEYTEMPOTO, tempoTo);
        b.putInt(KEYTEMPOINCREMENT, tempoIncrement);
        b.putInt(KEYROUNDS, rounds);
        b.putBoolean(KEYUSED, used);
        return b;
    }

    public boolean isChanged(Study newSps) {
        if (newSps.tempoFrom != tempoFrom || newSps.tempoTo != tempoTo
                || newSps.tempoIncrement != tempoIncrement
                || newSps.rounds != rounds || newSps.used != used) {
            return true;
        }
        return false;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYTEMPOFROM, tempoFrom);
            json.put(KEYTEMPOTO, tempoTo);
            json.put(KEYTEMPOINCREMENT, tempoIncrement);
            json.put(KEYROUNDS, rounds);
            json.put(KEYUSED, used);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception " + e.getMessage(), e);
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            tempoFrom = json.getInt(KEYTEMPOFROM);
            tempoTo = json.getInt(KEYTEMPOTO);
            tempoIncrement = json.getInt(KEYTEMPOINCREMENT);
            rounds = json.getInt(KEYROUNDS);
            used = json.getBoolean(KEYUSED);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        String s = toStringKort()+ ((used) ? "used" : "not used");
        return s;
    }

    public String toStringKort() {
        String s = tempoFrom + ".." + tempoTo;
        s += "[+" + tempoIncrement;
        s += "]" + rounds + "*";
        return s;
    }
}
