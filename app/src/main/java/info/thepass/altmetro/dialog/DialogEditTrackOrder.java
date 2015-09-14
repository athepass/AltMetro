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
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Order;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackOrder extends DialogFragment {
    public final static String TAG = "DialogEditTrakOrder";
    public HelperMetro h;
    private String oldTitle;
    private Order order;
    private boolean actionAdd;
    private boolean multi;

    private LinearLayout llSpinner;
    private Spinner spPat;
    private ArrayAdapter<String> patSelAdapter;
    private AdapterView.OnItemSelectedListener patSelListener;
    private EditText etCount;

    public DialogEditTrackOrder() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        actionAdd = b.getBoolean(Keys.EDITACTION);
        multi = b.getBoolean(Track.KEYMULTI);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_order, null);
        initListener();
        initViews(dialogView);
        initEmphasis(dialogView);
        builder.setTitle((actionAdd)
                ? h.getString(R.string.label_addorder)
                : h.getString(R.string.label_editorder));

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.putExtra(Track.KEYORDERS, order.toJson().toString());
                        intent.putExtra(Keys.EDITACTION, actionAdd);
                        getTargetFragment().onActivityResult(Keys.TARGETEDITORDER, Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackOrder.this.getDialog().cancel();
                    }
                });


        try {
            order = new Order(h);
            order.fromJson(new JSONObject(b.getString(Track.KEYORDERS)));
//            spPat.setSelection(0);
            etCount.setText(order.count);
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
        return builder.create();
    }

    private void initListener() {
        patSelListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
//                if (spBeat.getSelectedItemPosition() != lastBeatIndex) {
//                    pat.patBeats = Integer.parseInt(spBeat.getSelectedItem()
//                            .toString());
//                    pat.initBeatStates();
//                    evEditor.setPattern(pat);
//                    lastBeatIndex = spBeat.getSelectedItemPosition();
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
    }

    private void initViews(View dialogView) {
        llSpinner = (LinearLayout) dialogView.findViewById((R.id.ll_spinnerPat));
        llSpinner.setVisibility((multi) ? View.VISIBLE : View.GONE);

        etCount = (EditText) dialogView.findViewById(R.id.et_track_order_count);

        String[] patOpties = {"0", "1", "2", "3", "4"};
        patSelAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item, patOpties);
        spPat = (Spinner) dialogView.findViewById(R.id.spinnerPat);
        spPat.setAdapter(patSelAdapter);
//        spBeat.setSelection(getbarBeatsIndex());
        spPat.setSelection(0);
        spPat.setOnItemSelectedListener(patSelListener);
    }

    private void initEmphasis(View view) {
//        evEditor = new EmphasisViewManager("ed_editor", h, false, view);
//        evEditor.useLow = true;
    }
}