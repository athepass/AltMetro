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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
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
    private Button buttonTempoM1;
    private Button buttonTempoM5;
    private Button buttonTempoM20;
    private Button buttonTempoP1;
    private Button buttonTempoP5;
    private Button buttonTempoP20;
    private TextView tvTempo;

    private Button buttonCountM1;
    private Button buttonCountM5;
    private Button buttonCountM20;
    private Button buttonCountP1;
    private Button buttonCountP5;
    private Button buttonCountP20;
    private TextView tvCount;
    private CheckBox cbNoEnd;

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
        initTempo(dialogView);
        initCount(dialogView);
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
            repeat = new Repeat();
            String s = b.getString(Track.KEYREPEATS);
            repeat.fromJson(new JSONObject(s));

            pats = new ArrayList<Pat>();
            JSONArray patsArray = new JSONArray(b.getString(Track.KEYPATS));
            for (int i = 0; i < patsArray.length(); i++) {
                Pat pat = new Pat(h);
                pat.fromJson(patsArray.getJSONObject(i));
                pats.add(pat);
            }
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
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

    private void initTempo(View dialogView) {
        tvTempo = (TextView) dialogView.findViewById(R.id.tv_dlg_tempo);
        tvTempo.setText(String.valueOf(repeat.tempo));

        buttonTempoM1 = (Button) dialogView.findViewById(R.id.button_dlg_m1);
        buttonTempoM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-1);
            }
        });

        buttonTempoM5 = (Button) dialogView.findViewById(R.id.button_dlg_m5);
        buttonTempoM5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-5);
            }
        });
        buttonTempoM20 = (Button) dialogView.findViewById(R.id.button_dlg_m20);
        buttonTempoM20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(-20);
            }
        });
        buttonTempoP1 = (Button) dialogView.findViewById(R.id.button_dlg_p1);
        buttonTempoP1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(1);
            }
        });
        buttonTempoP5 = (Button) dialogView.findViewById(R.id.button_dlg_p5);
        buttonTempoP5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(5);
            }
        });
        buttonTempoP20 = (Button) dialogView.findViewById(R.id.button_dlg_p20);
        buttonTempoP20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigTempo(20);
            }
        });
    }

    private void initCount(View dialogView) {
        cbNoEnd = (CheckBox) dialogView.findViewById(R.id.cb_dlg_noend);
        cbNoEnd.setChecked(repeat.noEnd);

        tvCount = (TextView) dialogView.findViewById(R.id.tv_dlg_count);
        tvCount.setText(String.valueOf(repeat.count));

        buttonCountM1 = (Button) dialogView.findViewById(R.id.button_dlg_count_m1);
        buttonCountM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(-1);
            }
        });

        buttonCountM5 = (Button) dialogView.findViewById(R.id.button_dlg_count_m5);
        buttonCountM5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(-5);
            }
        });
        buttonCountM20 = (Button) dialogView.findViewById(R.id.button_dlg_count_m20);
        buttonCountM20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(-20);
            }
        });
        buttonCountP1 = (Button) dialogView.findViewById(R.id.button_dlg_count_p1);
        buttonCountP1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(1);
            }
        });
        buttonCountP5 = (Button) dialogView.findViewById(R.id.button_dlg_count_p5);
        buttonCountP5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(5);
            }
        });
        buttonCountP20 = (Button) dialogView.findViewById(R.id.button_dlg_count_p20);
        buttonCountP20.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wijzigCount(20);
            }
        });
    }

    private boolean validateData() {
        return true;
    }

    private void saveData() {
        repeat.indexPattern = spPat.getSelectedItemPosition();
        repeat.hashPattern = pats.get(repeat.indexPattern).patHash;
        repeat.tempo = Integer.parseInt(tvTempo.getText().toString());
        repeat.count = Integer.parseInt(tvCount.getText().toString());
        repeat.noEnd = cbNoEnd.isChecked();

        Intent intent = new Intent();
        intent.putExtra(Keys.EDITACTION, actionAdd);
        intent.putExtra(Keys.EDITINDEX, index);
        intent.putExtra(Track.KEYREPEATS, repeat.toJson().toString());
        Log.d(TAG, "save " + repeat.toJson().toString());
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

    }

    private void wijzigTempo(int iDelta) {
        String s = tvTempo.getText().toString();
        int newTempo = h.validatedTempo(Integer.parseInt(s) + iDelta);
        tvTempo.setText("" + newTempo);
    }

    private void wijzigCount(int iDelta) {
        String s = tvCount.getText().toString();
        int newCount = Integer.parseInt(s) + iDelta;
        newCount = (newCount < 1) ? 1 : newCount;
        tvCount.setText("" + newCount);
    }

    private void updatePat(int position) {
        spPat.setSelection(position);
        evmPats.setPattern(pats.get(position), false);
    }
}