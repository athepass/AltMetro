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
    public final static String KEYTITEL = "TRtitel";
    public final static String KEYMULTI = "TRmulti";
    public final static String KEYSTUDY = "TRstudy";
    public final static String KEYREPEATS = "TRrepeats";
    public final static String KEYREPEATSELECTED = "TRselrep";
    public int nummer;
    public int hashTrack;
    public String titel;
    public boolean multi;
    public Study study;
    public ArrayList<Repeat> repeats;
    public int repeatSelected;
    public ArrayList<String> items;

    public Track(HelperMetro h) {
        nummer = 0;
        hashTrack = h.getHash();
        titel = "";
        multi = false;

        study = new Study();

        repeatSelected = 0;
        repeats = new ArrayList<Repeat>();
        Repeat repeat = new Repeat(h);
        repeats.add(repeat);

        //Laat de repeat meteen verwijzen naar de 0e pattern
        repeat.indexPattern = 0;

        items = new ArrayList<String>();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYNR, nummer);
            json.put(KEYHASHTRACK, hashTrack);
            json.put(KEYTITEL, titel);
            json.put(KEYMULTI, multi);

            json.put(KEYSTUDY, study.toJson());

            json.put(KEYREPEATSELECTED, repeatSelected);
            JSONArray repeatsArray = new JSONArray();
            for (int i = 0; i < repeats.size(); i++) {
                repeatsArray.put(repeats.get(i).toJson());
            }
            json.put(KEYREPEATS, repeatsArray);

        } catch (Exception e) {
            throw new RuntimeException("toJson exception"+e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json, HelperMetro h) {
        try {
            nummer = json.getInt(KEYNR);
            hashTrack = json.getInt(KEYHASHTRACK);
            titel = json.getString(KEYTITEL);
            multi = json.getBoolean(KEYMULTI);

            study.fromJson(json.getJSONObject(KEYSTUDY));

            repeatSelected = json.getInt(KEYREPEATSELECTED);
            repeats.clear();
            JSONArray repeatsArray = json.getJSONArray(KEYREPEATS);
            for (int i = 0; i < repeatsArray.length(); i++) {
                Repeat rep = new Repeat(h);
                rep.fromJson(repeatsArray.getJSONObject(i));
                repeats.add(rep);
            }

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
        }
    }

    public String toStringH(HelperMetro h) {
        String s = "n:" + nummer + ",h:" + hashTrack + ",m:" + ((multi) ? "*" : "-") + ",t:" + titel;
        s += "\nStudy:" + study.toString();
        for (int i = 0; i < repeats.size(); i++) {
            s += "\nRepeat " + i + ": " + ((i == repeatSelected) ? "*" : "") + repeats.get(i).toString();
        }
        return s;
    }

    public String display(HelperMetro h, int index) {
        String s = "";
        s += (index >= 0) ? "t" + (index + 1) + ":" : "";
        s += (multi) ? "[*]" : "[-]";
        s += (nummer != 0) ? " nr " + nummer : "";
        s += (titel.length() > 0) ? " titel:" + titel : "";
        s += (repeats.size() > 1) ? " #repeats:" + repeats.size() : "";
        return s;
    }

    public String getTitle(TrackData trackData, int sel) {
        if ((trackData.tracks.size() == 1)
                && (repeats.size() == 1)
                && (titel.equals(""))
                && (nummer == 0)
                ) {
            return "";
        } else {
            return (sel + 1) + ((multi) ? "[*]:" : ":")
                    + ((nummer != 0) ? " " + nummer : "")
                    + " " + titel
                    + ((multi) ? "[" + repeats.size() + "]" : "");
        }
    }


    public int getItemRepeatPosition(int position) {
        if (multi) {
            return position - 1;
        } else {
            return 0;
        }
    }

    public int getItemPatPosition(int position) {
        if (multi) {
            return position - 1 - repeats.size() - 1;
        } else {
            return position - 2;
        }
    }

    public void syncItems(ArrayList<Pat> pats) {
        int aantal = 3 + pats.size();  // vaste aantal voor single items: study + repeat + add pattern + pats
        if (multi) {
            aantal = 1 + repeats.size() + 1 + pats.size() + 1;
        }
        while (items.size() < aantal)
            items.add("-----");
        while (items.size() > aantal)
            items.remove(0);
    }

}