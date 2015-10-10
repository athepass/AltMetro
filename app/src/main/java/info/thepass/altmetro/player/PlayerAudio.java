package info.thepass.altmetro.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class PlayerAudio implements Runnable {
    public final static String TAG = "trak:PlayerAudio";
    // parent
    public HelperMetro h;
    public BarManager bm;
    public PlayerData pd;
    // runnable management
    public Object mPauseLock;
    // sound management
    public int soundLength;
    public SoundCollection sc;
    public AudioTrack audioTrack;
    private boolean mFinished;
    private boolean mPaused;

    public PlayerAudio(HelperMetro hh, BarManager bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        h.logD(TAG, "constructor");
        mPauseLock = new Object();
        mPaused = true;
        mFinished = false;

        initAudio();
    }

    public void run() {
        initRun();

        while (!mFinished) {
            if (!mPaused) {
                doStep();
            }
            doWait();
        }
        finishRun();
    }

    private void doWait() {
        synchronized (mPauseLock) {
            while (mPaused) {
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void onPause() {
        h.logD(TAG, "onPause");
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        h.logD(TAG, "onResume");
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notify();
        }
    }

    private void initPlay() {
        pd.trackBarCounter = 0;
        pd.timeStart2 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.layoutUpdater);
        pd.timeStart3 = h.getNanoTime();
        h.logD(TAG, "initPlay t=" + h.deltaTime(pd.timeStart1, pd.timeStart2)
                + ".." + h.deltaTime(pd.timeStart1, pd.timeStart3));
    }

    private void finishPlay() {
        h.logD(TAG, "finish Play");
        pd.timeStop1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.stopper);
    }

    private void doStep() {
        initPlay();
        pd.repeatCounter = 0;
        while (pd.repeatCounter < pd.bmTrack.repeats.size()) {
            pd.bmRepeat = pd.bmTrack.repeats.get(pd.repeatCounter);
            pd.bmPat = pd.bmTrack.pats.get(pd.bmTrack.patSelected);
            playRepeat();
            pd.repeatCounter++;
        }
        finishPlay();
    }

    private void playRepeat() {
        pd.repeatBarCounter = 0;
        updateInfo();
        while (!mPaused && pd.repeatBarCounter < pd.bmRepeat.barCount) {
            pd.beatListCounter = 0;
            while (!mPaused && pd.beatListCounter < pd.bmRepeat.beatList.size()) {
                pd.bmBeat = pd.bmRepeat.beatList.get(pd.beatListCounter);
                pd.currentBeat = pd.bmBeat.beatIndex + 1;
                String logInfo = "beat[Sound] " + pd.beatListCounter + " info:"
                        + pd.bmRepeat.beatList.get(pd.beatListCounter).display(pd.beatListCounter, pd.subs);
                pd.timeBeat1 = h.getNanoTime();
                pd.nextBeat = getNextBeat();

                playSoundList(pd.bmBeat);

                logInfo += " draw:" + h.deltaTime(pd.timeLayout1, pd.timeLayout2);
                h.logD(TAG, logInfo);

                if (pd.beatListCounter == pd.bmBeat.beats - 1) { // bar counter ophogen
                    pd.repeatBarCounter++;
                    pd.trackBarCounter++;
                    updateInfo();
                }

                pd.beatListCounter += pd.nextBeat;
                if (pd.beatListCounter >= pd.bmRepeat.beatList.size()) {
                    h.logD(TAG, "beatSound ready");
                }
            }

            if (!pd.bmRepeat.noEnd) {
                pd.repeatCounter++;
            }
        }
    }

    private void updateInfo() {
        pd.playerInfo = "r" + (pd.repeatCounter + 1) + "/" + pd.bmTrack.repeats.size();
        pd.playerInfo += " "+ ((pd.bmRepeat.noEnd) ? h.getString(R.string.label_noend) : "");
        pd.playerInfo += " " + (pd.trackBarCounter + 1);
        bm.getActivity().runOnUiThread(bm.infoUpdater);
    }

    private int getNextBeat() {
        int nextBeat = 0;
        if (pd.beatListCounter < pd.bmBeat.beats - 1) { // niet op de laatste beat: volgend beat
            nextBeat = 1;
        } else {    // laatste beat
            if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                nextBeat = 1 - pd.bmBeat.beats;
            } else {
                if (pd.repeatBarCounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                    nextBeat = 1;
                } else { // naar 1 voor afspelen volgende bar
                    nextBeat = 1 - pd.bmBeat.beats;
                }
            }
        }
        return nextBeat;
    }

    private void playSoundList(Beat beat) {
        for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
            Sound sound = beat.soundList.get(iSound);
            switch (sound.soundType) {
                case Keys.SOUNDFIRST:
                    writeSound(sc.soundFirst, sound.duration);
                    break;
                case Keys.SOUNDHIGH:
                    writeSound(sc.soundHigh, sound.duration);
                    break;
                case Keys.SOUNDLOW:
                    writeSound(sc.soundLow, sound.duration);
                    break;
                case Keys.SOUNDNONE:
                    writeSound(sc.soundSilence, sound.duration);
                    break;
                case Keys.SOUNDSUB:
                    writeSound(sc.soundSub, sound.duration);
                    break;
                case Keys.SOUNDSILENCE:
                    writeSound(sc.soundSilence, sound.duration);
                    break;
                default:
                    throw new RuntimeException("playBeat invalid soundtype " + sound.soundType);
            }
        }
    }

    private void writeSound(byte[] soundBytes, int duration) {
        int playDuration = duration * 2;
        while (!mPaused && playDuration > 0) {
            if (playDuration > SoundCollection.SOUNDLENGTH) {
                soundLength = SoundCollection.SOUNDLENGTH;
                playDuration -= SoundCollection.SOUNDLENGTH;
            } else {
                soundLength = playDuration;
                playDuration = 0;
            }
            audioTrack.write(soundBytes, 0, soundLength);
        }
    }

    private void initAudio() {
        sc = new SoundCollection(h, TAG);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void initRun() {
        h.logD(TAG, "start Runnable " + bm.audioThread.getPriority());
    }

    private void finishRun() {
        h.logI(TAG, "finish Runnable");
    }
}