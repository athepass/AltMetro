package info.thepass.altmetro.data;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

/**
 * Created by nl03192 on 10-9-2015.
 */
public class Pat {
    public final static String TAG = "TrakPat";
    private final static String KEYHASHPATTERN = "PThpat";
    private final static String KEYBARBEATS = "PTbt";
    private final static String KEYBARTIME = "PTtm";
    private final static String KEYBEATSTATE = "PTbs";
    private final static String KEYTITLE = "PTtt";
    private final static String KEYSUBS = "PTsub";
    public int patHash;
    public int patBeats;
    public int patTime;
    public int patSubs;
    public int[] patBeatState;
    public String patTitle;

    public Pat(HelperMetro h) {
        patHash = h.getHash();
        patBeats = 4;
        patTime = 4;
        patSubs = Keys.SUBDEFAULT;
        patSubs = 0;
        patBeatState = new int[Keys.MAXEMPHASIS];
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            if (i == 0)
                patBeatState[i] = Keys.SOUNDHIGH;
            else
                patBeatState[i] = Keys.SOUNDLOW;

        }
        patTitle = "";
    }

    public Pat(Bundle b) {
        patHash = b.getInt(KEYHASHPATTERN);
        patBeats = b.getInt(KEYBARBEATS);
        patTime = b.getInt(KEYBARTIME);
        patBeatState = b.getIntArray(KEYBEATSTATE);
        patSubs = b.getInt(KEYSUBS);
        patTitle = b.getString(KEYTITLE);
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt(KEYHASHPATTERN, patHash);
        b.putInt(KEYBARBEATS, patBeats);
        b.putInt(KEYBARTIME, patTime);
        b.putInt(KEYSUBS, patSubs);
        b.putIntArray(KEYBEATSTATE, patBeatState);
        b.putString(KEYTITLE, patTitle);
        return b;
    }

    public void clone(Pat pattern, HelperMetro h) {
        patHash = h.getHash();
        patBeats = pattern.patBeats;
        patTime = pattern.patTime;
        patSubs = pattern.patSubs;
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            patBeatState[i] = pattern.patBeatState[i];
        }
        patTitle = pattern.patTitle;
    }

    public String statesToString() {
        String s = "";
        for (int i = 0; i < patBeats; i++) {
            if (i > 0)
                s += ".";
            s += String.valueOf(patBeatState[i]);
        }
        return s;
    }

    public String statesToStringText() {
        String s = "";
        for (int i = 0; i < patBeats; i++) {
            switch (patBeatState[i]) {
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
        return (currentBeat < patBeats) ? currentBeat + 1 : 1;
    }

    @Override
    public String toString() {
        String s = "measure=" + patBeats + "/" + patTime
                + ((patSubs > 0) ? " subs=" + patSubs : "");
        s += " Title=" + patTitle;
        s += " emphasis=" + statesToString();
        return s;
    }

    public String toString2(HelperMetro h) {
        String s = "measure: " + patBeats + "/" + patTime
                + ((patSubs > 0) ? " subs=" + h.subPattern[patSubs] : "");
        s += " Title=" + patTitle;
        return s;
    }

    public String toStringShort(HelperMetro h) {
        String s = "m:" + patBeats + "/" + patTime;
        s += " e:" + statesToString();
        s += (patSubs == 0) ? "" : " s:" + h.subPattern[patSubs];
        s += (patTitle.equals("")) ? "" : " t:" + patTitle;
        return s;
    }

    public String toStringShortText(HelperMetro h) {
        String s = "m:" + patBeats + "/" + patTime;
        s += " e:" + statesToStringText();
        s += (patSubs == 0) ? "" : " s:" + h.subPattern[h.getSubIndex(patSubs)];
        s += (patTitle.equals("")) ? "" : " t:" + patTitle;
        return s;
    }

    public String display(HelperMetro h, int index, boolean showEmphasis) {
        String s = "";
        s += (index >= 0) ? "p" + (index + 1) + ":" : "";
        s += patBeats + "/" + patTime;
        s += (showEmphasis) ? " " + statesToStringText() : "";
        s += (patSubs > 0) ? " " + h.getString(R.string.label_sub2) + ":" + h.subPattern[patSubs] : "";
        s += getTitle(" ");
        return s;
    }

    public String getTitle(String prefix) {
        if (patTitle.length() == 0) {
            return "";
        } else {
            return prefix + patTitle;
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYHASHPATTERN, patHash);
            json.put(KEYBARBEATS, patBeats);
            json.put(KEYBARTIME, patTime);
            json.put(KEYSUBS, patSubs);
            JSONArray statesArray = new JSONArray();
            for (int i = 0; i < patBeats; i++) {
                statesArray.put(patBeatState[i]);
            }
            json.put(KEYBEATSTATE, statesArray);
            json.put(KEYTITLE, patTitle);
        } catch (JSONException e) {
            throw new RuntimeException( "toJson exception" + e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            patHash = json.getInt(KEYHASHPATTERN);
            patBeats = json.getInt(KEYBARBEATS);
            patTime = json.getInt(KEYBARTIME);
            patSubs = (json.has(KEYSUBS)) ? json.getInt(KEYSUBS) : Keys.SUBDEFAULT;
            JSONArray statesArray = json.getJSONArray(KEYBEATSTATE);
            for (int i = 0; i < patBeats; i++) {
                patBeatState[i] = statesArray.getInt(i);
            }
            patTitle = json.getString(KEYTITLE);
        } catch (JSONException e) {
            throw new RuntimeException("toJson exception" + e.getMessage());
        }
    }

    public void initBeatStates() {
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            if (i < patBeats) {
                patBeatState[i] = Keys.SOUNDLOW;
            } else {
                patBeatState[i] = Keys.SOUNDNONE;
            }
        }
        switch (patBeats) {
            case 1:
            case 2:
            case 3:
                patBeatState[0] = Keys.SOUNDHIGH;
                break;
            case 4:
                patBeatState[0] = Keys.SOUNDHIGH;
                break;
            case 5:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[3] = Keys.SOUNDHIGH;
                break;
            case 6:
                if (patTime == 8) {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[3] = Keys.SOUNDHIGH;
                } else {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[2] = Keys.SOUNDHIGH;
                    patBeatState[4] = Keys.SOUNDHIGH;
                }
                break;
            case 7:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[4] = Keys.SOUNDHIGH;
                break;
            case 8:
                if (patTime == 8) {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[2] = Keys.SOUNDHIGH;
                    patBeatState[4] = Keys.SOUNDHIGH;
                    patBeatState[6] = Keys.SOUNDHIGH;
                } else {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[4] = Keys.SOUNDHIGH;
                }
                break;
            case 9:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[3] = Keys.SOUNDHIGH;
                patBeatState[6] = Keys.SOUNDHIGH;
                break;
            case 10:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[5] = Keys.SOUNDHIGH;
                break;
            case 11:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[6] = Keys.SOUNDHIGH;
                break;
            case 12:
                if (patTime == 8) {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[3] = Keys.SOUNDHIGH;
                    patBeatState[6] = Keys.SOUNDHIGH;
                    patBeatState[9] = Keys.SOUNDHIGH;
                } else {
                    patBeatState[0] = Keys.SOUNDHIGH;
                    patBeatState[2] = Keys.SOUNDHIGH;
                    patBeatState[4] = Keys.SOUNDHIGH;
                    patBeatState[6] = Keys.SOUNDHIGH;
                    patBeatState[8] = Keys.SOUNDHIGH;
                    patBeatState[10] = Keys.SOUNDHIGH;
                }
                break;
            case 13:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[7] = Keys.SOUNDHIGH;
                break;
            case 14:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[7] = Keys.SOUNDHIGH;
                break;
            case 15:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[5] = Keys.SOUNDHIGH;
                patBeatState[10] = Keys.SOUNDHIGH;
                break;
            case 16:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[4] = Keys.SOUNDHIGH;
                patBeatState[8] = Keys.SOUNDHIGH;
                patBeatState[12] = Keys.SOUNDHIGH;
                break;
            case 17:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[9] = Keys.SOUNDHIGH;
                break;
            case 18:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[9] = Keys.SOUNDHIGH;
                break;
            case 19:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[10] = Keys.SOUNDHIGH;
                break;
            case 20:
                patBeatState[0] = Keys.SOUNDHIGH;
                patBeatState[4] = Keys.SOUNDHIGH;
                patBeatState[8] = Keys.SOUNDHIGH;
                patBeatState[12] = Keys.SOUNDHIGH;
                patBeatState[16] = Keys.SOUNDHIGH;
                break;
            default:
                for (int i = 0; i < patBeats; i++) {
                    patBeatState[i] = Keys.SOUNDHIGH;
                }
        }
    }
}