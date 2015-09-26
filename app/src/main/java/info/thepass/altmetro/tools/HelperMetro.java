package info.thepass.altmetro.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import info.thepass.altmetro.R;

public class HelperMetro {
    public final static String KEYPREMIUM = "premium";
    public final static int PREMIUMUNKNOWN = 0;
    public final static int NOTPREMIUM = 1;
    public final static int ISPREMIUM = 2;
    private final static int TYPED = 0;
    private final static int TYPEE = 1;
    private final static int TYPEW = 2;
    private final static int TYPEI = 3;
    private static String TAG = "helperMetro";
    public Context context;
    public SharedPreferences prefs = null;
    public boolean activityRunning = false;
    public ProgressDialog progressDialog;
    public boolean telefoonAlexander = false;
    public String[] tempUnit = null;
    public String[] subPattern;
    public int[] subValue;
    private Resources resources;
    private Random random;
    private View toastLayout;
    private TextView toastText;
    private int premium = PREMIUMUNKNOWN;
    /*********************************************************************************/
    private String logFileName = null;
    private File logFile = null;

    public HelperMetro(Context cont) {
        context = cont;
        resources = context.getResources();
        random = new Random();
        initPrefs();
        initAlexander();
    }

    private void initPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        premium = prefs.getInt(KEYPREMIUM, PREMIUMUNKNOWN);
        subPattern = context.getResources().getStringArray(R.array.sub_pattern);
        subValue = context.getResources().getIntArray(R.array.sub_value);
    }

    private void initAlexander() {
        String androidId = ""
                + android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        telefoonAlexander = (androidId != null && androidId
                .equals("1926d253b68016ba"));
    }

    public int getHash() {
        return random.nextInt(1000000);
    }

    public boolean isPremium() {
        return (premium == ISPREMIUM);
    }

    public int getPremium() {
        return premium;
    }

    public void setPremium(boolean newPremium) {
        premium = (newPremium) ? ISPREMIUM : NOTPREMIUM;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEYPREMIUM, premium);
        editor.commit();
    }

    public Activity getActivity() {
        Activity act = (Activity) context;
        return act;
    }

    public long getTimeMillis() {
        return System.nanoTime() / 1000000;
    }

    public long getRelTime(long t, long beginTime) {
        return t - beginTime;
    }

    public long getRelTimeNow(long beginTime) {
        return getTimeMillis() - beginTime;
    }

    public int getSubIndex(int subs) {
        for (int i = 0; i < subValue.length; i++) {
            if (subs == subValue[i]) {
                return i;
            }
        }
        return -1;
    }

    public int getResIdentifier(String viewName, String resName) {
        return getActivity().getResources().getIdentifier(viewName, resName,
                getActivity().getPackageName());
    }

    public String getIabKey() {
        String s = "";
        AssetManager mngr = context.getAssets();
        ArrayList<String> delen = new ArrayList<String>();
        try {
            InputStream is = mngr.open("premium");
            int len = -1;
            if (is != null) {
                InputStreamReader chapterReader = new InputStreamReader(is);
                BufferedReader buffreader = new BufferedReader(chapterReader);
                String line;
                do {
                    line = buffreader.readLine();
                    if (len == -1) {
                        len = Integer.parseInt(line);
                    } else {
                        if (line != null && line.length() > 0) {
                            delen.add(0, line);
                        }
                    }
                } while (line != null);
                is.close();
                int l2 = delen.size();
                for (int i = 0; i < l2; i++) {
                    String t = delen.get(i);
                    s += t.substring(4, 8) + t.substring(0, 4);
                }

            }
        } catch (Exception e) {
            Log.d(TAG, "getIabKey exception" + e.getMessage(), e);
        }
        return s;
    }

    public void showToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public void initToastAlert(LayoutInflater inflater) {
        toastLayout = inflater.inflate(R.layout.toast_alert,
                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
        toastText = (TextView) toastLayout.findViewById(R.id.text);
    }

    public void showToastAlert(String msg) {
        toastText.setText(msg);
        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    public void dumpFragmentManager(Activity act) {
        act.getFragmentManager().dump("", null,
                new PrintWriter(System.out, true), null);
    }

    public int getColor(int id) {
        return resources.getColor(id);
    }

    public int getMaxTempo() {
        return Integer.parseInt(prefs.getString(Keys.PREFMAXTEMPO, Keys.MAXTEMPODEFAULT));
    }

    public int validatedTempo(int newTempo) {
        int testedTempo = (newTempo < Keys.MINTEMPO) ? Keys.MINTEMPO: newTempo;
        testedTempo = (testedTempo > getMaxTempo()) ? getMaxTempo() : testedTempo;
        return testedTempo;
    }

    public String alfaNum(int i) {
        if (i < 26) {
            return Character.toString((char) (97 + i));
        } else if (i < 52) {
            return "a" + Character.toString((char) (97 + (i - 26)));
        } else if (i < 78) {
            return "b" + Character.toString((char) (97 + (i - 52)));
        } else if (i < 104) {
            return "c" + Character.toString((char) (97 + (i - 78)));
        } else if (i < 130) {
            return "d" + Character.toString((char) (97 + (i - 104)));
        } else
            return String.valueOf(i);
    }

    public Drawable getDrawable(int id) {
        return resources.getDrawable(id);
    }

    // public Drawable getDrawableChecked(boolean isChecked) {
    // if (isChecked) {
    // return context.getResources().getDrawable(R.drawable.list_shape_checked);
    // } else {
    // return
    // context.getResources().getDrawable(R.drawable.list_shape_unchecked);
    // }
    // }
    //
    public String getString(int id) {
        return resources.getString(id);
    }

    public String[] getStringArray(int id) {
        return resources.getStringArray(id);
    }

    public String getString1(int id, String arg1) {
        String s = resources.getString(id);
        return String.format(s, arg1);
    }

    public String getString2(int id, String arg1, String arg2) {
        String s = resources.getString(id);
        return String.format(s, arg1, arg2);
    }

    public String getString3(int id, String arg1, String arg2, String arg3) {
        String s = resources.getString(id);
        return String.format(s, arg1, arg2, arg3);
    }

    public String getString4(int id, String arg1, String arg2, String arg3,
                             String arg4) {
        String s = resources.getString(id);
        return String.format(s, arg1, arg2, arg3, arg4);
    }

    public void writeToLog(int typ, String TAG, String msg, Throwable tr) {
        if ((telefoonAlexander) && (logFileName == null)) {
            openLogFile();
        }
        String m = null;
        switch (typ) {
            case TYPED:
                Log.d(TAG, msg);
                m = "d ";
                break;
            case TYPEW:
                Log.w(TAG, msg);
                m = "w ";
                break;
            case TYPEI:
                Log.i(TAG, msg);
                m = "i ";
                break;
            case TYPEE:
                Log.i(TAG, msg, tr);
                m = "e ";
                break;
        }
        if (telefoonAlexander) {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new FileWriter(logFile, true));
                bufferedWriter.write(m + TAG + " " + msg + "\n");
                bufferedWriter.close();
            } catch (Exception e) {
                Log.e(TAG, "write " + logFileName + ": " + e.getMessage());
            }
        }
    }

    public void dumpStringArrayToLog(String label, ArrayList<String> tekst) {
        String s = "";
        for (int i = 0; i < tekst.size(); i++) {
            if (i == 0) {
                s += tekst.get(i);
            } else {
                s += "\n" + tekst.get(i);
            }
        }
        dumpToLog(label, s);
    }

    public void dumpToLog(String label, String tekst) {
        if ((telefoonAlexander)) {
            if (logFileName == null) {
                openLogFile();
            }
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(
                        new FileWriter(logFile, true));
                bufferedWriter.write("\n======== " + label + " ========\n"
                        + tekst + "\n");
                bufferedWriter.close();
            } catch (Exception e) {
                Log.e(TAG, "write " + logFileName + ": " + e.getMessage());
            }
        }
    }

    public String getLogFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/AlthLog/altmetro/";
    }

    public String getLogFileName() {
        Time t = new Time();
        t.setToNow();
        String fileTimeStamp = t.format("%Y%m%d");
        return getLogFilePath() + fileTimeStamp + "_log.txt";
    }

    public void openLogFile() {
        String pad = getLogFilePath();
        File p = new File(pad);
        @SuppressWarnings("unused")
        boolean res = p.mkdirs();
        // Log.d(TAG, "openLog " + res);
        logFileName = getLogFileName();
        logFile = new File(logFileName);
        // Log.i(TAG, "logfile="+ logFileName);
    }

    public void logD(String TAG, String msg) {
        writeToLog(TYPED, TAG, msg, null);
    }

    public void logE(String TAG, String msg, Throwable tr) {
        writeToLog(TYPEE, TAG, msg, tr);
    }

    public void logE(String TAG, String msg) {
        writeToLog(TYPEE, TAG, msg, null);
    }

    public void logI(String TAG, String msg) {
        writeToLog(TYPEI, TAG, msg, null);
    }

    public void logW(String TAG, String msg) {
        writeToLog(TYPEW, TAG, msg, null);
    }
}
