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

public class TrackData {
    public final static String TAG = "TrakData";
    public final static String KEYTRACKS = "TDtrack";
    public final static String KEYTRACKSELECTED = "TDseltrk";
    public final static String KEYPATS = "TDpats";
    public final static String KEYPATSELECTED = "TDselpat";

    public ArrayList<Track> tracks;
    public int trackSelected;

    public ArrayList<Pat> pats;
    public int patSelected;

    private HelperMetro h;
    private String pad;
    private String filenaam;
    private File dataFile;

    public TrackData(HelperMetro hh) {
        h = hh;
        trackSelected = 0;
        tracks = new ArrayList<Track>();
        pats = new ArrayList<Pat>();
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

        Track track = new Track(h);
        tracks.add(track);
        trackSelected = 0;

        patSelected = 0;
        Pat pat = new Pat(h);
        pats.add(pat);
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
                + "Pat sel=" + patSelected + " size=" + pats.size();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put(KEYTRACKSELECTED, trackSelected);
            JSONArray tracksArray = new JSONArray();
            for (int i = 0; i < tracks.size(); i++) {
                tracksArray.put(tracks.get(i).toJson());
            }
            json.put(KEYTRACKS, tracksArray);

            json.put(KEYPATSELECTED, patSelected);
            JSONArray patsArray = new JSONArray();
            for (int i = 0; i < pats.size(); i++) {
                patsArray.put(pats.get(i).toJson());
            }
            json.put(KEYPATS, patsArray);

        } catch (JSONException e) {
            throw new RuntimeException("toJson " + e.getMessage());
        }
        return json;
    }

    public void fromJson(JSONObject json, HelperMetro h) {
        try {
            trackSelected = json.getInt(KEYTRACKSELECTED);
            JSONArray tracksArray = json.getJSONArray(KEYTRACKS);
            tracks.clear();
            for (int i = 0; i < tracksArray.length(); i++) {
                Track track = new Track(h);
                track.fromJson(tracksArray.getJSONObject(i), h);
                tracks.add(track);
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
}
