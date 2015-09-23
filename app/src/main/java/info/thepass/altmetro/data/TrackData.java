package info.thepass.altmetro.data;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackData {
    public final static String TAG = "TrakData";
    public final static String KEYTRACKS = "TDtrack";
    public final static String KEYTRACKSELECTED = "TDseltrk";
    public final static String KEYTDPATS = "TDpats";
    public final static String KEYTDPATSELECTED = "TDselpat";
    public final static String KEYMETROMODE = "TDmetmod";
    public int metroMode;
    public ArrayList<Track> tracks;
    public int trackSelected;
    private HelperMetro h;
    private String pad;
    private String filenaam;
    private File dataFile;
    private int tdPatSelected;
    private ArrayList<Pat> tdPats;

    public TrackData(HelperMetro hh) {
        h = hh;
        trackSelected = 0;
        tracks = new ArrayList<Track>();
        tdPats = new ArrayList<Pat>();

        initDataFile();
        if (!dataFile.exists()) {
            addDefaultData();
        } else {
            readData("constructor", false);
        }
    }

    private void initDataFile() {
        pad = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/AltMetro/";
        filenaam = pad + "trackData.txt";
        File p = new File(pad);
        p.mkdirs();
        dataFile = new File(filenaam);
    }

    private void addDefaultData() {
        h.logD(TAG, "addDefaultData");

        Track track = new Track(h, this);
        tracks.add(track);
        trackSelected = 0;

        tdPatSelected = 0;
        Pat pat = new Pat(h);
        tdPats.add(pat);

        metroMode = Integer.parseInt(h.prefs.getString(Keys.PREFMETROMODE, "" + Keys.METROMODESIMPLE));
    }

    private void readData(String tag, boolean doDump) {
        try {
            // read json string from file
            FileInputStream is = new FileInputStream(dataFile);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            h.logD(TAG, "\nreadData " + tag
                    + ((doDump) ? "\n" + sb.toString() : ""));

            // convert json string to JsonObject
            // vul data vanuit JSON object
            JSONObject jsonRoot = new JSONObject(sb.toString());
            fromJson(jsonRoot, h);

        } catch (IOException e) {
            throw new RuntimeException("ReadPattern IOException " + e.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException("ReadPattern JSONException " + e.getMessage());
        }
    }

    public void saveData(String tag, boolean doDump) {
        // van TrackData naar JSONobject
        JSONObject jsonRoot = null;
        try {
            jsonRoot = toJson();
            if (doDump) {
                h.logD(TAG, "saveData\n" + jsonRoot.toString());
            } else {
                h.logI(TAG, "saveData " + tag);
            }
        } catch (Exception e) {
            throw new RuntimeException("saveData " + e.getMessage());
        }

        // json object bewaren in file.
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                    dataFile, false));
            bufferedWriter.write(jsonRoot.toString());
            bufferedWriter.close();
            String s = "saveData, data written " + tag + saveInfo();
        } catch (Exception e) {
            Log.e(TAG, "write " + filenaam + ": " + e.getMessage());
        }
    }

    public String saveInfo() {
        return " Track sel=" + trackSelected + " size=" + tracks.size()
                + "Pat sel=" + tdPatSelected + " size=" + tdPats.size();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {

            json.put(KEYMETROMODE, metroMode);

            json.put(KEYTRACKSELECTED, trackSelected);
            JSONArray tracksArray = new JSONArray();
            for (int i = 0; i < tracks.size(); i++) {
                tracksArray.put(tracks.get(i).toJson());
            }
            json.put(KEYTRACKS, tracksArray);

            json.put(KEYTDPATSELECTED, tdPatSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < tdPats.size(); i++) {
                patsArray.put(tdPats.get(i).toJson());
            }
            json.put(KEYTDPATS, patsArray);

        } catch (JSONException e) {
            throw new RuntimeException("toJson " + e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json, HelperMetro h) {
        try {
            metroMode = json.getInt(KEYMETROMODE);

            trackSelected = json.getInt(KEYTRACKSELECTED);
            JSONArray tracksArray = json.getJSONArray(KEYTRACKS);
            tracks.clear();
            for (int i = 0; i < tracksArray.length(); i++) {
                Track track = new Track(h, this);
                track.fromJson(tracksArray.getJSONObject(i), h);
                tracks.add(track);
            }

            tdPatSelected = json.getInt(KEYTDPATSELECTED);
            tdPats.clear();
            JSONArray patsArray = json.getJSONArray(KEYTDPATS);
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                tdPats.add(pat);
            }

        } catch (JSONException e) {
            throw new RuntimeException("fromJson " + e.getMessage());
        }
    }

    public void updateTrack(Track trackNew) {
        for (int i = 0; i < tracks.size(); i++) {
            if (trackNew.hashTrack == tracks.get(i).hashTrack) {
                tracks.remove(i);
                tracks.add(i, trackNew);
            }
        }
    }

    public ArrayList<Pat> getTdPats() {
        return tdPats;
    }

    public int getTdPatSelected() {
        return tdPatSelected;
    }

    public int getMetroMode() {
        return metroMode;
    }

    public void checkMetroMode() {
        if (metroMode == Keys.METROMODESIMPLE) {
            trackSelected = 0;
            Track track = tracks.get(0);
            Repeat repeat = track.repeats.get(0);
            repeat.indexPattern = 0;
            repeat.hashPattern = track.getPats().get(0).patHash;
        }
    }
}
