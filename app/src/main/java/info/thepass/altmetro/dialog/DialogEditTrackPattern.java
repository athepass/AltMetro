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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackPattern extends DialogFragment {
    public final static String TAG = "DialogEditTrakPattern";
    public HelperMetro h;
    private String oldTitle;
    private Pat pat;
    private boolean actionAdd;

    private Spinner spBeat;
    private ArrayAdapter<String> mBeatAdapter;
    private AdapterView.OnItemSelectedListener beatListener;
    private int lastBeatIndex;

    private Spinner spTime;
    private ArrayAdapter<String> mTimeAdapter;

    private Spinner spSub;
    private ArrayAdapter<String> mSubAdapter;

    private EmphasisViewManager evEditor;

    private EditText etTitel;

    public DialogEditTrackPattern() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        actionAdd = b.getBoolean(Keys.EDITACTION);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_pattern, null);
        initListener();
        initViews(dialogView);
        initEmphasis(dialogView);
        builder.setTitle((actionAdd)
                ? h.getString(R.string.label_addpattern)
                : h.getString(R.string.label_editpattern));
        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        boolean changed = false;
                        int newBeat = Integer.parseInt(spBeat.getSelectedItem().toString());
                        int newTime = Integer.parseInt(spTime.getSelectedItem().toString());
                        int newSubs = spSub.getSelectedItemPosition();
//                        if (pat.patBeats !=newBeat
//                            || pat.patTime!=newTime
//                            || pat.patSubs!=newSubs
//                        //TODO Data ophalen uit UI
                        pat.patTime = newTime;
                        pat.patBeats = newBeat;
                        pat.patSubs = newSubs;
                        pat.patTitle = etTitel.getText().toString();

                        Intent intent = new Intent();
                        intent.putExtra(TrackData.KEYTRACKS, pat.toJson().toString());
                        intent.putExtra(Keys.EDITACTION, actionAdd);
                        getTargetFragment().onActivityResult(Keys.TARGETEDITPATTERN, Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackPattern.this.getDialog().cancel();
                    }
                });


        try {
            pat = new Pat(h);
            pat.fromJson(new JSONObject(b.getString(Track.KEYPATS)));

            evEditor.setPattern(pat);
            lastBeatIndex = getbarBeatsIndex(pat.patBeats);
            spBeat.setSelection(lastBeatIndex);
            spTime.setSelection(getbarTimeIndex(pat.patTime));
            spSub.setSelection(pat.patSubs);
            etTitel.setText(pat.patTitle);
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
        return builder.create();
    }

    private void initListener() {
        beatListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (spBeat.getSelectedItemPosition() != lastBeatIndex) {
                    pat.patBeats = Integer.parseInt(spBeat.getSelectedItem()
                            .toString());
                    pat.initBeatStates();
                    evEditor.setPattern(pat);
                    lastBeatIndex = spBeat.getSelectedItemPosition();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
    }

    private void initViews(View dialogView) {
        etTitel = (EditText) dialogView.findViewById(R.id.et_tracktitel);

        String[] mBeatOpties = {"1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20"};
        mBeatAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, mBeatOpties);
        spBeat = (Spinner) dialogView.findViewById(R.id.spinnerBeat);
        spBeat.setAdapter(mBeatAdapter);
//        spBeat.setSelection(getbarBeatsIndex());
        spBeat.setSelection(0);
        spBeat.setOnItemSelectedListener(beatListener);

        String[] mTimeOpties = {"1", "2", "4", "8"};
        mTimeAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, mTimeOpties);
        spTime = (Spinner) dialogView.findViewById(R.id.spinnerTime);
        spTime.setAdapter(mTimeAdapter);
        spTime.setSelection(0);

        mSubAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, h.subPattern);
        spSub = (Spinner) dialogView.findViewById(R.id.spinnerSub);
        spSub.setAdapter(mSubAdapter);
        spSub.setSelection(0);
    }

    private void initEmphasis(View view) {
        evEditor = new EmphasisViewManager("ed_editor", h, false, view);
        evEditor.useLow = true;
    }

    private int getbarBeatsIndex(int barBeats) {
        return barBeats - 1;
    }

    private int getbarTimeIndex(int barTime) {
        switch (barTime) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
            case 4:
                return 2;
            case 5:
            case 6:
            case 7:
            case 8:
                return 3;
            default:
                return -1;
        }
    }
}