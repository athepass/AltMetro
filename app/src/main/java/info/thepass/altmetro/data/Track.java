package info.thepass.altmetro.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.Audio.Beat;
import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class Track {
    public final static String TAG = "TrakTrack";
    public final static String KEYNR = "TRnr";
    public final static String KEYHASHTRACK = "TRhtrk";
    public final static String KEYTITEL = "TRtitel";
    public final static String KEYMULTI = "TRmulti";
    public final static String KEYSTUDY = "TRstudy";
    public final static String KEYREPEATS = "TRrepeats";
    public final static String KEYREPEATSELECTED = "TRselrep";
    public final static String KEYPATS = "TRpats";
    public final static String KEYPATSELECTED = "TRselpat";
    public int nummer;
    public int hashTrack;
    public String titel;
    public boolean multi;
    public Study study;
    public ArrayList<Repeat> repeats;
    public int repeatSelected;
    public ArrayList<String> items;
    private TrackData trackData;
    public int patSelected;
    public ArrayList<Pat> pats;

    public Track(HelperMetro h, TrackData data) {
        trackData = data;

        nummer = 0;
        hashTrack = h.getHash();
        titel = "";
        multi = h.prefs.getBoolean(Keys.PREFMULTI, false);

        study = new Study();

        repeatSelected = 0;
        repeats = new ArrayList<Repeat>();
        Repeat repeat = new Repeat();
        repeats.add(repeat);

        //Laat de repeat meteen verwijzen naar de 0e pattern
        repeat.indexPattern = 0;

        pats = new ArrayList<Pat>();
        patSelected = 0;
        Pat pat = new Pat(h);
        pats.add(pat);

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

            json.put(KEYPATSELECTED, patSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < pats.size(); i++) {
                patsArray.put(pats.get(i).toJson());
            }
            json.put(KEYPATS, patsArray);

        } catch (JSONException e) {
            throw new RuntimeException("toJson exception" + e.getMessage());
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
                Repeat rep = new Repeat();
                rep.fromJson(repeatsArray.getJSONObject(i));
                repeats.add(rep);
            }

            patSelected = json.getInt(KEYPATSELECTED);
            pats.clear();
            JSONArray patsArray = json.getJSONArray(KEYPATS);
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                pats.add(pat);
            }

        } catch (JSONException e) {
            throw new RuntimeException("fromJson exception" + e.getMessage());
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
        if (trackData.metroMode != Keys.METROMODESIMPLE) {
            return 0;
        }
        if (multi) {
            return position;
        } else {
            return 0;
        }
    }

    public int getItemPatPosition(int position) {
        if (trackData.metroMode == Keys.METROMODESIMPLE) {
            return 0;
        }
        if (multi) {
            return position - repeats.size() - 1;
        } else {   // single
            return position - 1;
        }
    }

    public void syncItems(ArrayList<Pat> pats) {
        int aantal = -1;
        if (trackData.metroMode == Keys.METROMODESIMPLE) {
            aantal = 2;
        } else {
            if (multi) {
                aantal = repeats.size() + 1 + pats.size() + 1;
            } else {  // Single repeat
                aantal = 1 + pats.size() + 1;  // vaste aantal voor single items: repeat + add pattern + pats
            }
        }
        // maak aantal regels
        while (items.size() < aantal)
            items.add("-----");
        while (items.size() > aantal)
            items.remove(0);

        // controleer consistentie pat hash in repeat
        for (int i = 0; i < repeats.size(); i++) {
            Repeat repeat = repeats.get(i);
            if (repeat.hashPattern == Repeat.NOHASH) {
                // hash default: vul hash obv repeat.indexpattern
                repeat.hashPattern = pats.get(repeat.indexPattern).patHash;
            } else {
                // hash ingevuld. Zet index goed.
                for (int j = 0; j < pats.size(); j++) {
                    Pat pat = pats.get(j);
                    if (repeat.hashPattern == pat.patHash) {
                        repeat.indexPattern = j;
                        j = pats.size();
                    }
                }
            }
        }
    }

    public void setTempo(int newTempo) {
        repeats.get(repeatSelected).tempo = newTempo;
    }

    public boolean trackOK(HelperMetro h) {
        if (trackData.metroMode == Keys.METROMODESIMPLE || !multi) {
            return true;
        }

        for (int i = 0; i < repeats.size(); i++) {
            Repeat repeat = repeats.get(i);
            if (repeat.noEnd && i < repeats.size() - 1) {
                h.showToastAlert(h.getString1(R.string.error_unreachable, "" + (i + 1)));
                return false;
            }
        }
        return true;
    }

    public void buildBeatList(ArrayList<Beat> beatList) {
        if (trackData.metroMode == Keys.METROMODESIMPLE || !multi) {
            repeats.get(0).buildBeatList(beatList);
        } else {
            for (int iRep = 0; iRep < repeats.size(); iRep++) {
                repeats.get(iRep).buildBeatList(beatList);
            }
        }
    }

    public void clean() {
        if (trackData.metroMode==Keys.METROMODESIMPLE) {
            while (repeats.size()>1)
                repeats.remove(1);
            while (pats.size()>1)
                pats.remove(1);
            repeats.get(0).indexPattern=0;
            repeats.get(0).hashPattern = pats.get(0).patHash;
            multi = false;
        } else {
            if (multi) {
                study.used = false;
            } else {
                while (repeats.size()>1)
                    repeats.remove(1);
            }
        }
    }
}