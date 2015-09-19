package info.thepass.altmetro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.data.TrackData;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;
import info.thepass.altmetro.ui.TrackFragment;

public class TrackItemsAdapter extends ArrayAdapter<String> {
    public final static int ROWTYPESTUDY = 0;
    public final static int ROWTYPEPAT = 1;
    public final static int ROWTYPEPATADD = 2;
    public final static int ROWTYPEREPEAT = 3;
    public final static int ROWTYPEREPEATADD = 4;
    private final static String TAG = "TrakItemsAdapter";
    public int selectedPat = 0;
    public int selectedRepeat = 0;
    private Context context;
    private HelperMetro h;
    private ListView lv;
    private Track track;
    private TrackData trackData;
    private TrackFragment frag;
    private int positionToolbar = -1;
    private int lvSelColor;
    private int resOverflow;
    private int resEdit;

    public TrackItemsAdapter(Context cont, int layout, ListView lv2,
                             Track track2, TrackData trackData2,
                             HelperMetro hConstructor, TrackFragment frag2) {
        super(cont, layout, track2.items);
        h = hConstructor;
        h.logD(TAG, "constructor");
        context = cont;
        this.track = track2;
        this.trackData = trackData2;
        lv = lv2;
        frag = frag2;
        lvSelColor = h.getColor(R.color.color_listitem_selected_background);
        resOverflow = h.context.getResources().getIdentifier("ic_action_overflow", "mipmap", h.context.getPackageName());
        resEdit = h.context.getResources().getIdentifier("ic_action_edit", "mipmap", h.context.getPackageName());
    }

    //    @Override
    public int getCount() {
        return track.items.size();
    }

    @Override
    public int getViewTypeCount() {
        return (track.multi) ? 5 : 4;
    }

    @Override
    public int getItemViewType(int position) {
        if (track.multi) {
            if (position == 0) {
                return ROWTYPESTUDY;
            } else if (position < (1 + track.repeats.size())) {
                return ROWTYPEREPEAT;
            } else if (position == (1 + track.repeats.size())) {
                return ROWTYPEREPEATADD;
            } else if (position < (1 + track.repeats.size() + 1 + trackData.pats.size())) {
                return ROWTYPEPAT;
            } else if (position == (1 + track.repeats.size() + 1 + trackData.pats.size())) {
                return ROWTYPEPATADD;
            } else {
                String msg = "invalid multi item type at position " + position;
                msg += " rep:" + track.repeats.size();
                msg += " pat:" + trackData.pats.size();
                throw new RuntimeException(msg);
            }
        } else {
            switch (position) {
                case 0:
                    return ROWTYPESTUDY;
                case 1:
                    return ROWTYPEREPEAT;
                default:
                    if (position < (2 + trackData.pats.size())) {
                        return ROWTYPEPAT;
                    } else if (position == (2 + trackData.pats.size())) {
                        return ROWTYPEPATADD;
                    } else {
                        throw new RuntimeException("invalid single item type at position " + position);
                    }
            }
        }
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private int getViewPosition(View v) {
        int position = lv.getPositionForView(v);
        int rowType = this.getItemViewType(position);
        switch (rowType) {
            case ROWTYPEREPEAT:
                selectedRepeat = track.getItemRepeatPosition(position);
                break;
            case ROWTYPEREPEATADD:
                selectedRepeat = track.repeats.size();
                break;
            case ROWTYPEPAT:
                selectedPat = track.getItemPatPosition(position);
                break;
            case ROWTYPEPATADD:
                selectedPat = trackData.pats.size();
                break;
        }
        return position;
    }

    public int getItemRepeatIndex(int position) {
        int index = track.getItemRepeatPosition(position);
        return (index + 1);
    }

    public int getItemPatIndex(int position) {
        int index = track.getItemPatPosition(position);
        return (index + 1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case ROWTYPESTUDY:
                return getViewStudy(position, convertView, parent);
            case ROWTYPEREPEAT:
                return getViewRepeat(position, convertView, parent);
            case ROWTYPEREPEATADD:
                return getViewRepeatAdd(convertView, parent);
            case ROWTYPEPAT:
                return getViewPat(position, convertView, parent);
            case ROWTYPEPATADD:
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

            holder.rg_practice = (RadioGroup) rowView.findViewById(R.id.rg_track_practice);
            holder.rg_practice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Log.d(TAG, "practice changed");
                    int newPractice = 0;
                    switch (checkedId) {
                        case R.id.rb_track_prac50:
                            newPractice = 50;
                            break;
                        case R.id.rb_track_prac70:
                            newPractice = 70;
                            break;
                        case R.id.rb_track_prac80:
                            newPractice = 80;
                            break;
                        case R.id.rb_track_prac90:
                            newPractice = 90;
                            break;
                        case R.id.rb_track_prac95:
                            newPractice = 95;
                            break;
                        case R.id.rb_track_prac100:
                            newPractice = 100;
                            break;
                    }
                    track.study.practice = newPractice;
                    trackData.saveData("Practice changed", false);
                }
            });

