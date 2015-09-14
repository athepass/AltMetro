package info.thepass.altmetro.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.tools.HelperMetro;

public class Track {
    public final static String TAG = "TrakTrack";
    public final static String KEYNR = "TRnr";
    public final static String KEYHASHTRACK = "TRhtrk";
    public final static String KEYTITEL = "TRtit";
    public final static String KEYMULTI = "TRmul";
    public final static String KEYSTUDY = "TRstu";
    public final static String KEYPATS = "TRpat";
    public final static String KEYPATSELECTED = "TRselpat";
    public final static String KEYORDERS = "TRord";
    public final static String KEYORDERSELECTED = "TRselord";
    public int nummer;
    public int hashTrack;
    public String titel;
    public boolean multi;
    public Study study;
    public ArrayList<Pat> pats;
    public int patSelected;
    public ArrayList<Order> orders;
    public int orderSelected;
    public ArrayList<String> items;

    public Track(HelperMetro h) {
        nummer = 0;
        hashTrack = h.getHash();
        titel = "";
        multi = false;

        study = new Study();

        pats = new ArrayList<Pat>();
        orders = new ArrayList<Order>();

        patSelected = 0;
        Pat pat = new Pat(h);
        pats.add(pat);

        orderSelected = 0;
        Order order = new Order(h);
        orders.add(order);

        items = new ArrayList<String>();
        syncItems();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYNR, nummer);
            json.put(KEYHASHTRACK, hashTrack);
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
            hashTrack = json.getInt(KEYHASHTRACK);
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
        String s = "n:" + nummer + ",h:" + hashTrack + ",m:" + ((multi) ? "*" : "-") + ",t:" + titel;
        s += "\nStudy:" + study.toString();
        for (int i = 0; i < pats.size(); i++) {
            s += "\npat " + i + ": " + ((i == patSelected) ? "*" : "") + pats.get(i).toStringShort(h);
        }
        for (int i = 0; i < orders.size(); i++) {
            s += "\norder " + i + ": " + ((i == orderSelected) ? "*" : "") + orders.get(i).toString();
        }
        return s;
    }

    public String getTitle(TrackData trackData, int sel) {
        if ((trackData.tracks.size() == 1)
                && (pats.size() == 1)
                && (orders.size() == 1)
                && (titel.equals(""))
                && (nummer == 0)
                ) {
            return "";
        } else {
            return (sel + 1) + ((multi) ? "[*]:" : ":")
                    + ((nummer != 0) ? " " + nummer : "")
                    + " " + titel
                    + ((multi) ? "[" + pats.size() + "|" + orders.size() + "]" : "");
        }
    }

    public void syncItems() {
        int aantal = 3;  // vaste aantal voor single items
        if (multi) {
            aantal = 1 + orders.size() + 1 + pats.size() + 1;}
        while (items.size() < aantal)
            items.add("+");
        while (items.size() > aantal)
            items.remove(0);
    }
    public int getItemOrderPosition(int position) {
        if (multi) {
            return position - 1;
        } else {
            return 0;
        }
    }

    public int getItemPatPosition(int position) {
        if (multi) {
            return position - 1 - orders.size() - 1;
        } else {
            return 0;
        }
    }

}