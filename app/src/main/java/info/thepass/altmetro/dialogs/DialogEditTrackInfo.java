package info.thepass.altmetro.dialogs;

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
import android.widget.Switch;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.ui.ActivityTrack;
import info.thepass.altmetro.data.MetronomeData;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackInfo extends DialogFragment {
    public final static String TAG = "DialogEditTrakInfo";
    public HelperMetro h;
    private String oldTitle;
    private Track trak;
    private EditText etNummer;
    private EditText etTitel;
    private Switch swMulti;
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
                        if (validate())
                            updateData();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackInfo.this.getDialog().cancel();
                    }
                });

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
            ActivityTrack act = (ActivityTrack) getActivity();
            MetronomeData metronomeData = act.metronomeData;
            trak = new Track(h, metronomeData);
            String sTrack = b.getString(MetronomeData.KEYTRACKS);
            trak.fromJson(new JSONObject(sTrack), h);
            etNummer.setText(String.valueOf(trak.nummer));
            etTitel.setText(trak.titel);
            swMulti.setChecked(trak.multi);
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
    }

    private boolean validate() {
        return true;
    }

    private void updateData() {
        // ophalen waardes uit view
        trak.titel = etTitel.getText().toString();
        String sNummer = etNummer.getText().toString();
        trak.nummer = (sNummer.length() > 0) ? Integer.parseInt(sNummer): 0;

        trak.multi = (swMulti.isChecked());

        Intent intent = new Intent();
        intent.putExtra(Keys.EDITACTION, actionAdd);
        intent.putExtra(Keys.EDITINDEX, index);
        String sTrack = trak.toJson().toString();
        intent.putExtra(MetronomeData.KEYTRACKS, sTrack);
        getTargetFragment().onActivityResult(Keys.TARGETEDITTRACK, Activity.RESULT_OK, intent);
    }

    private void initViews(View dialogView) {
        etTitel = (EditText) dialogView.findViewById(R.id.edittrack_titel);
        etNummer = (EditText) dialogView.findViewById(R.id.edittrack_number);
        swMulti = (Switch) dialogView.findViewById(R.id.swDlgInfo_Multi);
    }
}