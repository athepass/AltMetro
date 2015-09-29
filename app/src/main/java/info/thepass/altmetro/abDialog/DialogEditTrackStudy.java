package info.thepass.altmetro.abDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Study;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackStudy extends DialogFragment {
    public final static String TAG = "DialogEditTrakStudy";
    public HelperMetro h;
    private String oldTitle;
    private Study study;
    private int tempo;
    private NumberPicker npFrom;
    private NumberPicker npTo;
    private NumberPicker npIncrement;
    private NumberPicker npRounds;
    private ToggleButton tbUsed;
    //    private Button btnInit;
    private int[] waardes = {10, 20, 30, 40, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120};
    private String[] percentages = {"10%", "20%", "30%", "40%", "50%", "55%", "60%", "65%", "70%", "75%",
            "80%", "85%", "90%", "95%", "100%", "105%", "110%", "115%", "120%"};
    ;

    public DialogEditTrackStudy() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_study, null);
        h.initToastAlert(inflater);

        initView(dialogView);
        initData();

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (validateData())
                            updateData();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        DialogEditTrackStudy.this.getDialog().cancel();
                    }
                });

        String dlgTitle = h.getString1(R.string.label_editstudy, "" + tempo);
        builder.setTitle(dlgTitle);

        return builder.create();
    }

    private void initData() {
        Bundle b = getArguments();
        tempo = b.getInt(Repeat.KEYTEMPO, -1);
        String sStudy = b.getString(Track.KEYSTUDY);
        try {
            study = new Study();
            study.fromJson(new JSONObject(sStudy));
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
        npFrom.setValue(getPercentage(study.percentageFrom));
        npTo.setValue(getPercentage(study.percentageTo));
        npIncrement.setValue(study.percentageIncr);
        npRounds.setValue(study.times);
        tbUsed.setChecked(study.used);
    }

    private boolean validateData() {
        if (npFrom.getValue() >= npTo.getValue()) {
            h.showToastAlert(h.getString(R.string.error_studyfromto));
            return false;
        }
        return true;
    }

    private void updateData() {
        study.percentageFrom = waardes[npFrom.getValue()];
        study.percentageTo = waardes[npTo.getValue()];
        Log.d(TAG, "study update" + study.percentageFrom + ".." + study.percentageTo);
        study.percentageIncr = npIncrement.getValue();
        study.times = npRounds.getValue();
        study.used = tbUsed.isChecked();

        Intent intent = new Intent();
        intent.putExtra(Track.KEYSTUDY, study.toJson().toString());
        getTargetFragment().onActivityResult(Keys.TARGETEDITSTUDY, Activity.RESULT_OK, intent);
    }

    private void initView(View dialogView) {
        NumberPicker.OnValueChangeListener npChange = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                tbUsed.setChecked(true);
            }
        };

        tbUsed = (ToggleButton) dialogView.findViewById(R.id.tbDlgStudy);

        npFrom = (NumberPicker) dialogView.findViewById(R.id.npDlgTempoFrom);
        npFrom.setMinValue(0);
        npFrom.setMaxValue(percentages.length - 1);
        npFrom.setDisplayedValues(percentages);
        npFrom.setWrapSelectorWheel(false);
        npFrom.setOnValueChangedListener(npChange);

        npTo = (NumberPicker) dialogView.findViewById(R.id.npDlgTempoTo);
        npTo.setMinValue(0);
        npTo.setMaxValue(percentages.length - 1);
        npTo.setDisplayedValues(percentages);
        npTo.setWrapSelectorWheel(false);
        npTo.setOnValueChangedListener(npChange);

        npIncrement = (NumberPicker) dialogView
                .findViewById(R.id.npDlgTempoIncrement);
        npIncrement.setMinValue(1);
        npIncrement.setMaxValue(50);
        npIncrement.setWrapSelectorWheel(false);
        npIncrement.setOnValueChangedListener(npChange);

        npRounds = (NumberPicker) dialogView.findViewById(R.id.npDlgTempoCount);
        npRounds.setMinValue(1);
        npRounds.setMaxValue(50);
        npRounds.setWrapSelectorWheel(false);
        npRounds.setOnValueChangedListener(npChange);
    }

    private int getPercentage(int percent) {
        int idx = 0;
        for (int i = 0; i < waardes.length; i++) {
            if (percent >= waardes[i]) {
                idx = i;
                Log.d(TAG, "getPerc" + i + ":" + idx + "/" + percent + "/" + waardes[idx]);
            }
        }
        return idx;
    }
}