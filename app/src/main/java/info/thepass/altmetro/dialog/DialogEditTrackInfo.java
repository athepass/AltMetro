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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackInfo extends DialogFragment {
    public final static String TAG = "DialogEditTrakInfo";
    public HelperMetro h;
    private String oldTitle;
    private Track trak;
    private EditText etNummer;
    private EditText etTitel;
    private RadioGroup rgMulti;
    private RadioButton rbSingle;
    private RadioButton rbMulti;
    private boolean actionAdd;
    private int index = 0;
    private int editSize = -1;

    public DialogEditTrackInfo() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_info, null);

        initViews(dialogView);
        initData();
        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // ophalen waardes uit view
                        trak.titel = etTitel.getText().toString();
                        trak.nummer = Integer.parseInt(etNummer.getText().toString());
                        trak.multi = (rgMulti.getCheckedRadioButtonId() == R.id.edittrack_multi);

                        // igv single verwijzen naar 1e pattern en 1e repeat
                        // 1e repeat moet oneindig doorgaan: count <=0
                        if (!trak.multi) {
                            trak.repeatSelected = 0;
                            trak.repeats.get(0).count = 0;
                        }

                        Intent intent = new Intent();
                        intent.putExtra(Keys.EDITACTION, actionAdd);
                        intent.putExtra(Keys.EDITINDEX, index);
                        String sTrack = trak.toJson().toString();
                        intent.putExtra(TrackData.KEYTRACKS, sTrack);
                        getTargetFragment().onActivityResult(Keys.TARGETEDITTRACK, Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackInfo.this.getDialog().cancel();
                    }
                });

        if ((editSize > 1) && (!actionAdd)) {
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent();
                    intent.putExtra(Keys.EDITINDEX, index);
                    getTargetFragment().onActivityResult(Keys.TARGETDELETETRACK, Activity.RESULT_OK, intent);
                }
            });
        }

        String dlgTitle = (actionAdd)
                ? h.getString(R.string.label_addtrack)
                : h.getString(R.string.label_edittrack) + " t" + (index + 1);
        builder.setTitle(dlgTitle);

        return builder.create();
    }

    private void initData() {
        Bundle b = getArguments();
        actionAdd = b.getBoolean(Keys.EDITACTION);
        index = b.getInt(Keys.EDITINDEX);
        editSize = b.getInt(Keys.EDITSIZE);
        try {
            trak = new Track(h);
            String sTrack = b.getString(TrackData.KEYTRACKS);
            trak.fromJson(new JSONObject(sTrack), h);
            etNummer.setText(String.valueOf(trak.nummer));
            etTitel.setText(trak.titel);
            rbSingle.setChecked(!trak.multi);
            rbMulti.setChecked(trak.multi);
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
    }

    private void initViews(View dialogView) {
        etTitel = (EditText) dialogView.findViewById(R.id.edittrack_titel);
        etNummer = (EditText) dialogView.findViewById(R.id.edittrack_number);
        rgMulti = (RadioGroup) dialogView.findViewById(R.id.edittrack_rg);
        rbMulti = (RadioButton) dialogView.findViewById(R.id.edittrack_multi);
        rbSingle = (RadioButton) dialogView.findViewById(R.id.edittrack_single);
    }
}