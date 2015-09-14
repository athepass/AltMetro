package info.thepass.altmetro.data;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import info.thepass.altmetro.tools.HelperMetro;

public class Order {
    public final static String TAG = "TrakOrder";
    private final static String KEYINDEX = "ORidx";
    private final static String KEYHASH = "ORhpat";
    private final static String KEYTEMPO = "ORtmp";
    private final static String KEYCOUNT = "ORcnt";
    public int index;
    public int hashPattern;
    public int tempo;
    public int count;

    public Order(HelperMetro h) {
        index = -1;
        hashPattern = h.getHash();
        tempo = 90;
        count = 0;
    }

    public Order(Bundle b) {
        index = b.getInt(KEYINDEX);
        hashPattern = b.getInt(KEYHASH);
        tempo = b.getInt(KEYTEMPO);
        count = b.getInt(KEYCOUNT);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYINDEX, index);
            json.put(KEYHASH, hashPattern);
            json.put(KEYTEMPO, tempo);
            json.put(KEYCOUNT, count);

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            index = json.getInt(KEYINDEX);
            hashPattern = json.getInt(KEYHASH);
            tempo = json.getInt(KEYTEMPO);
            count = json.getInt(KEYCOUNT);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    public String toString() {
        return "i:" + index + ",h:" + hashPattern + ",c:" + count;
    }

    public String toString2() {
        return "count:" + count;
    }
}