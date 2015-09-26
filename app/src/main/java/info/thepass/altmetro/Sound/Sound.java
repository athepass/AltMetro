package info.thepass.altmetro.Sound;

public class Sound implements Comparable<Sound> {
    public int frameBegin;
    public int frameEnd;
    public int frameBeginBase;
    public int frameEndBase;
    public int duration;
    public int soundType;
    public String tag;

    @Override
    public int compareTo(Sound sound2) {
        if (frameBegin < sound2.frameBegin) {
            return -1;
        } else if (frameBegin > sound2.frameBegin) {
            return 1;
        } else {
            if (frameEnd < sound2.frameEnd) {
                return -1;
            } else if (frameEnd > sound2.frameEnd) {
                return +1;
            } else {
                return 0;
            }
        }
    }

    public String displayKort() {
        String s = tag
                + frameBegin
                + ((frameEnd == frameEndBase) ? ".." + frameEnd : "..("
                + frameEndBase + ") " + frameEnd) + "|" + duration;
        s += (tag != null) ? " (" + tag + ")" : "";
        return s;
    }

    public Sound split(Sound sNext) {
        Sound sound = this.kloon();
        sound.frameBegin = sNext.frameBegin;
        sound.calcDuration();

        this.frameEnd = sNext.frameBegin;
        this.calcDuration();
        return sound;
    }

    public Sound kloon() {
        Sound sound = new Sound();
        sound.frameBeginBase = frameBeginBase;
        sound.frameEndBase = frameEndBase;
        sound.copyBase();
        sound.calcDuration();
        sound.soundType = soundType;
        sound.tag = "split";
        return sound;
    }

    public void copyBase() {
        frameBegin = frameBeginBase;
        frameEnd = frameEndBase;
    }

    public void calcDuration() {
        duration = frameEnd - frameBegin;
    }
}