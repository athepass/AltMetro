package info.thepass.altmetro.Audio;

/**
 * Created by nl03192 on 20-9-2015.
 */
public class Beat {
    // #rep
    int repeatSize;
    int repeatIndex;
    int barCount;
    int barIndex;
    int beats;
    int beatIndex;
    int beatState;
    int sub;

    public String display(int seq , String[] subs) {
        String s = "";
        s += "#"+ seq + " ";
        s += " rep:" + repeatSize + "|" + repeatIndex;
        s += " bar:" + barCount + "|" + barIndex;
        s += " beat:" + beatIndex + "|" + beatState;
        s += " sub:" + subs[sub];
        return s;
    }
}
