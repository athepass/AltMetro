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
    private Track track;
    private EditText etNummer;
    private EditText etTitel;
    private RadioGroup rgMulti;
    private RadioButton rbSingle;
    private RadioButton rbMulti;

    public DialogEditTrackInfo() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_info, null);
        etTitel = (EditText) dialogView.findViewById(R.id.edittrack_titel);
        etNummer = (EditText) dialogView.findViewById(R.id.edittrack_number);
        rgMulti = (RadioGroup) dialogView.findViewById(R.id.edittrack_rg);
        rbMulti = (RadioButton) dialogView.findViewById(R.id.edittrack_multi);
        rbSingle = (RadioButton) dialogView.findViewById(R.id.edittrack_single);


        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // ophalen waardes uit view
                        track.titel = etTitel.getText().toString();
                        track.nummer = Integer.parseInt(etNummer.getText().toString());
                        track.multi = (rgMulti.getCheckedRadioButtonId() == R.id.edittrack_multi);

                        // igv single verwijzen naar 1e pattern en 1e oorder
                        // 1e order moet oneindig doorgaan: count <=0
                        if (!track.multi) {
                            track.patSelected = 0;
                            track.orderSelected = 0;
                            track.orders.get(0).count = 0;
                        }

                        Intent intent = new Intent();
                        intent.putExtra(TrackData.KEYTRACKS, track.toJson().toString());
                        getTargetFragment().onActivityResult(Keys.TARGETTRACKLIST, Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackInfo.this.getDialog().cancel();
                    }
                });


        Bundle b = getArguments();
        try {
            track = new Track(h);
            track.fromJson(new JSONObject(b.getString(TrackData.KEYTRACKS)));
            etNummer.setText(String.valueOf(track.nummer));
            etTitel.setText(track.titel);
            rbSingle.setChecked(!track.multi);
            rbMulti.setChecked(track.multi);
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
        return builder.create();
    }

 }