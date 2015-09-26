package info.thepass.altmetro.Audio;

import info.thepass.altmetro.tools.Keys;

public class Sound implements Comparable<Sound> {
    public int frameBegin;
    public int frameEnd;
    public int frameBeginBase;
    public int frameEndBase;
    public int duration;
    public int soundType;
    public String tag;

    @Override
    public int compareTo(Sound bs2) {
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

    public String display() {
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

    public String displayKort() {
        String s = "";
        switch (soundType) {
            case Keys.SOUNDTYPEBEAT:
                s += "B";
                break;
            case Keys.SOUNDTYPESUB:
                s += "S";
                break;
            case Keys.SOUNDTYPESILENCE:
                s += "Q";
                break;
        }
        s += " "
                + frameBegin
                + ((frameEnd == frameEndBase) ? ".." + frameEnd : "..("
                + frameEndBase + ") " + frameEnd) + "|" + duration;
        s += (tag != null) ? " (" + tag + ")" : "";
        return s;
    }

    public Sound split(Sound sNext) {
        Sound bs = this.kloon();
        bs.frameBegin = sNext.frameBegin;
        bs.calcDuration();

        this.frameEnd = sNext.frameBegin;
        this.calcDuration();
        return bs;
    }

    public Sound kloon() {
        Sound bs = new Sound();
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