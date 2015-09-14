package info.thepass.altmetro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Order;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.ui.TrackFragment;

public class TrackItemsAdapter extends ArrayAdapter<String> {
    private final static String TAG = "TrakItemsAdapter";
    public final static int TYPESTUDY = 0;
    public final static int TYPEORDER = 1;
    public final static int TYPEPAT = 2;
    public final static int TYPEORDERADD = 3;
    public final static int TYPEPATADD = 4;
    public int selectedPat = 0;
    public int selectedOrder = 0;
    private Context context;
    private HelperMetro h;
    private Track track;
    private TrackFragment frag;

    public TrackItemsAdapter(Context cont, int layout,
                             Track track, HelperMetro hConstructor, TrackFragment frag2) {
        super(cont, layout, track.items);
        h = hConstructor;
        h.logD(TAG, "constructor");
        context = cont;
        this.track = track;
        frag = frag2;
    }

//    @Override
    public int getCount() {
        if (track.multi) {
            return track.items.size();
        } else {
            return 3;
        }
    }

    @Override
    public int getViewTypeCount() {
        return (track.multi) ? 5 : 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (track.multi) {
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
                throw new RuntimeException("invalid multi item type at position " + position);
            }
        } else {
            switch (position) {
                case 0:
                    return TYPESTUDY;
                case 1:
                    return TYPEORDER;
                case 2:
                    return TYPEPAT;
                default:
                    throw new RuntimeException("invalid single item type at position " + position);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    private int getItemOrderPosition(int position) {
        if (track.multi) {
            return position - 1;
        } else {
            return 0;
        }
    }

    private int getItemPatPosition(int position) {
        if (track.multi) {
            return position - 1 - track.orders.size() - 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPESTUDY:
                return getViewStudy(position, convertView, parent);
            case TYPEORDER:
                return getViewOrder(position, convertView, parent);
            case TYPEORDERADD:
                return getViewOrderAdd(convertView, parent);
            case TYPEPAT:
                return getViewPat(position, convertView, parent);
            case TYPEPATADD:
                return getViewPatAdd(convertView, parent);
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
                    frag.editTempoTap();
                }
            });
            holder.tv_study = (TextView) rowView.findViewById(R.id.tv_track_study_study);
            holder.tv_study.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    frag.editSpeedStudy();
                }
            });
            holder.tv_practice = (TextView) rowView.findViewById(R.id.tv_track_study_practice);
            holder.tv_practice.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    frag.editPractice();
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
        int index = getItemOrderPosition(position);
        Order order = track.orders.get(index);
        s = "order " + order.toString();
        if (index == selectedOrder) {
            s = ">> " + s;
        }
        holder.titel.setText(s);
        return rowView;
    }

    private View getViewOrderAdd(View convertView, ViewGroup parent) {
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
                    frag.editOrder(true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderOrderAdd) rowView.getTag();
        }

        String s = "add order";
        holder.titel.setText(s);
        return rowView;
    }

    private View getViewPat(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderPat holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_pat_row,
                    parent, false);
            holder = new ViewHolderPat();
            holder.rij = (LinearLayout) rowView.findViewById(R.id.ll_track_pat);
            holder.info= (TextView) rowView.findViewById(R.id.tv_track_pat_info);
            holder.edit = (ImageView) rowView.findViewById(R.id.iv_track_pat_edit);
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    frag.editPattern(position, false);
                }
            });

            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPat) rowView.getTag();
        }

        int index = getItemPatPosition(position);
        Pat pat = track.pats.get(index);
        holder.info.setText(pat.toString2(h));

//        holder.rij.setBackgroundColor((index == selectedPat) ? Color.BLUE : Color.TRANSPARENT);

        return rowView;
    }

    private View getViewPatAdd(View convertView, ViewGroup parent) {
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
                    frag.editPattern (0, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPatAdd) rowView.getTag();
        }

        holder.titel.setText("add pattern");
        return rowView;
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
        public TextView info;
        public ImageView edit;
    }

    public static class ViewHolderPatAdd {
        public LinearLayout rij;
        public TextView titel;
    }
}