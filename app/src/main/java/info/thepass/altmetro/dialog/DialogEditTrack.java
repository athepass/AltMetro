package info.thepass.altmetro.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.MetroData;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class DialogEditTrack extends DialogFragment {
    public final static String TAG = "DialogEditTrack";
    private String oldTitle;
    public HelperMetro h;
    public DialogFragment frag;

    public interface EditTrackListener {
        void onFinishEditTrack(Track track);
    }

    private EditText etNummer;
    private EditText etTitel;
    private Button buttonOK;

    public DialogEditTrack() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        h = new HelperMetro(getActivity());

        frag = this;
        View view = inflater.inflate(R.layout.dialog_edittrack, container);
        mEditText = (EditText) view.findViewById(R.id.txt_title);
        Bundle b = getArguments();
        try {
            JSONObject jTrack = new JSONObject(b.getString(MetroData.KEYTRACKS));
            Track track = new Track(h);
            track.fromJson(jTrack);
            mEditText.setText(track.titel);
            mEditText.requestFocus();
        } catch (Exception e) {
            h.logE(TAG,"json exception",e);
        }
//        getDialog().setTitle(h.getString(R.string.title_dlg_title));

        buttonOK = (Button) view.findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String newTitel = mEditText.getText().toString();
                if (!newTitel.equals(oldTitle)) {
                    BarTitleListener activity = (BarTitleListener) getActivity();
                    activity.onFinishEditBarTitle(newTitel);
                }
                frag.dismiss();
            }
        });

        return view;
    }
}