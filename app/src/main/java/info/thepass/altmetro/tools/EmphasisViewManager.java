package info.thepass.altmetro.tools;

import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;

public class EmphasisViewManager {
    private final static String TAG = "EmphasisTrak";
    public boolean useLow = true;
    public Pat pat;
    public Track track;
    private HelperMetro h;
    private boolean settingMetroData;
    private int type;
    private int ivLast;
    private LinearLayout[] llEmphasis;
    private int llCount;
    private int llCountVisible;
    private ImageView ivArray[];
    private AnimationDrawable animArray[];
    private String prefix;
    private int[] ivIndexArray;
    private int[] ivReverseIndex;
    private int evmType;
    private boolean isPlayer;
    private View.OnClickListener emphasisListener;
    private View layout;

    public EmphasisViewManager(String prefx,
                               int type,
                               View llayout,
                               HelperMetro hh) {
        h = hh;
//        h.logD(TAG, "constructor " + prefx + " type=" + type);

        evmType = type;
        prefix = prefx;
        isPlayer = (evmType == Keys.EVMPLAYER);
        layout = llayout;

        switch (evmType) {
            case Keys.EVMEDITOR:
                llCount = 3;
                break;
            case Keys.EVMLIST:
                llCount = 1;
                break;
            case Keys.EVMPLAYER:
                llCount = 2;
                break;
            default:
                throw new RuntimeException("invalid evmType " + evmType);
        }

        // find View LinearLayout
        llEmphasis = new LinearLayout[llCount];
        for (int i = 0; i < llCount; i++) {
            String llId = "ll_" + prefix + "_emphasis" + i;
            int resId = h.getResIdentifier(llId, "id");
            llEmphasis[i] = (LinearLayout) layout.findViewById(resId);
        }

        // init button Listener
        emphasisListener = new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                incState(v);
            }
        };

        // find View ImageViews
        ivArray = new ImageView[Keys.MAXEMPHASIS];
        animArray = new AnimationDrawable[Keys.MAXEMPHASIS];
        ivReverseIndex = new int[Keys.MAXEMPHASIS];
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            String ivId = "iv_" + prefix + "_emphasis" + i;
            int resId = h.getResIdentifier(ivId, "id");
            ivArray[i] = (ImageView) layout.findViewById(resId);
            if (evmType == Keys.EVMEDITOR) {
                ivArray[i].setOnClickListener(emphasisListener);
            }
        }
    }

    private void incState(View v) {
        String s = (String) v.getTag();
        int i = Integer.parseInt(s.substring(2, 4));
        pat.patBeatState[i] = (pat.patBeatState[i] < Keys.SOUNDNONE) ? pat.patBeatState[i] + 1
                : Keys.SOUNDHIGH;
        ImageView iv = (ImageView) v;
        iv.setImageLevel(pat.patBeatState[i]);
    }

    public void setPattern(Pat pp, boolean isPlaying) {
        settingMetroData = true;
        pat = pp;
        setIvIndex();
        setImageVisibility();
        setLinLayoutVisibility(isPlaying);
        setViewState();
        settingMetroData = false;
    }

    private void setIvIndex() {
        // ivIndexArray bevat gebruikte views voor aantal maten.
        if (useLow) {
            ivIndexArray = getViewIndexLow(pat.patBeats);
        } else {
            ivIndexArray = getViewIndex(pat.patBeats);
        }
    }

    private void setImageVisibility() {
        // bepaal welke views niet gebruikt worden en zet deze op onzichtbaar
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            ivReverseIndex[i] = -1;
        }
        for (int i = 0; i < ivIndexArray.length; i++) {
            ivReverseIndex[ivIndexArray[i]] = i;
        }
        for (int i = 0; i < Keys.MAXEMPHASIS; i++) {
            if (ivReverseIndex[i] == -1) {
                ivArray[i].setVisibility(ImageView.INVISIBLE);
            } else {
                ivArray[ivIndexArray[i]].setVisibility(ImageView.VISIBLE);
            }
        }
    }

    private void setViewState() {
        // init state
        for (int i = 0; i < ivIndexArray.length; i++) {
            if (isPlayer) {
                switch (pat.patBeatState[i]) {
                    case Keys.SOUNDHIGH:
                        ivArray[ivIndexArray[i]]
                                .setBackgroundResource(R.drawable.sh_animlist_high_player);
                        break;
                    case Keys.SOUNDLOW:
                        ivArray[ivIndexArray[i]]
                                .setBackgroundResource(R.drawable.sh_animlist_low_player);
                        break;

                    case Keys.SOUNDNONE:
                        ivArray[ivIndexArray[i]]
                                .setBackgroundResource(R.drawable.sh_animlist_none_player);
                        break;
                }
                animArray[ivIndexArray[i]] = (AnimationDrawable) ivArray[ivIndexArray[i]]
                        .getBackground();
                animArray[ivIndexArray[i]].selectDrawable(0);
                ivArray[ivIndexArray[i]].setImageLevel(pat.patBeatState[i]);
            } else {
                ivArray[ivIndexArray[i]].setImageLevel(pat.patBeatState[i]);
                ivArray[ivIndexArray[i]]
                        .setImageResource(R.drawable.levellist_emphasis_editor);
            }
        }
        if (isPlayer) {
            for (int j = 0; j < llCount; j++) {
                llEmphasis[j].invalidate();
            }
        }
    }

    public void start() {
        ivLast = -1;
    }

    public void stop() {
        if (isPlayer) {
            // init state
            for (int i = 0; i < ivIndexArray.length; i++) {
                animArray[ivIndexArray[i]].selectDrawable(0);
            }
        } else {
            if (pat != null) {
                if (ivLast != -1) {
                    ivArray[ivIndexArray[ivLast]]
                            .setImageLevel(pat.patBeatState[ivLast]);
                    ivLast = -1;
                }
            }
        }
    }

    private void setLinLayoutVisibility(boolean isPlaying) {
        if (pat == null)
            return;
        if (evmType == Keys.EVMEDITOR) {
            if (pat.patBeats > 16) {
                llEmphasis[1].setVisibility(View.VISIBLE);
                llEmphasis[2].setVisibility(View.VISIBLE);
            } else if (pat.patBeats > 8) {
                llEmphasis[1].setVisibility(View.VISIBLE);
                llEmphasis[2].setVisibility(View.GONE);
            } else {
                llEmphasis[1].setVisibility(View.GONE);
                llEmphasis[2].setVisibility(View.GONE);
            }
        }
    }

    public void updateEmphasisView(int currentBeat) {
        if (isPlayer) {
            Log.d(TAG,"beat="+ currentBeat);
            checkLlVisibility();
//            ivLast = currentBeat - 1;
            animArray[ivIndexArray[currentBeat]].stop();
            animArray[ivIndexArray[currentBeat]].start();
        } else {
            if (ivLast != -1) {
                ivArray[ivIndexArray[ivLast]]
                        .setImageLevel(pat.patBeatState[ivLast]);
            }
            ivLast = currentBeat - 1;
            ivArray[ivIndexArray[ivLast]]
                    .setImageLevel(pat.patBeatState[ivLast] + 3);
        }
    }

    private void checkLlVisibility() {
        int vis = (pat.patBeats<=10)? View.GONE : View.VISIBLE;
        if (llEmphasis[1].getVisibility()!=vis) {
            llEmphasis[1].setVisibility(vis);
        }
    }

    public void setEmphasisVisible(boolean isPlayer) {
        for (int i = 0; i < llCount; i++)
            llEmphasis[i].setVisibility((isPlayer && (i < llCountVisible)) ? View.VISIBLE : View.GONE);
    }

    private int[] getViewIndex(int beatCount) {
        switch (beatCount) {
            case 1:
                int[] a1 = {0};
                return a1;
            case 2:
                int[] a2 = {0, 1};
                return a2;
            case 3:
                int[] a3 = {0, 1, 2};
                return a3;
            case 4:
                int[] a4 = {0, 1, 2, 3};
                return a4;
            case 5:
                int[] a5 = {0, 1, 2, 3, 4};
                return a5;
            case 6:
                int[] a6 = {0, 1, 2, 3, 4, 5};
                return a6;
            case 7:
                int[] a7 = {0, 1, 2, 3, 10, 11, 12};
                return a7;
            case 8:
                int[] a8 = {0, 1, 2, 3, 10, 11, 12, 13};
                return a8;
            case 9:
                int[] a9 = {0, 1, 2, 3, 4, 10, 11, 12, 13};
                return a9;
            case 10:
                int[] a10 = {0, 1, 2, 3, 4, 10, 11, 12, 13, 14};
                return a10;
            case 11:
                int[] a11 = {0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14};
                return a11;
            case 12:
                int[] a12 = {0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15};
                return a12;
            case 13:
                int[] a13 = {0, 1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15};
                return a13;
            case 14:
                int[] a14 = {0, 1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 16};
                return a14;
            case 15:
                int[] a15 = {0, 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16};
                return a15;
            case 16:
                int[] a16 = {0, 1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16,
                        17};
                return a16;
            case 17:
                int[] a17 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15,
                        16, 17};
                return a17;
            case 18:
                int[] a18 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15,
                        16, 17, 18};
                return a18;
            case 19:
                int[] a19 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16, 17, 18};
                return a19;
            case 20:
                int[] a20 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16, 17, 18, 19};
                return a20;
        }
        return null;
    }

    private int[] getViewIndexLow(int beatCount) {
        switch (beatCount) {
            case 1:
                int[] a1 = {0};
                return a1;
            case 2:
                int[] a2 = {0, 1};
                return a2;
            case 3:
                int[] a3 = {0, 1, 2};
                return a3;
            case 4:
                int[] a4 = {0, 1, 2, 3};
                return a4;
            case 5:
                int[] a5 = {0, 1, 2, 3, 4};
                return a5;
            case 6:
                int[] a6 = {0, 1, 2, 3, 4, 5};
                return a6;
            case 7:
                int[] a7 = {0, 1, 2, 3, 4, 5, 6};
                return a7;
            case 8:
                int[] a8 = {0, 1, 2, 3, 4, 5, 6, 7};
                return a8;
            case 9:
                int[] a9 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
                return a9;
            case 10:
                int[] a10 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                return a10;
            case 11:
                int[] a11 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                return a11;
            case 12:
                int[] a12 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
                return a12;
            case 13:
                int[] a13 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
                return a13;
            case 14:
                int[] a14 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
                return a14;
            case 15:
                int[] a15 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
                return a15;
            case 16:
                int[] a16 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
                return a16;
            case 17:
                int[] a17 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16};
                return a17;
            case 18:
                int[] a18 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16, 17};
                return a18;
            case 19:
                int[] a19 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16, 17, 18};
                return a19;
            case 20:
                int[] a20 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                        16, 17, 18, 19};
                return a20;

        }
        return null;
    }
}
