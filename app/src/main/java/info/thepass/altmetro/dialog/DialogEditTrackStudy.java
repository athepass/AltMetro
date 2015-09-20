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
    private Button btnInit;

    public DialogEditTrackStudy() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_study, null);

        initView(dialogView);
        initData(true);

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        study.tempoFrom = npFrom.getValue();
                        study.tempoTo = npTo.getValue();
                        study.tempoIncrement = npIncrement.getValue();
                        study.rounds = npRounds.getValue();
                        study.used = tbUsed.isChecked();

                        Intent intent = new Intent();
                        intent.putExtra(Track.KEYSTUDY, study.toJson().toString());
                        getTargetFragment().onActivityResult(Keys.TARGETEDITSTUDY, Activity.RESULT_OK, intent);
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

    private void initData(boolean fromArgs) {
        if (fromArgs) {
            Bundle b = getArguments();
            tempo = b.getInt(Repeat.KEYTEMPO, -1);
            String sStudy = b.getString(Track.KEYSTUDY);
            try {
                study = new Study();
                study.fromJson(new JSONObject(sStudy));
            } catch (Exception e) {
                h.logE(TAG, "from Json", e);
            }
        }
        if (!fromArgs || (study.rounds <= 0)) {
            study.used = true;
            study.tempoFrom = Math.round(tempo * 0.8f);
            study.tempoTo = Math.round(tempo * 1.2f);
            study.tempoIncrement = 5;
            study.rounds = 4;
        }

        npFrom.setValue(study.tempoFrom);
        npTo.setValue(study.tempoTo);
        npIncrement.setValue(study.tempoIncrement);
        npRounds.setValue(study.rounds);
        tbUsed.setChecked(study.used);
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
        npFrom.setMinValue(Keys.MINTEMPO);
        npFrom.setMaxValue(Keys.MAXTEMPOMAX);
        npFrom.setWrapSelectorWheel(false);
        npFrom.setOnValueChangedListener(npChange);

        npTo = (NumberPicker) dialogView.findViewById(R.id.npDlgTempoTo);
        npTo.setMinValue(Keys.MINTEMPO);
        npTo.setMaxValue(Keys.MAXTEMPOMAX);
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

        btnInit = (Button) dialogView.findViewById(R.id.btnDlgInit);
        btnInit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initData(false);
            }
        });


    }
}