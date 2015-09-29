package info.thepass.altmetro.aaaUI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class PrefsFragment extends PreferenceFragment {
    public final static String TAG = "PrefsFragment";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HelperMetro h = new HelperMetro(getActivity());
        addPreferencesFromResource(R.xml.preferences);
        getActivity().setTitle(h.getString(R.string.pref_title));
    }
    public void onDetach() {
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(Keys.TARGETPREF, Activity.RESULT_OK, intent);
        super.onDetach();
    }
}