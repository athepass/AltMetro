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

import java.util.ArrayList;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Order;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class DialogEditTrackOrder extends DialogFragment {
    public final static String TAG = "DialogEditTrakOrder";
    public HelperMetro h;
    public Track track;
    private Order order;
    private boolean actionAdd;
    private int position;
    private int index = 0;
    private boolean multi;

    private LinearLayout llSpinner;
    private LinearLayout llEmphasis;
    private EmphasisViewManager evmPats;

    private Spinner spPat;
    private ArrayAdapter<String> patSelAdapter;
    private AdapterView.OnItemSelectedListener patSelListener;
    private EditText etCount;

    public DialogEditTrackOrder() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittrack_order, null);

        initData();
        initEmphasis(dialogView);
        initViews(dialogView);
        initSpinner(dialogView);

        builder.setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //todo update order
                        order.indexPattern = spPat.getSelectedItemPosition();
                        order.hashPattern = track.pats.get(order.indexPattern).patHash;
                        order.count = Integer.parseInt(etCount.getText().toString());
                        Intent intent = new Intent();
                        intent.putExtra(Track.KEYORDERS, order.toJson().toString());
                        intent.putExtra(Keys.EDITACTION, actionAdd);
                        intent.putExtra(Keys.EDITPOSITION, position);
                        getTargetFragment().onActivityResult(Keys.TARGETEDITORDER, Activity.RESULT_OK, intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditTrackOrder.this.getDialog().cancel();
                    }
                });


        return builder.create();
    }

    private void initData() {
        Bundle b = getArguments();
        actionAdd = b.getBoolean(Keys.EDITACTION);
        multi = b.getBoolean(Track.KEYMULTI);
        position = b.getInt(Keys.EDITPOSITION);
        index = b.getInt(Keys.EDITINDEX);
        try {
            order = new Order(h);
            String s = b.getString(Track.KEYORDERS);
            order.fromJson(new JSONObject(s));
        } catch (Exception e) {
            h.logE(TAG, "from Json", e);
        }
    }

    private void initEmphasis(View dialogView) {
        evmPats = new EmphasisViewManager("dlgorderpat", Keys.EVMLIST, dialogView, h);
        evmPats.useLow = true;
    }

    private void initViews(View dialogView) {
        llSpinner = (LinearLayout) dialogView.findViewById((R.id.ll_spinnerPat));
        llSpinner.setVisibility((multi) ? View.VISIBLE : View.GONE);

        llEmphasis = (LinearLayout) dialogView.findViewById((R.id.ll_dlgorderpat_emphasis));
        llEmphasis.setVisibility((multi) ? View.VISIBLE : View.GONE);

        etCount = (EditText) dialogView.findViewById(R.id.et_track_order_count);
        etCount.setText(String.valueOf(order.count));
    }

    private void initSpinner(View dialogView) {
        ArrayList<String> patsList = new ArrayList<String>();
        for (int i = 0; i < track.pats.size(); i++) {
            patsList.add(track.pats.get(i).toStringShort(h));
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
        updatePat(order.indexPattern);
    }

    private void updatePat(int position) {
        spPat.setSelection(position);
        evmPats.setPattern(track.pats.get(position));
    }
}