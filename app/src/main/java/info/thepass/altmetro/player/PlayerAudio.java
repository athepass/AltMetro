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
    public Player bm;
    public PlayerData pd;
    // runnable management
    public Object mPauseLock;
    public boolean mFinished;
    // sound management
    public int soundLength;
    public SoundCollection sc;
    public AudioTrack audioTrack;

    public PlayerAudio(HelperMetro hh, Player bm) {
        h = hh;
        this.bm = bm;
        this.pd = bm.pd;
        Log.d(TAG, "constructor");
        mPauseLock = new Object();
        pd.mPaused = true;
        pd.mPlaying = false;
        mFinished = false;

        initAudio();
    }

    public void run() {
        initRun();

        while (!mFinished) {
            if (pd.mPlaying) {
                doStep();
            }
            doWait();
        }
        finishRun();
    }

    private void doStep() {
        initRun();
        for (int irep = 0; irep < pd.bmTrack.repeats.size(); irep++) {
            pd.bmRepeat = pd.bmTrack.repeats.get(irep);
            pd.bmPat = pd.bmTrack.pats.get(pd.bmTrack.patSelected);
            playRepeat();
        }
        finishRun();
    }

    private void doWait() {
        synchronized (mPauseLock) {
            while (pd.mPaused) {
                Log.d(TAG, "pauselock");
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        synchronized (mPauseLock) {
            pd.mPaused = true;
        }
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        synchronized (mPauseLock) {
            pd.mPaused = false;
            mPauseLock.notify();
        }
    }

    private void initPlay() {
        pd.timeStart2 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.layoutUpdater);
        pd.timeLayout1 = h.getNanoTime();
        pd.timeStart3 = h.getNanoTime();
        h.logD(TAG, "start t=" + h.deltaTime(pd.timeStart1, pd.timeStart2)
                + ".." + h.deltaTime(pd.timeStart1, pd.timeStart3));
    }
    
    private void finishPlay() {
        Log.d(TAG, "finish");
        pd.timeStop1 = h.getNanoTime();
        bm.getActivity().runOnUiThread(bm.stopper);
    }
    
    private void playRepeat() {
        int iRepeat = 0;
        int step = 0;
        int barCounter = 0;
        while (pd.mPlaying && iRepeat < pd.bmRepeat.barCount) {
            pd.iBeatList = 0;
            while (pd.mPlaying && pd.iBeatList < pd.bmRepeat.beatList.size()) {
                Beat beat = pd.bmRepeat.beatList.get(pd.iBeatList);
                pd.bmBeat = beat.beatIndex + 1;
                Log.d(TAG, "beat[Sound] " + pd.iBeatList + " info:" + pd.bmRepeat.beatList.get(pd.iBeatList).display(pd.iBeatList, pd.subs));
                pd.timeBeat1 = h.getNanoTime();
                if (pd.iBeatList < beat.beats - 1) { // niet op de laatste beat: volgend beat
                    step = 1;
                } else {    // laatste beat
                    if (pd.bmRepeat.noEnd) {   // noend: altijd naar 1
                        step = 1 - beat.beats;
                    } else {
                        if (barCounter == pd.bmRepeat.barCount - 1) { // laatste bar binnen repeat
                            step = 1;
                        } else { // naar 1 voor afspelen volgende bar
                            step = 1 - beat.beats;
                        }
                    }
                }
                playSoundList(beat);

                if (pd.iBeatList == beat.beats - 1) { // bar counter ophogen
                    barCounter++;
                }

                pd.iBeatList += step;
                if (pd.iBeatList >= pd.bmRepeat.beatList.size()) {
                    Log.d(TAG, "beatSound ready");
                    pd.mPlaying = false;
                }
            }

            if (!pd.bmRepeat.noEnd) {
                iRepeat++;
            }
        }
    }

    private void playSoundList(Beat beat) {
        for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
//                long nu = h.getNanoTime();
//                Log.d(TAG,"sound"+iSound + " time:"+deltaTime(timeBeat1,nu));
            Sound sound = beat.soundList.get(iSound);
//                if (sound.playBeat) {
//                    getActivity().runOnUiThread(beatUpdater);
//                }

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
        while (pd.mPlaying && playDuration > 0) {
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
        Log.d(TAG, "start Runnable");
    }

    private void finishRun() {
        Log.i(TAG, "finish Runnable");
    }


}