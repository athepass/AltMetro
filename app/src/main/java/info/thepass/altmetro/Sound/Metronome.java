package info.thepass.altmetro.Sound;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.tools.Keys;

public class Metronome implements Runnable {
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;

    public Metronome() {
        Log.d(TAGM, "constructor");
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
    }

    public void run() {
        long timeStart2 = getNanoTime();
        timeLayout1 = getNanoTime();
        getActivity().runOnUiThread(layoutUpdater);
        Canvas canvas = sh.lockCanvas(null);
        canvas.drawColor(Color.BLACK);
        sh.unlockCanvasAndPost(canvas);
        long timeStart3 = getNanoTime();


        h.logD(TAGM, "Run metronome t=" + deltaTime(timeStart1, timeStart2) + ".." + deltaTime(timeStart2, timeStart3));
        for (int irep = 0; irep < bmTrack.repeats.size(); irep++) {
            bmRepeat = bmTrack.repeats.get(irep);
            Pat pat = bmTrack.pats.get(bmTrack.patSelected);
            playRepeat();
        }

        Log.d(TAGM, "Run metronome finished");
        timeStop1 = getNanoTime();
        getActivity().runOnUiThread(stopper);
    }

    public void onPause() {
        Log.d(TAGM, "onPause");
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        Log.d(TAGM, "onResume");
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notify();
        }
    }

    private void playRepeat() {
        int iRepeat = 0;
        int step = 0;
        int barCounter = 0;
        while (playing && iRepeat < bmRepeat.barCount) {
            iBeatList = 0;
            while (playing && iBeatList < bmRepeat.beatList.size()) {
                Beat beat = bmRepeat.beatList.get(iBeatList);
                Log.d(TAGM, "beat[Sound] " + iBeatList + " info:" + bmRepeat.beatList.get(iBeatList).display(iBeatList, subs));
                timeBeat1 = getNanoTime();
                if (iBeatList < beat.beats - 1) { // niet op de laatste beat: volgend beat
                    step = 1;
                } else {    // laatste beat
                    if (bmRepeat.noEnd) {   // noend: altijd naar 1
                        step = 1 - beat.beats;
                    } else {
                        if (barCounter == bmRepeat.barCount - 1) { // laatste bar binnen repeat
                            step = 1;
                        } else { // naar 1 voor afspelen volgende bar
                            step = 1 - beat.beats;
                        }
                    }
                }
                playSoundList(beat);

                if (iBeatList == beat.beats - 1) { // bar counter ophogen
                    barCounter++;
                }

                iBeatList += step;
                if (iBeatList >= bmRepeat.beatList.size()) {
                    Log.d(TAGM, "beatSound ready");
                    playing = false;
                }
            }

            if (!bmRepeat.noEnd) {
                iRepeat++;
            }
        }
    }
    private void playSoundList(Beat beat) {
        for (int iSound = 0; iSound < beat.soundList.size(); iSound++) {
//                long nu = getNanoTime();
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
        playDuration = duration * 2;
        while (playing && playDuration > 0) {
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

    private void doDraw(Canvas canvas) {
//            canvas.restore();
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(20 + barCounter * 10, 20, 50, paintHigh);
        canvas.drawText("counter=" + barCounter, 20, 20, paintText);
    }

}