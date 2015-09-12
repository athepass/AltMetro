package info.thepass.altmetro.data;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

/**
 * Created by nl03192 on 10-9-2015.
 */
public class Pattern {
    public final static String TAG = "TrakPattern";
    private final static String KEYHASHPATTERN = "PThpat";
    private final static String KEYBARBEATS = "PTbt";
    private final static String KEYBARTIME = "PTtm";
    private final static String KEYBEATSTATE = "PTbs";
    private final static String KEYTITLE = "PTtt";
    private final static String KEYSUBS = "PTsub";
    public int hashPattern;
    public int barBeats;
    public int barTime;
    public int subs;
    public int[] beatState;
    public String title;

    public Pattern(HelperMetro h) {
        hashPattern = h.getHash();
        barBeats = 4;
        barTime = 4;
        subs = Keys.SUBDEFAULT;
        beatState = new int[Keys.MAXEMPHASIS];
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            if (i == 0)
                beatState[i] = Keys.SOUNDHIGH;
            else
                beatState[i] = Keys.SOUNDLOW;

        }
        title = "";
    }

    public Pattern(Bundle b) {
        hashPattern = b.getInt(KEYHASHPATTERN);
        barBeats = b.getInt(KEYBARBEATS);
        barTime = b.getInt(KEYBARTIME);
        beatState = b.getIntArray(KEYBEATSTATE);
        subs = b.getInt(KEYSUBS);
        title = b.getString(KEYTITLE);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(KEYHASHPATTERN, hashPattern);
        b.putInt(KEYBARBEATS, barBeats);
        b.putInt(KEYBARTIME, barTime);
        b.putInt(KEYSUBS, subs);
        b.putIntArray(KEYBEATSTATE, beatState);
        b.putString(KEYTITLE, title);
        return b;
    }

    public void clone(Pattern pattern, HelperMetro h) {
        hashPattern = h.getHash();
        barBeats = pattern.barBeats;
        barTime = pattern.barTime;
        subs = pattern.subs;
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            beatState[i] = pattern.beatState[i];
        }
        title = pattern.title;
    }

    public String statesToString() {
        String s = "";
        for (int i = 0; i < barBeats; i++) {
            if (i > 0)
                s += ".";
            s += String.valueOf(beatState[i]);
        }
        return s;
    }

    public String statesToStringText() {
        String s = "";
        for (int i = 0; i < barBeats; i++) {
            switch (beatState[i]) {
                case Keys.SOUNDHIGH:
                    s += "H";
                    break;
                case Keys.SOUNDLOW:
                    s += "L";
                    break;
                case Keys.SOUNDNONE:
                    s += "-";
                    break;
            }
        }
        return s;
    }

    public int getNextBeat(int currentBeat) {
        return (currentBeat < barBeats) ? currentBeat + 1 : 1;
    }

    @Override
    public String toString() {
        String s = "measure=" + barBeats + "/" + barTime
                + ((subs > 0) ? " subs=" + subs : "");
        s += " title=" + title;
        s += " emphasis=" + statesToString();
        return s;
    }

    public String toStringShort(HelperMetro h) {
        String s = "m:" + barBeats + "/" + barTime;
        s += " e:" + statesToString();
        s += (subs == 0) ? "" : " s:" + h.subPattern[h.getSubIndex(subs)];
        s += (title.equals("")) ? "" : " t:" + title;
        return s;
    }

    public String toStringShortText(HelperMetro h) {
        String s = "m:" + barBeats + "/" + barTime;
        s += " e:" + statesToStringText();
//		h.logD(TAG,"subs="+subs);
        s += (subs == 0) ? "" : " s:" + h.subPattern[h.getSubIndex(subs)];
        s += (title.equals("")) ? "" : " t:" + title;
        return s;
    }

    public String getTitle(String prefix) {
        if (title.length() == 0) {
            return "";
        } else {
            return prefix + title;
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYHASHPATTERN, hashPattern);
            json.put(KEYBARBEATS, barBeats);
            json.put(KEYBARTIME, barTime);
            json.put(KEYSUBS, subs);
            JSONArray statesArray = new JSONArray();
            for (int i = 0; i < barBeats; i++) {
                statesArray.put(beatState[i]);
            }
            json.put(KEYBEATSTATE, statesArray);
            json.put(KEYTITLE, title);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            hashPattern = json.getInt(KEYHASHPATTERN);
            barBeats = json.getInt(KEYBARBEATS);
            barTime = json.getInt(KEYBARTIME);
            subs = (json.has(KEYSUBS)) ? json.getInt(KEYSUBS) : Keys.SUBDEFAULT;
            JSONArray statesArray = json.getJSONArray(KEYBEATSTATE);
            for (int i = 0; i < barBeats; i++) {
                beatState[i] = statesArray.getInt(i);
            }
            title = json.getString(KEYTITLE);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    public void initBeatStates() {
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            if (i < barBeats) {
                beatState[i] = Keys.SOUNDLOW;
            } else {
                beatState[i] = Keys.SOUNDNONE;
            }
        }
        switch (barBeats) {
            case 1:
            case 2:
            case 3:
                beatState[0] = Keys.SOUNDHIGH;
                break;
            case 4:
                beatState[0] = Keys.SOUNDHIGH;
                break;
            case 5:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[3] = Keys.SOUNDHIGH;
                break;
            case 6:
                if (barTime == 8) {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[3] = Keys.SOUNDHIGH;
                } else {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[2] = Keys.SOUNDHIGH;
                    beatState[4] = Keys.SOUNDHIGH;
                }
                break;
            case 7:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[4] = Keys.SOUNDHIGH;
                break;
            case 8:
                if (barTime == 8) {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[2] = Keys.SOUNDHIGH;
                    beatState[4] = Keys.SOUNDHIGH;
                    beatState[6] = Keys.SOUNDHIGH;
                } else {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[4] = Keys.SOUNDHIGH;
                }
                break;
            case 9:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[3] = Keys.SOUNDHIGH;
                beatState[6] = Keys.SOUNDHIGH;
                break;
            case 10:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[5] = Keys.SOUNDHIGH;
                break;
            case 11:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[6] = Keys.SOUNDHIGH;
                break;
            case 12:
                if (barTime == 8) {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[3] = Keys.SOUNDHIGH;
                    beatState[6] = Keys.SOUNDHIGH;
                    beatState[9] = Keys.SOUNDHIGH;
                } else {
                    beatState[0] = Keys.SOUNDHIGH;
                    beatState[2] = Keys.SOUNDHIGH;
                    beatState[4] = Keys.SOUNDHIGH;
                    beatState[6] = Keys.SOUNDHIGH;
                    beatState[8] = Keys.SOUNDHIGH;
                    beatState[10] = Keys.SOUNDHIGH;
                }
                break;
            case 13:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[7] = Keys.SOUNDHIGH;
                break;
            case 14:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[7] = Keys.SOUNDHIGH;
                break;
            case 15:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[5] = Keys.SOUNDHIGH;
                beatState[10] = Keys.SOUNDHIGH;
                break;
            case 16:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[4] = Keys.SOUNDHIGH;
                beatState[8] = Keys.SOUNDHIGH;
                beatState[12] = Keys.SOUNDHIGH;
                break;
            case 17:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[9] = Keys.SOUNDHIGH;
                break;
            case 18:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[9] = Keys.SOUNDHIGH;
                break;
            case 19:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[10] = Keys.SOUNDHIGH;
                break;
            case 20:
                beatState[0] = Keys.SOUNDHIGH;
                beatState[4] = Keys.SOUNDHIGH;
                beatState[8] = Keys.SOUNDHIGH;
                beatState[12] = Keys.SOUNDHIGH;
                beatState[16] = Keys.SOUNDHIGH;
                break;
            default:
                for (int i = 0; i < barBeats; i++) {
                    beatState[i] = Keys.SOUNDHIGH;
                }
        }
    }
}