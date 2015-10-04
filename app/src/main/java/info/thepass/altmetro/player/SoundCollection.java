package info.thepass.altmetro.player;

import info.thepass.altmetro.tools.HelperMetro;

public class SoundCollection {
	private final static String TAG = "trak:SoundCollection";
	public final static int SAMPLERATE = 8000;
	private HelperMetro h;
	private final static double FREQFIRST = 523.25;
	private final static double FREQHIGH = 659.25;
	private final static double FREQLOW = 880;
	private final static double FREQSUB = 1046.5;
	private final static double FREQSILENCE = 0;
	public final static int TICKDURATION = 30 * 8; // samples of tick
	public final static int SOUNDLENGTH = 200 * 8;

	public byte[] soundHigh;
	public byte[] soundLow;
	public byte[] soundSub;
	public byte[] soundFirst;
	public byte[] soundSilence;

	public SoundCollection(HelperMetro hh, String tag) {
		h = hh;
		initSounds(tag);
	}

	private void initSounds(String tag) {
		h.logD(TAG, "initSounds " + tag);
		soundHigh = buildSound(FREQHIGH, SOUNDLENGTH);
		soundLow = buildSound(FREQLOW, SOUNDLENGTH);
		soundSub = buildSound(FREQSUB, SOUNDLENGTH);
		soundFirst = buildSound(FREQFIRST, SOUNDLENGTH);
		soundSilence = buildSound(FREQSILENCE, SOUNDLENGTH);
	}

	private byte[] buildSound(double freq, int duration) {
		double[] samples = new double[duration];
		for (int i = 0; i < duration; i++) {
			if (freq > 0) {
				samples[i] = Math.sin(2 * Math.PI * i / (SAMPLERATE / freq));
			} else {
				samples[i] = 0;
			}
		}
		byte[] result = get16BitPcm(samples);
		return result;
	}

	public byte[] get16BitPcm(double[] samples) {
		byte[] generatedSound = new byte[2 * samples.length];
		int index = 0;
		for (double sample : samples) {
			// scale to maximum amplitude
			short maxSample = (short) ((sample * Short.MAX_VALUE));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSound[index++] = (byte) (maxSample & 0x00ff);
			generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);
		}
		return generatedSound;
	}
}