            switch (track.study.practice) {
                case 50:
                    holder.rb_prac50 = (RadioButton) rowView.findViewById(R.id.rb_track_prac50);
                    holder.rb_prac50.setChecked(true);
                    break;
                case 70:
                    holder.rb_prac70 = (RadioButton) rowView.findViewById(R.id.rb_track_prac70);
                    holder.rb_prac70.setChecked(true);
                    break;
                case 80:
                    holder.rb_prac80 = (RadioButton) rowView.findViewById(R.id.rb_track_prac80);
                    holder.rb_prac80.setChecked(true);
                    break;
                case 90:
                    holder.rb_prac90 = (RadioButton) rowView.findViewById(R.id.rb_track_prac90);
                    holder.rb_prac90.setChecked(true);
                    break;
                case 95:
                    holder.rb_prac95 = (RadioButton) rowView.findViewById(R.id.rb_track_prac95);
                    holder.rb_prac95.setChecked(true);
                    break;
                case 100:
                    holder.rb_prac100 = (RadioButton) rowView.findViewById(R.id.rb_track_prac100);
                    holder.rb_prac100.setChecked(true);
                    break;
            }
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderStudy) rowView.getTag();
        }

        boolean showStudy = h.prefs.getBoolean(Keys.PREFSHOWSTUDY, true);
        holder.tv_study.setVisibility((showStudy)? View.VISIBLE : View.GONE);
        holder.tv_study.setText((track.study.used)
                ? h.getString(R.string.label_study_on)
                : h.getString(R.string.label_study_off));

        boolean showPractice = h.prefs.getBoolean(Keys.PREFSHOWPRACTICE, true);
        if (!showPractice) {
            holder.rg_practice.setVisibility(View.GONE);
        } else {
            holder.rg_practice.setVisibility((track.study.used || (!showPractice)) ? View.INVISIBLE : View.VISIBLE);
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
            holder.rijBody = (LinearLayout) rowView.findViewById(R.id.ll_track_repeat_body);
            holder.rijToolbar = (LinearLayout) rowView.findViewById(R.id.ll_track_repeat_toolbar);
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_repeat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_repeat_info);
            holder.overflow = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_overflow);
            holder.play = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_play);
            holder.edit = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_edit);
            holder.delete = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_delete);
            holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_add);
            holder.up = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_up);
            holder.down = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_down);

            if (!track.multi) {
            }

            holder.overflow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (track.multi) {
                        int position = getViewPosition(v);
                        if (position > 0) {
                            positionToolbar = (positionToolbar == position) ? -1 : position;
                        } else {
                            positionToolbar = position;
                        }
                        notifyDataSetChanged();
                    } else {
                        frag.editRepeat(position, false);
                    }
                }
            });
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.editRepeat(position, false);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.confirmDeleteRepeat(position);
                }
            });
            holder.add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.editRepeat(position, true);
                }
            });
            holder.up.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    int index = track.getItemRepeatPosition(position);
                    if (index >= 1) {
                        positionToolbar--;
                        selectedRepeat--;
                        Repeat repeat0 = track.repeats.get(index - 1);
                        Repeat repeat1 = track.repeats.get(index);
                        track.repeats.set(index - 1, repeat1);
                        track.repeats.set(index, repeat0);
                    }
                    notifyDataSetChanged();
                }
            });
            holder.down.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    int index = track.getItemRepeatPosition(position);
                    if (index < track.repeats.size() - 1) {
                        positionToolbar++;
                        selectedRepeat++;
                        Repeat repeat0 = track.repeats.get(index);
                        Repeat repeat1 = track.repeats.get(index + 1);
                        track.repeats.set(index, repeat1);
                        track.repeats.set(index + 1, repeat0);
                    }
                    notifyDataSetChanged();
                }
            });

            holder.evRepeatList = new EmphasisViewManager("listrepeat", Keys.EVMLIST, rowView, h);
            holder.evRepeatList.useLow = true;

            rowView.setTag(holder);
        } else {
            holder = (ViewHolderRepeat) rowView.getTag();
        }

        int index = track.getItemRepeatPosition(position);

        Repeat repeat = track.repeats.get(index);
        Pat pat = trackData.pats.get(repeat.indexPattern);
        String patDisplay = pat.display(h, repeat.indexPattern, false);
        String s = repeat.display(h, index, patDisplay);
        holder.info.setText(s);
        holder.evRepeatList.setPattern(pat);

        holder.header.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
        holder.header.setTextColor(Color.BLACK);
        holder.rijBody.setBackgroundColor((index == selectedRepeat) ? lvSelColor : Color.TRANSPARENT);
        if (track.multi) {
            holder.overflow.setImageResource(resOverflow);
            holder.rijToolbar.setVisibility((position == positionToolbar) ? View.VISIBLE : View.GONE);
        } else {
            holder.overflow.setImageResource(resEdit);
            holder.rijToolbar.setVisibility(View.GONE);
        }
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
            holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_repeatadd_add);
            holder.add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    frag.editRepeat(position, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderRepeatAdd) rowView.getTag();
        }

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
            holder.rijBody = (LinearLayout) rowView.findViewById(R.id.ll_track_pat_body);
            holder.rijToolbar = (LinearLayout) rowView.findViewById(R.id.ll_track_pat_toolbar);
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_pat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_pat_info);

            holder.overflow = (ImageButton) rowView.findViewById(R.id.imb_track_pat_overflow);
            holder.play = (ImageButton) rowView.findViewById(R.id.imb_track_pat_play);
            holder.edit = (ImageButton) rowView.findViewById(R.id.imb_track_pat_edit);
            holder.delete = (ImageButton) rowView.findViewById(R.id.imb_track_pat_delete);
            holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_pat_add);
            holder.up = (ImageButton) rowView.findViewById(R.id.imb_track_pat_up);
            holder.down = (ImageButton) rowView.findViewById(R.id.imb_track_pat_down);

            holder.overflow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    if (position > 0) {
                        positionToolbar = (positionToolbar == position) ? -1 : position;
                    } else {
                        positionToolbar = position;
                    }
                    notifyDataSetChanged();
                }
            });
            holder.edit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.editPattern(position, false);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.confirmDeletePattern(position);
                }
            });
            holder.add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.editPattern(position, true);
                }
            });
            holder.up.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    int index = track.getItemPatPosition(position);
                    if (index >= 1) {
                        positionToolbar--;
                        Pat pat0 = trackData.pats.get(index - 1);
                        Pat pat1 = trackData.pats.get(index);
                        trackData.pats.set(index - 1, pat1);
                        trackData.pats.set(index, pat0);
                    }
                    notifyDataSetChanged();
                }
            });
            holder.down.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    int index = track.getItemPatPosition(position);
                    if (index < trackData.pats.size() - 1) {
                        positionToolbar++;
                        Pat pat0 = trackData.pats.get(index);
                        Pat pat1 = trackData.pats.get(index + 1);
                        trackData.pats.set(index, pat1);
                        trackData.pats.set(index + 1, pat0);
                    }
                    notifyDataSetChanged();
                }
            });

            holder.evPatList = new EmphasisViewManager("listpat", Keys.EVMLIST, rowView, h);
            holder.evPatList.useLow = true;

            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPat) rowView.getTag();
        }

        int index = track.getItemPatPosition(position);
        Pat pat = trackData.pats.get(index);
        String s = pat.display(h, index, false);
        holder.info.setText(s);
        holder.evPatList.setPattern(pat);

        holder.header.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
        holder.rijToolbar.setVisibility((position == positionToolbar) ? View.VISIBLE : View.GONE);
        holder.header.setTextColor(Color.BLACK);

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
            holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_patadd_add);
            holder.add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getViewPosition(v);
                    frag.editPattern(position, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPatAdd) rowView.getTag();
        }

        return rowView;
    }

    public static class ViewHolderStudy {
        public LinearLayout rij;
        public TextView tv_tap;
        public TextView tv_study;
        public RadioGroup rg_practice;
        public RadioButton rb_prac50;
        public RadioButton rb_prac70;
        public RadioButton rb_prac80;
        public RadioButton rb_prac90;
        public RadioButton rb_prac95;
        public RadioButton rb_prac100;
    }

    public static class ViewHolderRepeat {
        public LinearLayout rijBody;
        public LinearLayout rijToolbar;
        public TextView header;
        public TextView info;
        public EmphasisViewManager evRepeatList;
        public ImageButton overflow;
        public ImageButton play;
        public ImageButton edit;
        public ImageButton delete;
        public ImageButton add;
        public ImageButton up;
        public ImageButton down;
    }

    public static class ViewHolderRepeatAdd {
        public LinearLayout rij;
        public ImageButton add;
    }

    public static class ViewHolderPat {
        public LinearLayout rijBody;
        public LinearLayout rijToolbar;
        public TextView header;
        public TextView info;
        public EmphasisViewManager evPatList;
        public ImageButton overflow;
        public ImageButton play;
        public ImageButton edit;
        public ImageButton delete;
        public ImageButton add;
        public ImageButton up;
        public ImageButton down;
    }

    public static class ViewHolderPatAdd {
        public LinearLayout rij;
        public ImageButton add;
    }
}