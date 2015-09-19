package info.thepass.altmetro.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackTap extends DialogFragment {
    public final static String TAG = "DialogEditTrakStudy";
    public HelperMetro h;
    private int position;
    private int index = 0;

    private Button btnTap;
    private TextView tvTap;
    private Repeat repeat;
    private boolean firstTap;
    private long time1;
    private long time2;
    private int newTempo;
    private int maxTempo;

    public DialogEditTrackTap() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_tap, null);
        h.initToastAlert(inflater);

        initData();
        initView(dialogView);

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String sTempo = tvTap.getText().toString();
                        if (sTempo.length() > 0) {
                            newTempo = Integer.parseInt(sTempo);
                            if (newTempo >= Keys.MINTEMPO && newTempo <= maxTempo) {
                                repeat.tempo = newTempo;
                                Intent intent = new Intent();
                                intent.putExtra(Keys.EDITINDEX, index);
                                intent.putExtra(Track.KEYREPEATS, repeat.toJson().toString());
                                getTargetFragment().onActivityResult(Keys.TARGETEDITTAP, Activity.RESULT_OK, intent);
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackTap.this.getDialog().cancel();
                    }
                });

        String dlgTitle = h.getString(R.string.label_edittap) + " "
                + h.getString(R.string.label_repeat) + " " + (index + 1);
        builder.setTitle(dlgTitle);

        return builder.create();
    }

    private void initData() {
        maxTempo = h.getMaxTempo();

        Bundle b = getArguments();
        index = b.getInt(Keys.EDITINDEX);
        try {
            repeat = new Repeat(h);
            String s = b.getString(Track.KEYREPEATS);
            repeat.fromJson(new JSONObject(s));
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }

        firstTap = true;
    }

    private void initView(View dialogView) {
        btnTap = (Button) dialogView.findViewById(R.id.buttonTap);
        btnTap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getTap();
            }
        });
        tvTap = (TextView) dialogView.findViewById(R.id.tv_tap);
    }

    private void getTap() {
        if (firstTap) {
            tvTap.setText("");
            time1 = h.getTimeMillis();
            newTempo = -1;
        } else {
            time2 = h.getTimeMillis();
            newTempo = Math.round(60000f / (time2 - time1));
            if (newTempo < Keys.MINTEMPO) {
                h.showToastAlert(h.getString2(R.string.error_taptempolow, "" + newTempo, "" + Keys.MINTEMPO));
                newTempo = Keys.MINTEMPO;
            } else if (newTempo > maxTempo) {
                h.showToastAlert(h.getString2(R.string.error_taptempohigh, "" + newTempo, "" + maxTempo));
                newTempo = maxTempo;
            }
            tvTap.setText(String.valueOf(newTempo));
        }
        firstTap = !firstTap;
    }
}