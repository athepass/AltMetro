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
    public final static String KEYPATS = "TRpats";
    public final static String KEYPATSELECTED = "TRselpat";
    public final static String KEYREPEATS = "TRrepeats";
    public final static String KEYREPEATSELECTED = "TRselrep";
    public int nummer;
    public int hashTrack;
    public String titel;
    public boolean multi;
    public Study study;
    public ArrayList<Pat> pats;
    public int patSelected;
    public ArrayList<Repeat> repeats;
    public int repeatSelected;
    public ArrayList<String> items;

    public Track(HelperMetro h) {
        nummer = 0;
        hashTrack = h.getHash();
        titel = "";
        multi = false;

        study = new Study();

        pats = new ArrayList<Pat>();
        repeats = new ArrayList<Repeat>();

        patSelected = 0;
        Pat pat = new Pat(h);
        pats.add(pat);

        repeatSelected = 0;
        Repeat repeat = new Repeat(h);
        repeats.add(repeat);

        //Laat de repeat meteen verwijzen naar de 0e pattern
        repeat.indexPattern = 0;
        repeat.hashPattern = pat.patHash;

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

            json.put(KEYREPEATSELECTED, repeatSelected);
            JSONArray repeatsArray = new JSONArray();
            for (int i = 0; i < repeats.size(); i++) {
                repeatsArray.put(repeats.get(i).toJson());
            }
            json.put(KEYREPEATS, repeatsArray);
//            Log.d(TAG,"toJson repeats " + repeatsArray.toString(3));

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
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

            patSelected = json.getInt(KEYPATSELECTED);
            pats.clear();
            JSONArray patsArray = json.getJSONArray(KEYPATS);
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                pats.add(pat);
            }

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
        for (int i = 0; i < pats.size(); i++) {
            s += "\npat " + i + ": " + ((i == patSelected) ? "*" : "") + pats.get(i).toStringShort(h);
        }
        for (int i = 0; i < repeats.size(); i++) {
            s += "\nRepeat " + i + ": " + ((i == repeatSelected) ? "*" : "") + repeats.get(i).toString();
        }
        return s;
    }

    public String display(HelperMetro h) {
        String s = (nummer!=0) ? "nr:" + nummer : "" ;
        s += (titel.length()>0) ? " titel:" + titel : "";
        s += (multi) ? "[*]" : "[-]";
        s += " repeats:" + repeats.size();
        return s;
    }

    public String getTitle(TrackData trackData, int sel) {
        if ((trackData.tracks.size() == 1)
                && (pats.size() == 1)
                && (repeats.size() == 1)
                && (titel.equals(""))
                && (nummer == 0)
                ) {
            return "";
        } else {
            return (sel + 1) + ((multi) ? "[*]:" : ":")
                    + ((nummer != 0) ? " " + nummer : "")
                    + " " + titel
                    + ((multi) ? "[" + pats.size() + "|" + repeats.size() + "]" : "");
        }
    }

    public void syncItems() {
        int aantal = 3;  // vaste aantal voor single items
        if (multi) {
            aantal = 1 + repeats.size() + 1 + pats.size() + 1;
        }
        while (items.size() < aantal)
            items.add("+");
        while (items.size() > aantal)
            items.remove(0);
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
            return 0;
        }
    }
}