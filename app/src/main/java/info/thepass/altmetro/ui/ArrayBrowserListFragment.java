package info.thepass.altmetro.ui;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import info.thepass.altmetro.tools.HelperMetro;

public class ArrayBrowserListFragment extends ListFragment {
    private final static String TAG = "LogFileBrowserFragment";
    public final static String ROW = "row";
    public final static String TITEL =  "titel";
    private HelperMetro h;
    ArrayList<String> rows;
    String titel;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        h = new HelperMetro(getActivity());
        h.logD(TAG, "onActivityCreated");

        Bundle b = getArguments();
        rows = b.getStringArrayList(ROW);
        titel = b.getString(TITEL, "???");
        getActivity().setTitle(titel);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, rows);
        getListView().setAdapter(adapter);
    }
}