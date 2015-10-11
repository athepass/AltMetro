package info.thepass.altmetro.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

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
                switch (pd.playStatus) {
                    case Keys.PLAYSTART:
                        initPlay();
                        break;
                    case Keys.PLAYPLAY:
                        stepPlay();
                        break;
                    case Keys.PLAYSTOP:
                        finishPlay();
                        break;
                    default:
                        throw new RuntimeException("ongeldige playStatus " + pd.playStatus);
                }
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
            pd.playStatus = Keys.PLAYEND;
            mPaused = true;
        }
    }

    public void onResume() {
        h.logD(TAG, "onResume");
        synchronized (mPauseLock) {
            pd.playStatus = Keys.PLAYSTART;
            pd.timeNextStop = -1;
            mPaused = false;
            mPauseLock.notify();
        }
    }

    private void initPlay() {
        // init track
        pd.trackBarCounter = 0;
        // init study
        pd.studyCounter = 0;
        // init repeat
        pd.repeatListCounter = 0;
        pd.repeatBarCounter = 0;
        pd.bmRepeat = pd.bmTrack.repeatList.get(pd.repeatListCounter);
        pd.bmPat = pd.bmTrack.patList.get(pd.bmRepeat.patSelected);
        // init beat
        pd.beatListCounter = 0;
        pd.bmBeat = pd.bmRepeat.beatList.get(pd.beatListCounter);
        pd.nextBeat = pd.beatListCounter + 1;
        // init sound
        pd.soundListCounter = 0;
        pd.bmSound = pd.bmBeat.soundList.get(pd.soundListCounter);

        pd.playStatus = Keys.PLAYPLAY;
        pd.timeInitPlay = h.getNanoTime();
        h.logD(TAG, "initPlay t=" + h.deltaTime(pd.timeStartPlay, pd.timeInitPlay));
        showStudyInfo();
        showRepeatInfo();
        showBeatInfo();
    }

    private void finishPlay() {
        h.logD(TAG, "finish Play");
        pd.timeStop1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.stopper);
        pd.playStatus = Keys.PLAYSTOP;
    }

    private void stepPlay() {
        playSoundList();
        pd.soundListCounter++;          // next sound

        if (pd.soundListCounter >= pd.bmBeat.soundList.size()) {
            // einde soundlist, volgende beat
            showBeatInfo();
            pd.soundListCounter = 0;
            pd.beatListCounter += pd.nextBeat;
            if (pd.beatListCounter >= pd.bmRepeat.beatList.size()) {
                // einde beat, ga naar next bar in repeat
                pd.beatListCounter = 0;
                pd.repeatBarCounter++;
                pd.trackBarCounter++;
                if (!pd.bmRepeat.noEnd && pd.repeatBarCounter >= pd.bmRepeat.barCount) {
                    // einde repeat
                    pd.repeatListCounter++;
                    pd.bmRepeat = pd.bmTrack.repeatList.get(pd.repeatListCounter);
                    pd.bmPat = pd.bmTrack.patList.get(pd.bmRepeat.patSelected);
                    showRepeatInfo();
                }
            }
        }
        pd.bmBeat = pd.bmRepeat.beatList.get(pd.beatListCounter);
        pd.currentBeat = pd.bmBeat.beatIndex + 1;
        pd.nextBeat = getNextBeat();
        pd.bmSound = pd.bmBeat.soundList.get(pd.soundListCounter);
    }

    private void showStudyInfo() {

    }

    private void showRepeatInfo() {
        String msg = "REP: " + (pd.repeatListCounter + 1)
                + "/" + pd.bmTrack.repeatList.size();
        String patInfo = pd.bmPat.display(h, pd.bmRepeat.patSelected, true);
        msg += " " + pd.bmRepeat.display(h, pd.repeatListCounter, patInfo, true);
        h.logD(TAG, msg);
    }

    private void showBeatInfo() {
        String msg = "rep=" + pd.repeatListCounter;
        msg += " repBar=" + (pd.repeatBarCounter + 1)
                + ((pd.bmRepeat.noEnd) ? "" : "/" + pd.bmRepeat.barCount);
        msg += " beat[S] " + pd.beatListCounter + " info:"
                + pd.bmBeat.display(pd.beatListCounter, pd.subs);
        h.logD(TAG, msg);
    }

    private int getNextBeat() {
        int nextBeat = 0;
        if (pd.beatListCounter < pd.bmBeat.beats - 1) { // niet op de laatste beat: volgend beat
            nextBeat = 1;
            Log.d(TAG,"niet op laatste beat " + pd.beatListCounter +":" + pd.bmBeat.beats );
        } else {    // laatste beat
            if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                Log.d(TAG,"noEnd " + pd.beatListCounter +":" + pd.bmBeat.beats );
                nextBeat = 1 - pd.bmBeat.beats;
            } else {
                if (pd.repeatBarCounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                    Log.d(TAG,"laatste bar binnen repeat " + pd.repeatBarCounter +":" + pd.bmRepeat.barCount);
                    nextBeat = 1;
                } else { // naar 1 voor afspelen volgende bar
                    Log.d(TAG,"naar beat 1 voor volgende bar in repeat " + pd.repeatBarCounter +":" + pd.bmRepeat.barCount);
                    nextBeat = 1 - pd.bmBeat.beats;
                }
            }
        }
        return nextBeat;
    }

    private void playSoundList() {
        switch (pd.bmSound.soundType) {
            case Keys.SOUNDFIRST:
                writeSound(sc.soundFirst, pd.bmSound.duration);
                break;
            case Keys.SOUNDHIGH:
                writeSound(sc.soundHigh, pd.bmSound.duration);
                break;
            case Keys.SOUNDLOW:
                writeSound(sc.soundLow, pd.bmSound.duration);
                break;
            case Keys.SOUNDNONE:
                writeSound(sc.soundSilence, pd.bmSound.duration);
                break;
            case Keys.SOUNDSUB:
                writeSound(sc.soundSub, pd.bmSound.duration);
                break;
            case Keys.SOUNDSILENCE:
                writeSound(sc.soundSilence, pd.bmSound.duration);
                break;
            default:
                throw new RuntimeException("playBeat invalid soundtype " + pd.bmSound.soundType);
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