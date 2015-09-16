package info.thepass.altmetro.data;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import info.thepass.altmetro.tools.HelperMetro;

public class TrackData {
    public final static String TAG = "TrakData";
    public final static String KEYTRACKS = "MDtrack";
    public final static String KEYTRACKSELECTED = "MDseltrk";
    public ArrayList<Track> tracks;
    public int trackSelected;
    private HelperMetro h;
    private String pad;
    private String filenaam;
    private File dataFile;

    public TrackData(HelperMetro hh) {
        h = hh;
        trackSelected = 0;
        tracks = new ArrayList<Track>();
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
        Log.d(TAG, "addDefaultData");
        Track track = new Track(h);
        tracks.add(track);
        trackSelected = 0;
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
        } catch (Exception e) {
            h.logE(TAG, "readPattern", e);
        }
    }

    public void save(String tag) {
        // van TrackData naar JSONobject
        JSONObject jsonRoot = null;
        try {
            jsonRoot = toJson();
//        if (doDump) {
//            h.logD(TAG, "saveData " + jsonRoot.toString(3));
//        } else {
            h.logI(TAG, "saveData " + tag);
//        }
        } catch (Exception e) {
            h.logD(TAG, "exception SaveData " + e.getMessage());
        }

        // json object bewaren in file.
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                    dataFile, false));
            bufferedWriter.write(jsonRoot.toString());
            bufferedWriter.close();
            String s = "save, data written " + tag + "sel=" + trackSelected + " size=" + tracks.size();
            for (int i = 0; i < tracks.size(); i++) {
                Track track = tracks.get(i);
                s += " t" + i + ":r" + track.repeats.size() + "p" + track.pats.size();
            }
            h.logD(TAG, s);
        } catch (Exception e) {
            Log.e(TAG, "write " + filenaam + ": " + e.getMessage());
        }
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

        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
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
        } catch (Exception e) {
            Log.e(TAG, "toJson exception" + e.getMessage(), e);
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
