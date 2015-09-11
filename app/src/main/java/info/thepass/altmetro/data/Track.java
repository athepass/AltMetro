package info.thepass.altmetro.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.tools.HelperMetro;

public class Track {
    public final static String TAG = "Track";
    public final static String KEYNR = "TRnr";
    public final static String KEYHASH = "TRhash";
    public final static String KEYTITEL = "TRtit";
    public final static String KEYMULTI = "TRmul";
    public final static String KEYSTUDY = "TRstu";
    public final static String KEYPATS = "TRpat";
    public final static String KEYPATSELECTED = "TRselpat";
    public final static String KEYORDERS = "TRord";
    public final static String KEYORDERSELECTED = "TRselord";
    public int nummer;
    public int hash;
    public String titel;
    public boolean multi;
    public Study study;
    public ArrayList<Pattern> pats;
    public int patSelected;
    public ArrayList<Order> orders;
    public int orderSelected;

    public Track(HelperMetro h) {
        nummer = 0;
        hash = h.getRandom();
        titel = "";
        multi = false;

        study = new Study();

        pats = new ArrayList<Pattern>();
        patSelected = 0;
        Pattern pat = new Pattern();
        pats.add(pat);

        orders = new ArrayList<Order>();
        orderSelected = 0;
        Order order = new Order(h);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYNR, nummer);
            json.put(KEYHASH, hash);
            json.put(KEYTITEL, titel);
            json.put(KEYMULTI, multi);

            json.put(KEYSTUDY, study.toJson());

            json.put(KEYPATSELECTED, patSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < pats.size(); i++) {
                patsArray.put(pats.get(i).toJson());
            }
            json.put(KEYPATS, patsArray);

            json.put(KEYORDERSELECTED, orderSelected);
            JSONArray ordersArray = new JSONArray();
            for (int i = 0; i < orders.size(); i++) {
                ordersArray.put(orders.get(i).toJson());
            }
            json.put(KEYORDERS, ordersArray);

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        try {
            nummer = json.getInt(KEYNR);
            hash = json.getInt(KEYHASH);
            titel = json.getString(KEYTITEL);
            multi = json.getBoolean(KEYMULTI);

            study.fromJson(json.getJSONObject(KEYSTUDY));

            json.put(KEYPATSELECTED, patSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < pats.size(); i++) {
                patsArray.put(pats.get(i).toJson());
            }
            json.put(KEYPATS, patsArray);

            json.put(KEYORDERSELECTED, orderSelected);
            JSONArray ordersArray = new JSONArray();
            for (int i = 0; i < orders.size(); i++) {
                ordersArray.put(orders.get(i).toJson());
            }
            json.put(KEYORDERS, ordersArray);

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    public String toStringH(HelperMetro h) {
        String s = "n:" + nummer + ",h:" + hash + ",m:" + ((multi) ? "*" : "-") + ",t:" + titel;
        s += "\nStudy:" + study.toString();
        for (int i = 0; i < pats.size(); i++) {
            s += "\npat " + i + ": " + ((i == patSelected) ? "*" : "") + pats.get(i).toStringShort(h);
        }
        for (int i = 0; i < orders.size(); i++) {
            s += "\norder " + i + ": " + ((i == orderSelected) ? "*" : "") + orders.get(i).toString();
        }
        return s;
    }
}