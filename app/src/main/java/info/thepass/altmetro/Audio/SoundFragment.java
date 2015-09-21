package info.thepass.altmetro.Audio;

import android.app.Fragment;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import info.thepass.altmetro.tools.HelperMetro;

public class SoundFragment extends Fragment {
    public final static String TAG = "Trak.SoundFragment";
    private HelperMetro h;
    private SoundCollection sc;
    private SoundManager soundManager;
    private AudioTrack audioTrack;
    private MetronomeAsyncTask metroTask;

    private Handler mHandler;
    private int beatRunnable;

    private long timeRunning;
    private long timeSound;
    private long beginTimeSound = -1;
    private long stopTimeSound = -1;
    private long beginTimeOnClick = -1;

    private boolean eersteRun = true;
    private boolean doorgaan = true;
    private long uiDelay;
    private long uiNextStop;
    private long uiDelayShift;

	public void startPlayer(Bundle b) {
	}

	public void stopPlayer() {
	}

    public void initSound() {
        sc = new SoundCollection(h, "TrakPAT");

        metroTask = new MetronomeAsyncTask();
        soundManager = new SoundManager(h);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SoundCollection.SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, SoundCollection.SAMPLERATE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    Runnable runTimer = new Runnable() {
        public void run() {
            timeRunning = h.getRelTimeNow(beginTimeOnClick);
            if (eersteRun) {
//                h.logI(TAG, "runTimer eerste keer");
                eersteRun = false;
//                beatRunnable = 1;
//                uiNextStop = Math.round(barSounds.beatFrames / 8f)
//                        + uiDelayShift;
//                b.putInt(Keys.KEYBEAT, beatRunnable);
//                b.putInt(Keys.KEYTEMPO, Math.round(barSounds.tempoP));
//                b.putString(Keys.KEYBARINFO, "seq:" + soundManager.seqInfo()
//                        + " bar:" + barSounds.counterInfo());
//                mCallback.onFragmentAction(Keys.CMD_BEATPAT,
//                        Keys.FRAGPATEDITOR, b);
            } else {
//                b.putInt(Keys.KEYBEAT, beatRunnable);
//                b.putString(Keys.KEYBARINFO, "seq:" + soundManager.seqInfo()
//                        + " bar:" + barSounds.counterInfo());
//                mCallback.onFragmentAction(Keys.CMD_BEATPAT,
//                        Keys.FRAGPATEDITOR, b);
//                uiNextStop += Math.round(barSounds.beatFrames / 8f);
//                uiDelay = uiNextStop - timeRunning;
            }
            uiDelay = 1000;
            String s = "tRun=" + timeRunning;
            s += (eersteRun) ? "[1]" : "";
            s += " nStop=" + uiNextStop;
            s += " delay=" + uiDelay;
            s += " beat=" + beatRunnable;
//            s += " beats=" + barSounds.barBeats;
            h.logD(TAG, s);
//            beatRunnable = (beatRunnable < barSounds.barBeats) ? beatRunnable + 1
//                    : 1;
            if (doorgaan) {
                mHandler.postDelayed(runTimer, uiDelay);
            }
        }
    };


    /*****************************************************************/

	private class MetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

		public MetronomeAsyncTask() {
			h.logD(TAG, "MetronomeAsyncTask constructor");
		}

		protected String doInBackground(Void... params) {
			h.logD(TAG, "doInBackground");
			eersteRun = true;
			mHandler.post(runTimer);
//			do {
//				playBarSoundsList();
//			} while (doorgaan);
			return null;
		}
	}
}
