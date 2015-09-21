package info.thepass.altmetro.data;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

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
    public int tempoFrom;
    public int tempoTo;
    public int tempoIncrement;
    public int rounds;
    public int practice;
    public boolean used;

    public Study() {
        used = false;
        setInitial();
    }

    public Study(Bundle b) {
        tempoFrom = b.getInt(KEYTEMPOFROM);
        tempoTo = b.getInt(KEYTEMPOTO);
        tempoIncrement = b.getInt(KEYTEMPOINCREMENT);
        rounds = b.getInt(KEYROUNDS);
        used = b.getBoolean(KEYUSED);
        practice = b.getInt(KEYPRACTICE, 100);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(KEYTEMPOFROM, tempoFrom);
        b.putInt(KEYTEMPOTO, tempoTo);
        b.putInt(KEYTEMPOINCREMENT, tempoIncrement);
        b.putInt(KEYROUNDS, rounds);
        b.putBoolean(KEYUSED, used);
        b.putInt(KEYPRACTICE, practice);
        return b;
    }

    public boolean isChanged(Study newSps) {
        return newSps.tempoFrom != tempoFrom || newSps.tempoTo != tempoTo
                || newSps.tempoIncrement != tempoIncrement
                || newSps.rounds != rounds || newSps.used != used;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYTEMPOFROM, tempoFrom);
            json.put(KEYTEMPOTO, tempoTo);
            json.put(KEYTEMPOINCREMENT, tempoIncrement);
            json.put(KEYROUNDS, rounds);
            json.put(KEYUSED, used);
            json.put(KEYPRACTICE, practice);
        } catch (JSONException e) {
            throw new RuntimeException("FromJson exception" + e.getMessage());
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
            s += h.getString(R.string.label_tempo_from) + " " + tempoFrom + " ";
            s += h.getString(R.string.label_tempo_to) + " " + tempoTo + " ";
            s += h.getString(R.string.label_tempo_increment) + " " + tempoIncrement + " ";
            s += h.getString(R.string.label_tempo_rondes) + " " + rounds;
            s = s.toLowerCase();
            return s;
        }
    }

    public void setInitial() {
        tempoIncrement = -1;
        rounds = -1;
        practice = 100;
    }

    public boolean isInitial() {
        return (rounds==-1);
    }

    public String toStringKort() {
        String s = tempoFrom + ".." + tempoTo;
        s += "[+" + tempoIncrement;
        s += "]" + rounds + "*";
        return s;
    }
}
