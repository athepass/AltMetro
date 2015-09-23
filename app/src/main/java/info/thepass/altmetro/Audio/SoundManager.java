package info.thepass.altmetro.Audio;

import java.util.ArrayList;

import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class SoundManager {
    public final static String TAG = "SoundManager";
    public ArrayList<BarSounds> barSoundsList;
    public int seqCounter;
    private HelperMetro h;

    public SoundManager(HelperMetro hh) {
        h = hh;
        barSoundsList = new ArrayList<BarSounds>();
    }

    public String seqInfo() {
        return (seqCounter + 1) + "/" + barSoundsList.size();
    }

    public void build(Track track, boolean buildPattern) {
        barSoundsList.clear();
//		if (data.sps.used) {
//			BarPattern p = data.getBarPatternSelected();
//			buildSps(data.sps, p);
//		} else {
        Repeat repeat = track.repeats.get(0);
        Pat p = track.getPats().get(repeat.indexPattern);
        BarSounds brs = new BarSounds(p.patBeats, p.patBeatState, 0);
        brs.buildTempo(repeat.tempo, 100, p.patSubs);
        barSoundsList.add(brs);
//		}
    }

//    public void buildSps(SpeedStudy sps, Pat p) {
//        BarSounds brs = null;
//        h.logD(TAG, "sps=" + sps.toString());
//        int i = sps.tempoFrom;
//        while (i <= sps.tempoTo) {
//            brs = new BarSounds(p.barBeats, p.beatState, sps.rounds);
//            brs.buildTempo(i, 100, p.subs);
//            brs.info = "sps " + String.valueOf(i) + ": " + sps.toStringKort();
//            barSoundsList.add(brs);
//            i += sps.tempoIncrement;
//        }
//        // laatste regel blijven loopen
//        brs.rondes = 0;
//    }
//
    private int[] getSubPattern(int i) {
        switch (i) {
            case 21:
                int[] a21 = {1, 1};
                return a21;
            case 31:
                int[] a31 = {1, 1, 1};
                return a31;
            case 32:
                int[] a32 = {1, 0, 1};
                return a32;
            case 33:
                int[] a33 = {1, 1, 0};
                return a33;
            case 41:
                int[] a41 = {1, 1, 1, 1};
                return a41;
            case 42:
                int[] a42 = {1, 0, 0, 1};
                return a42;
            case 43:
                int[] a43 = {1, 0, 1, 1};
                return a43;
            case 44:
                int[] a44 = {1, 1, 0, 1};
                return a44;
            case 45:
                int[] a45 = {1, 1, 1, 0};
                return a45;
            case 46:
                int[] a46 = {1, 1, 0, 0};
                return a46;
            case 51:
                int[] a51 = {1, 1, 1, 1, 1};
                return a51;
        }
        return null;
    }

    public class BarSounds {
        public String info;
        public int rondes;
        public int beatFrames;
        public int barBeats;
        public int tempo;
        public float tempoP;
        public int subFrames;
        public int practice;
        public int[] beatState;
        public ArrayList<BeatSounds> beatSoundsList;
        public int rondeCounter;
        public int beatCounter;

        public BarSounds(int beats, int[] states, int ronde) {
            beatSoundsList = new ArrayList<BeatSounds>();
            barBeats = beats;
            beatState = states;
            rondes = ronde;
        }

        public void buildTempo(int ttempo, int ppractice, int subs) {
            tempo = ttempo;
            practice = ppractice;
            tempoP = (practice * tempo) / 100f;
            beatFrames = Math.round(60f * SoundCollection.SAMPLERATE / tempoP);
            h.logD(TAG, "t=" + tempo + ",prac=" + practice + ",subs=" + subs
                    + ",tP=" + tempoP + ",bFr=" + beatFrames);
            buildBeat();
            buildSub(subs);
            // dump("subs");
            removeOverlap();
            // dump("overlap");
            fillGaps();
            // dump("fillGaps");
            fillEndGap();
            // dump("buildTempo ready");
            // h.logD(TAG, "buildTempo");
        }

        private void buildBeat() {
            BeatSounds sound = new BeatSounds();
            beatSoundsList.add(sound);
            sound.duration = SoundCollection.TICKDURATION;
            sound.frameBeginBase = 0;
            sound.frameEndBase = sound.duration;
            sound.copyBase();
            sound.soundType = Keys.SOUNDTYPEBEAT;
            sound.tag = "beat";
        }

        private void buildSub(int subs) {
            if (subs != Keys.SUBDEFAULT) {
                int[] notes = getSubPattern(subs);
                subFrames = beatFrames / notes.length;
                for (int j = 1; j < notes.length; j++) {
                    if (notes[j] == 1) {
                        BeatSounds sound = new BeatSounds();
                        beatSoundsList.add(sound);
                        sound.duration = SoundCollection.TICKDURATION;
                        sound.frameBeginBase = j * subFrames;
                        sound.frameEndBase = sound.frameBeginBase
                                + sound.duration;
                        sound.copyBase();
                        sound.soundType = Keys.SOUNDTYPESUB;
                        sound.tag = "sub  " + j;
                    }
                }
            }
        }

        private void removeOverlap() {
            for (int i = 0; i < beatSoundsList.size() - 1; i++) {
                BeatSounds sThis = beatSoundsList.get(i);
                BeatSounds sNext = beatSoundsList.get(i + 1);
                if (sThis.frameEnd > sNext.frameBegin) {
                    sThis.frameEnd = sNext.frameBegin;
                    sThis.calcDuration();
                }
            }
        }

        private void fillGaps() {
            for (int i = 0; i < beatSoundsList.size() - 1; i++) {
                BeatSounds sThis = beatSoundsList.get(i);
                BeatSounds sNext = beatSoundsList.get(i + 1);
                if (sThis.frameEnd < sNext.frameBegin) {
                    this.addSilence(sThis.frameEnd, sNext.frameBegin, i + 1);
                }
            }
        }

        private void fillEndGap() {
            int i = beatSoundsList.size() - 1;
            BeatSounds sThis = beatSoundsList.get(i);
            if (sThis.frameEnd > beatFrames) {
                sThis.frameEnd = beatFrames;
                sThis.calcDuration();
            }
            if (sThis.frameEnd < beatFrames) {
                this.addSilence(sThis.frameEnd, beatFrames, i + 1);
            }
        }

        private void addSilence(int frameFrom, int frameTo, int position) {
            BeatSounds sNew = new BeatSounds();
            beatSoundsList.add(position, sNew);
            sNew.frameBeginBase = frameFrom;
            sNew.frameEndBase = frameTo;
            sNew.copyBase();
            sNew.calcDuration();
            sNew.soundType = Keys.SOUNDTYPESILENCE;
            sNew.tag = "addSilence";
        }

        public void dump(String info) {
            h.logD(TAG, "========" + info + "========");
            for (int i = 0; i < beatSoundsList.size(); i++) {
                BeatSounds sound = beatSoundsList.get(i);
                h.logD(TAG, "[" + i + "] :" + sound.toString());
            }
        }

        public String counterInfo() {
            String s = "";
            if (this.rondes > 0) {
                s += info;
                s += " " + (this.rondeCounter + 1) + "/" + this.rondes;
            }
            return s;
        }
    }

    public class BeatSounds implements Comparable<BeatSounds> {
        public int frameBegin;
        public int frameEnd;
        public int frameBeginBase;
        public int frameEndBase;
        public int duration;
        public int soundType;
        public String tag;

        @Override
        public int compareTo(BeatSounds bs2) {
            if (frameBegin < bs2.frameBegin) {
                return -1;
            } else if (frameBegin > bs2.frameBegin) {
                return 1;
            } else {
                if (frameEnd < bs2.frameEnd) {
                    return -1;
                } else if (frameEnd > bs2.frameEnd) {
                    return +1;
                } else {
                    return 0;
                }
            }
        }

        @Override
        public String toString() {
            String s = "";
            switch (soundType) {
                case Keys.SOUNDTYPEBEAT:
                    s += "beat";
                    break;
                case Keys.SOUNDTYPESUB:
                    s += "sub";
                    break;
                case Keys.SOUNDTYPESILENCE:
                    s += "sil";
                    break;
            }
            s += " "
                    + frameBegin
                    + ((frameEnd == frameEndBase) ? ".." + frameEnd : "..("
                    + frameEndBase + ") " + frameEnd) + "|" + duration;
            s += (tag != null) ? " (" + tag + ")" : "";
            return s;
        }

        public BeatSounds split(BeatSounds sNext) {
            BeatSounds bs = this.kloon();
            bs.frameBegin = sNext.frameBegin;
            bs.calcDuration();

            this.frameEnd = sNext.frameBegin;
            this.calcDuration();
            return bs;
        }

        public BeatSounds kloon() {
            BeatSounds bs = new BeatSounds();
            bs.frameBeginBase = frameBeginBase;
            bs.frameEndBase = frameEndBase;
            bs.copyBase();
            bs.calcDuration();
            bs.soundType = soundType;
            bs.tag = "split";
            return bs;
        }

        public void copyBase() {
            frameBegin = frameBeginBase;
            frameEnd = frameEndBase;
        }

        public void calcDuration() {
            duration = frameEnd - frameBegin;
        }
    }
}
