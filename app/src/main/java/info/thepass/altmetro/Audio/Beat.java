package info.thepass.altmetro.Audio;

import java.util.ArrayList;

import info.thepass.altmetro.tools.Keys;

/**
 * Created by nl03192 on 20-9-2015.
 */
public class Beat {
    // #rep
    public boolean noEnd;
    public int repeatCount;     // repeat.count
    public int repeatIndex;     // 1..repeat.count
    public int barIndex;        // bm.barCounter
    public int barNext;
    public int beats;
    public int beatIndex;
    public int beatState;
    public int sub;
    public int tempo;
    public int practice;
    public int tempoCalc;
    public int totFrames;
    public String info;
    public ArrayList<Sound> soundList;
    private boolean soundFirst;

    public Beat(boolean soundFirstP) {
        soundList = new ArrayList<Sound>();
        soundFirst = soundFirstP;
    }

    public void buildSound() {
        totFrames = Math.round(60f * SoundCollection.SAMPLERATE / tempoCalc);
        addBeat();
        addSub();
        removeOverlap();
        fillGaps();
        fillEndGap();
    }

    private void addBeat() {
        Sound sound = new Sound();
        soundList.add(sound);
        sound.duration = SoundCollection.TICKDURATION;
        sound.frameBeginBase = 0;
        sound.frameEndBase = sound.duration;
        sound.copyBase();
        if (soundFirst && beatIndex == 1) {
            sound.soundType = Keys.SOUNDFIRST;
            sound.tag = "F";
        } else {
            sound.soundType = beatState;
            switch (beatState) {
                case Keys.SOUNDHIGH:
                    sound.tag = "BH";
                    break;
                case Keys.SOUNDLOW:
                    sound.tag = "BL";
                    break;
                case Keys.SOUNDNONE:
                    sound.tag = "BN";
                    break;
                default:
                    throw new RuntimeException("invalid beatState " + beatState);
            }
        }
    }

    private void addSub() {
        if (sub != Keys.SUBDEFAULT) {
            int[] subBeats = getSubPattern(sub);
            int subFrames = totFrames / subBeats.length;
            for (int j = 1; j < subBeats.length; j++) {
                if (subBeats[j] == 1) {
                    Sound sound = new Sound();
                    soundList.add(sound);
                    sound.duration = SoundCollection.TICKDURATION;
                    sound.frameBeginBase = j * subFrames;
                    sound.frameEndBase = sound.frameBeginBase
                            + sound.duration;
                    sound.copyBase();
                    sound.soundType = Keys.SOUNDSUB ;
                    sound.tag = "S" + j;
                }
            }
        }
    }

    private void removeOverlap() {
        for (int i = 0; i < soundList.size() - 1; i++) {
            Sound sThis = soundList.get(i);
            Sound sNext = soundList.get(i + 1);
            if (sThis.frameEnd > sNext.frameBegin) {
                sThis.frameEnd = sNext.frameBegin;
                sThis.calcDuration();
            }
        }
    }

    private void fillGaps() {
        for (int i = 0; i < soundList.size() - 1; i++) {
            Sound sThis = soundList.get(i);
            Sound sNext = soundList.get(i + 1);
            if (sThis.frameEnd < sNext.frameBegin) {
                this.addSilence(sThis.frameEnd, sNext.frameBegin, i + 1);
            }
        }
    }

    private void fillEndGap() {
        int i = soundList.size() - 1;
        Sound sThis = soundList.get(i);
        if (sThis.frameEnd > totFrames) {
            sThis.frameEnd = totFrames;
            sThis.calcDuration();
        }
        if (sThis.frameEnd < totFrames) {
            this.addSilence(sThis.frameEnd, totFrames, i + 1);
        }
    }

    private void addSilence(int frameFrom, int frameTo, int position) {
        Sound sNew = new Sound();
        soundList.add(position, sNew);
        sNew.frameBeginBase = frameFrom;
        sNew.frameEndBase = frameTo;
        sNew.copyBase();
        sNew.calcDuration();
        sNew.soundType = Keys.SOUNDSILENCE;
        sNew.tag = "Q";
    }

    public String display(int seq, String[] subs) {
        String s = "";
        s += "#" + (seq + 1) + " ";
        s += " rep:" + repeatCount + "|" + repeatIndex;
        s += " bar:" + barIndex + "|" + barNext;
        s += " beat:" + beats + "|" + beatIndex + "|" + beatState;
        s += " sub:" + subs[sub];
        s += " tempo:" + tempo + "|" + practice + "|" + tempoCalc;
        return s;
    }

    public String displayBeat() {
        String s = "";
        for (int i = 0; i < soundList.size(); i++) {
            s += soundList.get(i).displayKort() + "|";
            s += (i % 3 == 0 && i > 0) ? "\n" : "";
        }
        return s;
    }

    private int[] getSubPattern(int i) {
        switch (i) {
            case 1:
                int[] a21 = {1, 1};
                return a21;
            case 2:
                int[] a31 = {1, 1, 1};
                return a31;
            case 3:
                int[] a32 = {1, 0, 1};
                return a32;
            case 4:
                int[] a33 = {1, 1, 0};
                return a33;
            case 5:
                int[] a41 = {1, 1, 1, 1};
                return a41;
            case 6:
                int[] a42 = {1, 0, 0, 1};
                return a42;
            case 7:
                int[] a43 = {1, 0, 1, 1};
                return a43;
            case 8:
                int[] a44 = {1, 1, 0, 1};
                return a44;
            case 9:
                int[] a45 = {1, 1, 1, 0};
                return a45;
            case 10:
                int[] a46 = {1, 1, 0, 0};
                return a46;
            case 11:
                int[] a51 = {1, 1, 1, 1, 1};
                return a51;
            default:
                throw new RuntimeException("getSubPattern i invalid "+i);
        }
    }
}
