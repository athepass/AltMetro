package info.thepass.altmetro.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import info.thepass.altmetro.R;
import info.thepass.altmetro.player.BarManager;
import info.thepass.altmetro.player.Beat;
import info.thepass.altmetro.player.Sound;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class Track {
    public final static String TAG = "TrakTrack";
    private HelperMetro h;
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
    public ArrayList<Repeat> repeatList;
    public int repeatSelected;
    public ArrayList<String> items;
    public int patSelected;
    public ArrayList<Pat> patList;
    private MetronomeData metronomeData;

    public Track(HelperMetro h, MetronomeData data) {
        this.h = h;
        metronomeData = data;

        nummer = 0;
        hashTrack = h.getHash();
        titel = "";
        multi = h.prefs.getBoolean(Keys.PREFMULTI, false);

        study = new Study();

        repeatSelected = 0;
        repeatList = new ArrayList<Repeat>();
        Repeat repeat = new Repeat();
        repeatList.add(repeat);

        //Laat de repeat meteen verwijzen naar de 0e pattern
        repeat.patSelected = 0;

        patList = new ArrayList<Pat>();
        patSelected = 0;
        Pat pat = new Pat(h);
        patList.add(pat);

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
            for (int i = 0; i < repeatList.size(); i++) {
                repeatsArray.put(repeatList.get(i).toJson());
            }
            json.put(KEYREPEATS, repeatsArray);

            json.put(KEYPATSELECTED, patSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < patList.size(); i++) {
                patsArray.put(patList.get(i).toJson());
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
            repeatList.clear();
            JSONArray repeatsArray = json.getJSONArray(KEYREPEATS);
            for (int i = 0; i < repeatsArray.length(); i++) {
                Repeat rep = new Repeat();
                rep.fromJson(repeatsArray.getJSONObject(i));
                repeatList.add(rep);
            }

            patSelected = json.getInt(KEYPATSELECTED);
            patList.clear();
            JSONArray patsArray = json.getJSONArray(KEYPATS);
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                patList.add(pat);
            }

        } catch (JSONException e) {
            throw new RuntimeException("fromJson exception" + e.getMessage());
        }
    }

    public String toStringH(HelperMetro h) {
        String s = "n:" + nummer + ",h:" + hashTrack + ",m:" + ((multi) ? "*" : "-") + ",t:" + titel;
        s += "\nStudy:" + study.toString();
        for (int i = 0; i < repeatList.size(); i++) {
            s += "\nRepeat " + i + ": " + ((i == repeatSelected) ? "*" : "") + repeatList.get(i).toString();
        }
        return s;
    }

    public String display(HelperMetro h, int index) {
        String s = "";
        s += (index >= 0) ? "t" + (index + 1) + ":" : "";
        s += (multi) ? "[*]" : "[-]";
        s += (nummer != 0) ? " nr " + nummer : "";
        s += (titel.length() > 0) ? " titel:" + titel : "";
        s += (repeatList.size() > 1) ? " #repeatList:" + repeatList.size() : "";
        return s;
    }

    public String getTitle(MetronomeData metronomeData, int sel) {
        if ((metronomeData.tracks.size() == 1)
                && (repeatList.size() == 1)
                && (titel.equals(""))
                && (nummer == 0)
                ) {
            return "";
        } else {
            return (sel + 1) + ((multi) ? "[*]:" : ":")
                    + ((nummer != 0) ? " " + nummer : "")
                    + " " + titel
                    + ((multi) ? "[" + repeatList.size() + "]" : "");
        }
    }


    public int getItemRepeatPosition(int position) {
        if (multi) {
            return position;
        } else {
            return 0;
        }
    }

    public int getItemPatPosition(int position) {
        if (multi) {
            return position - repeatList.size() - 1;
        } else {   // single
            return 0;
        }
    }

    public void clean() {
        // singleTempo: 1 pat, 1 repeat, repeat verwijst naar pat.
        if (!multi) {
            while (repeatList.size() > 1)
                repeatList.remove(1);
            while (patList.size() > 1)
                patList.remove(1);
            repeatList.get(0).patSelected = 0;
            repeatList.get(0).patSelectedHash = patList.get(0).patHash;
        }
        // controleer consistentie pat hash in repeat
        for (int i = 0; i < repeatList.size(); i++) {
            Repeat repeat = repeatList.get(i);
            if (repeat.patSelectedHash == Repeat.NOHASH) {
                // hash default: vul hash obv repeat.indexpattern
                repeat.patSelectedHash = patList.get(repeat.patSelected).patHash;
            } else {
                // hash ingevuld. Zet index goed.
                for (int j = 0; j < patList.size(); j++) {
                    Pat pat = patList.get(j);
                    if (repeat.patSelectedHash == pat.patHash) {
                        repeat.patSelected = j;
                        j = patList.size();
                    }
                }
            }
        }
        syncItems();
    }

    public void syncItems() {
        int aantal = -1;
        if (multi) {
            aantal = repeatList.size() + 1 + patList.size() + 1;
        } else {  // Single repeat
            aantal = 1 + 1;  // vaste aantal voor single items: repeat + patList + add pattern
        }
        // maak aantal regels
        while (items.size() < aantal)
            items.add("-----");
        while (items.size() > aantal)
            items.remove(0);
    }

    public void setTempo(int newTempo) {
        repeatList.get(repeatSelected).tempo = newTempo;
    }

    public boolean trackPlayable(HelperMetro h) {
        if (repeatList.size() == 1) {
            return true;
        }

        for (int i = 0; i < repeatList.size(); i++) {
            Repeat repeat = repeatList.get(i);
            if (repeat.noEnd && i < repeatList.size() - 1) {
                String msg = h.getString1(R.string.error_unreachable, "" + (i + 1));
                h.logD(TAG, msg);
                h.showToastAlert(msg);
                return false;
            }
        }
        return true;
    }

    public void buildBeat(BarManager bm, HelperMetro h) {
        bm.pd.repeatBarCounter = 0;
        for (int iRep = 0; iRep < repeatList.size(); iRep++) {
            repeatList.get(iRep).buildBeatList(bm, iRep, h);
            repeatList.get(iRep).buildSound(bm);
        }
    }

    public void soundDump(BarManager bm) {
        String[] subs = bm.h.getStringArray(R.array.sub_pattern);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.hhmm", Locale.getDefault());
        String pad = bm.h.getLogFilePath();
        String filename = pad + sdf.format(new Date()) + ".txt";
        File padFile = new File(pad);
        boolean res = padFile.mkdirs();
        File dumpFile = new File(filename);
        String s = "\n======== repeatList ======== build:"
                + bm.buildCounter + " bars:" + bm.pd.repeatBarCounter;
        for (int irep = 0; irep < repeatList.size(); irep++) {
            Repeat tRepeat = repeatList.get(irep);
            String dispPat = patList.get(tRepeat.patSelected).display(bm.h, tRepeat.patSelected, true);
            s += "\n ======== [rep " + irep + "] " + repeatList.get(irep).display(bm.h, irep, dispPat, true);
            for (int ibeat = 0; ibeat < tRepeat.beatList.size(); ibeat++) {
                Beat beat = tRepeat.beatList.get(ibeat);
                s += "\n=== beat " + ibeat + ": " + beat.display(ibeat, subs) + "\n";
                for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
                    Sound sound = beat.soundList.get(iSound);
                    s += sound.display() + "  ";
                }
            }
        }
        h.logD(TAG,s);
    }
}