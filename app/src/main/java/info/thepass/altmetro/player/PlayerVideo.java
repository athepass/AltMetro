package info.thepass.altmetro.player;

import android.util.Log;

import info.thepass.altmetro.tools.HelperMetro;

public class PlayerVideo implements Runnable {
    public final static String TAG = "trak:PlayerVideo";
    HelperMetro h;
    Player bm;
    PlayerData pd;

    boolean mFinished;

    public PlayerVideo(HelperMetro hh, Player bm) {
        h = hh;
        this.bm = bm;
        pd = bm.pd;
        Log.d(TAG, "constructor");
    }

    public void run() {

        while (!mFinished) {
        }
    }
}