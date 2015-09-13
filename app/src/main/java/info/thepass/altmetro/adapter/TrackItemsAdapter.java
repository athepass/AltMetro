package info.thepass.altmetro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Order;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;

public class TrackItemsAdapter extends ArrayAdapter<String> {
    private final static String TAG = "TrakItemsAdapter";
    private final static int TYPESTUDY = 0;
    private final static int TYPEORDER = 1;
    private final static int TYPEORDERADD = 2;
    private final static int TYPEPAT = 3;
    private final static int TYPEPATADD = 4;
    public int selectedPat = -1;
    public int selectedOrder = -1;
    private Context context;
    private HelperMetro h;
    private Track track;

    public TrackItemsAdapter(Context cont, int layout,
                             Track track, HelperMetro hConstructor) {
        super(cont, layout, track.items);
        h = hConstructor;
        h.logD(TAG, "constructor");
        context = cont;
        this.track = track;
    }

    @Override
    public int getCount() {
        return track.items.size();
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItemType(position);
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPESTUDY:
                return getViewStudy(position, convertView, parent);
            case TYPEORDER:
                return getViewOrder(position, convertView, parent);
            case TYPEORDERADD:
                return getViewOrderAdd(position, convertView, parent);
            case TYPEPAT:
                return getViewPat(position, convertView, parent);
            case TYPEPATADD:
                return getViewPatAdd(position, convertView, parent);
            default:
                return null;
        }
    }

    private View getViewStudy(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderStudy holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_study_row,
                    parent, false);
            holder = new ViewHolderStudy();
            holder.tv_tap = (TextView) rowView.findViewById(R.id.tv_track_study_tap);
            holder.tv_tap.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("tap button clicked", "**********");
                    Toast.makeText(context, "TAP  button Clicked",
                            Toast.LENGTH_SHORT).show();
                }
            });
            holder.tv_study = (TextView) rowView.findViewById(R.id.tv_track_study_study);
            holder.tv_study.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("tap button clicked", "**********");
                    Toast.makeText(context, "STUDY  button Clicked",
                            Toast.LENGTH_SHORT).show();
                }
            });
            holder.tv_practice = (TextView) rowView.findViewById(R.id.tv_track_study_practice);
            holder.tv_practice.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("tap button clicked", "**********");
                    Toast.makeText(context, "PRACTICE  button Clicked",
                            Toast.LENGTH_SHORT).show();
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderStudy) rowView.getTag();
        }

        return rowView;
    }

    private View getViewOrder(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderOrder holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_order_row,
                    parent, false);
            holder = new ViewHolderOrder();
            holder.titel = (TextView) rowView.findViewById(R.id.tv_track_order_titel);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderOrder) rowView.getTag();
        }

        String s = "";
        int index  = getItemOrderPosition(position);
        Order order = track.orders.get(index);
        s = "order " + order.toString();
        if (index == selectedOrder) {
            s = ">> " + s;
        }
        holder.titel.setText(s);
        return rowView;
    }

    private View getViewOrderAdd(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderOrderAdd holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_orderadd_row,
                    parent, false);
            holder = new ViewHolderOrderAdd();
            holder.rij = (LinearLayout) rowView.findViewById(R.id.ll_track_orderadd);
            holder.titel = (TextView) rowView.findViewById(R.id.tv_track_orderadd_titel);
            holder.titel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("tap button clicked", "**********");
                    Toast.makeText(context, "OrderAdd  button Clicked",
                            Toast.LENGTH_SHORT).show();
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderOrderAdd) rowView.getTag();
        }

        holder.rij.setVisibility((track.multi) ? LinearLayout.VISIBLE:  LinearLayout.GONE);
        String s = "add order";
        holder.titel.setText(s);
        return rowView;
    }
    private View getViewPat(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderPat holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_pat_row,
                    parent, false);
            holder = new ViewHolderPat();
            holder.titel = (TextView) rowView.findViewById(R.id.tv_track_pat_titel);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPat) rowView.getTag();
        }

        String s = "";
        int index = getItemPatPosition(position);
        Pat pat = track.pats.get(index);
        s = "pat " + pat.toString();
        if (index == selectedPat) {
            s = ">> " + s;
        }
        holder.titel.setText(s);
        return rowView;
    }

    private View getViewPatAdd(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderPatAdd holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_patadd_row,
                    parent, false);
            holder = new ViewHolderPatAdd();
            holder.rij = (LinearLayout) rowView.findViewById(R.id.ll_track_patadd);
            holder.titel = (TextView) rowView.findViewById(R.id.tv_track_patadd_titel);
            holder.titel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("tap button clicked", "**********");
                    Toast.makeText(context, "Pattern ADD button Clicked",
                            Toast.LENGTH_SHORT).show();
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPatAdd) rowView.getTag();
        }

        holder.rij.setVisibility((track.multi) ? LinearLayout.VISIBLE:  LinearLayout.GONE);
        holder.titel.setText("add pattern");
        return rowView;
    }

    private int getItemOrderPosition(int position) {
        return position - 1;
    }

    private int getItemPatPosition(int position) {
        return position - 1 - track.orders.size() - 1;
    }

    private int getItemType(int position) {
        String s;
        if (position == 0) {
            return TYPESTUDY;
        } else if (position < (1 + track.orders.size())) {
            return TYPEORDER;
        } else if (position == (1 + track.orders.size())) {
            return TYPEORDERADD;
        } else if (position < (1 + track.orders.size() + 1 + track.pats.size())) {
            return TYPEPAT;
        } else if (position == (1 + track.orders.size() + 1 + track.pats.size())) {
            return TYPEPATADD;
        } else {
            throw new RuntimeException("invalid item type at position " + position);
        }
    }

    public static class ViewHolderStudy {
        public LinearLayout rij;
        public TextView tv_tap;
        public TextView tv_study;
        public TextView tv_practice;
    }
    public static class ViewHolderOrder {
        public LinearLayout rij;
        public TextView titel;
    }
    public static class ViewHolderOrderAdd {
        public LinearLayout rij;
        public TextView titel;
    }
    public static class ViewHolderPat {
        public LinearLayout rij;
        public TextView titel;
    }
    public static class ViewHolderPatAdd {
        public LinearLayout rij;
        public TextView titel;
    }
}