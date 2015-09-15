package info.thepass.altmetro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;
import info.thepass.altmetro.ui.TrackFragment;

public class TrackItemsAdapter extends ArrayAdapter<String> {
    public final static int TYPESTUDY = 0;
    public final static int TYPEREPEAT = 1;
    public final static int TYPEPAT = 2;
    public final static int TYPEREPEATADD = 3;
    public final static int TYPEPATADD = 4;
    private final static String TAG = "TrakItemsAdapter";
    public int selectedPat = 0;
    public int selectedRepeat = 0;
    private Context context;
    private HelperMetro h;
    private Track track;
    private TrackFragment frag;
    private int lvSelColor;

    public TrackItemsAdapter(Context cont, int layout,
                             Track track, HelperMetro hConstructor, TrackFragment frag2) {
        super(cont, layout, track.items);
        h = hConstructor;
        h.logD(TAG, "constructor");
        context = cont;
        this.track = track;
        frag = frag2;
        lvSelColor = h.getColor(R.color.color_listitem_selected_background);
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
            } else if (position < (1 + track.repeats.size())) {
                return TYPEREPEAT;
            } else if (position == (1 + track.repeats.size())) {
                return TYPEREPEATADD;
            } else if (position < (1 + track.repeats.size() + 1 + track.pats.size())) {
                return TYPEPAT;
            } else if (position == (1 + track.repeats.size() + 1 + track.pats.size())) {
                return TYPEPATADD;
            } else {
                throw new RuntimeException("invalid multi item type at position " + position);
            }
        } else {
            switch (position) {
                case 0:
                    return TYPESTUDY;
                case 1:
                    return TYPEREPEAT;
                case 2:
                    return TYPEPAT;
                default:
                    throw new RuntimeException("invalid single item type at position " + position);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
//        Log.d(TAG, "notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    public int getItemRepeatNumber(int position) {
        int index = track.getItemRepeatPosition(position);
        return (index + 1);
    }

    public int getItemPatNumber(int position) {
        int index = track.getItemPatPosition(position);
        return (index + 1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPESTUDY:
                return getViewStudy(position, convertView, parent);
            case TYPEREPEAT:
                return getViewRepeat(position, convertView, parent);
            case TYPEREPEATADD:
                return getViewRepeatAdd(convertView, parent);
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
                    frag.editTap();
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

    private View getViewRepeat(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderRepeat holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_repeat_row,
                    parent, false);
            holder = new ViewHolderRepeat();
            holder.rijAlles = (LinearLayout) rowView.findViewById(R.id.ll_track_repeat_alles);
            holder.rijEmphasis = (LinearLayout) rowView.findViewById(R.id.ll_list_repeat_emphasis);
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_repeat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_repeat_info);
            holder.edit = (ImageView) rowView.findViewById(R.id.iv_track_repeat_edit);
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Repeat edit onclicklistener " + position);
                    frag.editRepeatView(v);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderRepeat) rowView.getTag();
        }

        int index = track.getItemRepeatPosition(position);

        Repeat repeat = track.repeats.get(index);
        String s = "r" + getItemRepeatNumber(position) + ": " + repeat.toString2((index != selectedRepeat), h);
        holder.info.setText(s);

        holder.header.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
        holder.header.setTextColor(Color.BLACK);
        holder.rijEmphasis.setVisibility((track.multi) ? View.VISIBLE : View.GONE);
//        holder.info.setTextColor((index == selectedRepeat) ? Color.YELLOW : Color.WHITE);
        holder.rijAlles.setBackgroundColor((index == selectedRepeat) ? lvSelColor : Color.TRANSPARENT);
        return rowView;
    }

    private View getViewRepeatAdd(View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderRepeatAdd holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_repeatadd_row,
                    parent, false);
            holder = new ViewHolderRepeatAdd();
            holder.rij = (LinearLayout) rowView.findViewById(R.id.ll_track_repeatadd);
            holder.titel = (TextView) rowView.findViewById(R.id.tv_track_repeatadd_titel);
            holder.titel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    frag.editRepeat(0, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderRepeatAdd) rowView.getTag();
        }

        String s = h.getString(R.string.label_addrepeat);
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
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_pat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_pat_info);

            holder.edit = (ImageView) rowView.findViewById(R.id.iv_track_pat_edit);
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    frag.editPatternView(v);
                }
            });

            holder.evPatList = new EmphasisViewManager("listpat", Keys.EVMLIST, rowView, h);
            holder.evPatList.useLow = true;
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPat) rowView.getTag();
        }

        int index = track.getItemPatPosition(position);
        Pat pat = track.pats.get(index);
        String s = "p" + getItemPatNumber(position) + ": " + pat.toString2(h);
        holder.info.setText(s);
        holder.evPatList.setPattern(pat);

        holder.header.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
        holder.header.setTextColor(Color.BLACK);
//        holder.info.setTextColor((index == selectedPat) ? Color.YELLOW : Color.WHITE);
        holder.rij.setBackgroundColor((index == selectedPat) ? lvSelColor : Color.TRANSPARENT);

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
                    frag.editPattern(0, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPatAdd) rowView.getTag();
        }

        String s = h.getString(R.string.label_addpattern);
        holder.titel.setText(s);
        return rowView;
    }

    public static class ViewHolderStudy {
        public LinearLayout rij;
        public TextView tv_tap;
        public TextView tv_study;
        public TextView tv_practice;
    }

    public static class ViewHolderRepeat {
        public LinearLayout rijAlles;
        public LinearLayout rijEmphasis;
        public TextView header;
        public TextView info;
        public ImageView edit;
    }

    public static class ViewHolderRepeatAdd {
        public LinearLayout rij;
        public TextView titel;
    }

    public static class ViewHolderPat {
        public LinearLayout rij;
        public TextView header;
        public TextView info;
        public ImageView edit;
        public EmphasisViewManager evPatList;
    }

    public static class ViewHolderPatAdd {
        public LinearLayout rij;
        public TextView titel;
    }
}