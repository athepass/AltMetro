package info.thepass.altmetro.dialog;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackRepeat extends DialogFragment {
    public final static String TAG = "DialogEditTrakRepeat";
    public HelperMetro h;
    private Repeat repeat;
    private ArrayList<Pat> pats;
    private boolean actionAdd;
    private int position;
    private int index = 0;
    private int editSize = -1;
    private boolean multi;

    private LinearLayout llSpinner;
    private LinearLayout llEmphasis;
    private EmphasisViewManager evmPats;

    private Spinner spPat;
    private ArrayAdapter<String> patSelAdapter;
    private AdapterView.OnItemSelectedListener patSelListener;
    private EditText etCount;
    private EditText etTempo;

    public DialogEditTrackRepeat() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_repeat, null);
        h.initToastAlert(inflater);

        initData();
        initEmphasis(dialogView);
        initViews(dialogView);
        initSpinner(dialogView);

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackRepeat.this.getDialog().cancel();
                    }
                });

        String dlgTitle = (actionAdd)
                ? h.getString(R.string.label_addrepeat)
                : h.getString(R.string.label_editrepeat) + " r" + (index + 1);
        builder.setTitle(dlgTitle);

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Log.d(TAG, "onClick");
                        if (validateData()) {
                            saveData();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

    private void initData() {
        Bundle b = getArguments();
        actionAdd = b.getBoolean(Keys.EDITACTION);
        index = b.getInt(Keys.EDITINDEX);
        editSize = b.getInt(Keys.EDITSIZE);
        multi = b.getBoolean(Track.KEYMULTI);

        try {
            repeat = new Repeat(h);
            String s = b.getString(Track.KEYREPEATS);
            repeat.fromJson(new JSONObject(s));

            pats = new ArrayList<Pat>();
            JSONArray patsArray = new JSONArray(b.getString(TrackData.KEYPATS));
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                pats.add(pat);
            }
            Log.d(TAG, "init pats=" + pats.size());
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
    }

    private boolean validateData() {
        int tempo = Integer.parseInt(etTempo.getText().toString());
        if (tempo < 30) {
            String msg = h.getString2(R.string.error_tempomin, "" + tempo, "" + 30);
            h.showToastAlert(msg);
            return false;
        } else if (tempo > 200) {
            String msg = h.getString2(R.string.error_tempomax, "" + tempo, "" + 200);
            h.showToastAlert(msg);
            return false;
        } else {
            return true;
        }
    }

    private void saveData() {
        //todo update repeat
        repeat.indexPattern = spPat.getSelectedItemPosition();
        repeat.hashPattern = pats.get(repeat.indexPattern).patHash;
        repeat.count = Integer.parseInt(etCount.getText().toString());
        repeat.tempo = Integer.parseInt(etTempo.getText().toString());
        Intent intent = new Intent();
        intent.putExtra(Keys.EDITACTION, actionAdd);
        intent.putExtra(Keys.EDITINDEX, index);
        intent.putExtra(Track.KEYREPEATS, repeat.toJson().toString());
        getTargetFragment().onActivityResult(Keys.TARGETEDITREPEAT, Activity.RESULT_OK, intent);
    }

    private void initEmphasis(View dialogView) {
        evmPats = new EmphasisViewManager("dlgrepeatpat", Keys.EVMLIST, dialogView, h);
        evmPats.useLow = true;
    }

    private void initViews(View dialogView) {
        llSpinner = (LinearLayout) dialogView.findViewById((R.id.ll_spinnerPat));
        llSpinner.setVisibility((multi) ? View.VISIBLE : View.GONE);

        llEmphasis = (LinearLayout) dialogView.findViewById((R.id.ll_dlgrepeatpat_emphasis));
        llEmphasis.setVisibility((multi) ? View.VISIBLE : View.GONE);

        etCount = (EditText) dialogView.findViewById(R.id.et_dlg_repeat_count);
        etCount.setText(String.valueOf(repeat.count));
        etTempo = (EditText) dialogView.findViewById(R.id.et_dlg_repeat_tempo);
        etTempo.setText(String.valueOf(repeat.tempo));
    }

    private void initSpinner(View dialogView) {
        ArrayList<String> patsList = new ArrayList<String>();
        for (int i = 0; i < pats.size(); i++) {
            patsList.add(pats.get(i).display(h, i, true));
        }

        patSelListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                updatePat(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };

        patSelAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, patsList);
        spPat = (Spinner) dialogView.findViewById(R.id.spinnerPat);
        spPat.setAdapter(patSelAdapter);
        spPat.setOnItemSelectedListener(patSelListener);
        updatePat(repeat.indexPattern);
    }

    private void updatePat(int position) {
        spPat.setSelection(position);
        evmPats.setPattern(pats.get(position));
    }
}