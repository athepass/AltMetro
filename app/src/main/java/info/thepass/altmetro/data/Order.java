package info.thepass.altmetro.data;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import info.thepass.altmetro.tools.HelperMetro;

public class Order {
    public final static String TAG = "Track";
    private final static String KEYINDEX = "ORidx";
    private final static String KEYHASH = "ORhash";
    private final static String KEYCOUNT = "ORcnt";
    public int index;
    public int hash;
    public int count;

    public Order(HelperMetro h) {
        index = -1;
        hash = h.getRandom();
        count = 0;
    }

    public Order(Bundle b) {
        index = b.getInt(KEYINDEX);
        hash = b.getInt(KEYHASH);
        count = b.getInt(KEYCOUNT);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYINDEX, index);
            json.put(KEYHASH, hash);
            json.put(KEYCOUNT, count);

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            index = json.getInt(KEYINDEX);
            hash = json.getInt(KEYHASH);
            count = json.getInt(KEYCOUNT);
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    public String toString() {
        return "i:" + index + ",h:" + hash + ",c:" + count;
    }
}