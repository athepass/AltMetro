package info.thepass.altmetro.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import info.thepass.altmetro.R;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackFragment extends Fragment {
    public final static String TAG = "TrackFragment";
    private HelperMetro h;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_metro, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        h = new HelperMetro(getActivity());
        h.logD(TAG, "activityCreated");
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                h.showToast("settings not yet implemented");
                return true;
            case R.id.action_tracklist:
                doTrackList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSelected(int selected) {
        getActivity().setTitle("sel=" + selected);
        h.showToast("Selectie=" + selected);
    }

    private void doTrackList() {
        TrackListFragment frag = new TrackListFragment();
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
