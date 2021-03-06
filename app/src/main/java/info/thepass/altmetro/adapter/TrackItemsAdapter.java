package info.thepass.altmetro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import info.thepass.altmetro.R;
import info.thepass.altmetro.ui.TrackFragment;
import info.thepass.altmetro.data.MetronomeData;
import info.thepass.altmetro.data.Pat;
import info.thepass.altmetro.data.Repeat;
import info.thepass.altmetro.data.Track;
import info.thepass.altmetro.tools.EmphasisViewManager;
import info.thepass.altmetro.tools.HelperMetro;
import info.thepass.altmetro.tools.Keys;

public class TrackItemsAdapter extends ArrayAdapter<String> {
    public final static int ROWTYPEPAT = 0;
    public final static int ROWTYPEPATADD = 1;
    public final static int ROWTYPEREPEAT = 2;
    public final static int ROWTYPEREPEATADD = 3;
    private final static String TAG = "TrakItemsAdapter";
    public int selectedPat = 0;
    public int selectedRepeat = 0;
    private Context context;
    private HelperMetro h;
    private ListView lv;
    private Track track;
    private MetronomeData metronomeData;
    private TrackFragment frag;
    private int positionToolbar = -1;
    private int lvSelColor;

    public TrackItemsAdapter(Context cont, int layout, ListView lv2,
                             Track track2, MetronomeData metronomeData2,
                             HelperMetro hConstructor, TrackFragment frag2) {
        super(cont, layout, track2.items);
        h = hConstructor;
        context = cont;
        this.track = track2;
        this.metronomeData = metronomeData2;
        lv = lv2;
        frag = frag2;
        lvSelColor = h.getColor(R.color.color_listitem_selected_background);
    }

    //    @Override
    public int getCount() {
        return track.items.size();
    }

    @Override
    public int getViewTypeCount() {
        return (track.multi) ? 4 : 3;
    }

    @Override
    public int getItemViewType(int position) {
        int vType;
        if (track.multi) {
            if (position < (track.repeatList.size())) {
                vType = ROWTYPEREPEAT;
            } else if (position == (track.repeatList.size())) {
                vType = ROWTYPEREPEATADD;
            } else if (position < (track.repeatList.size() + 1 + track.patList.size())) {
                vType = ROWTYPEPAT;
            } else if (position == (track.repeatList.size() + 1 + track.patList.size())) {
                vType = ROWTYPEPATADD;
            } else {
                String msg = "invalid multi item type at position " + position;
                msg += " rep:" + track.repeatList.size();
                msg += " pat:" + track.patList.size();
                throw new RuntimeException(msg);
            }
            return vType;

        } else {
            switch (position) {
                case 0:
                    vType = ROWTYPEREPEAT;
                    break;
                default:
                    if (position < (1 + track.patList.size())) {
                        vType = ROWTYPEPAT;
                    } else if (position == (1 + track.patList.size())) {
                        vType = ROWTYPEPATADD;
                    } else {
                        throw new RuntimeException("invalid single item type at position " + position);
                    }
            }
            return vType;
        }
    }

    @Override
    public void notifyDataSetChanged() {
        track.syncItems();
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
                selectedRepeat = track.repeatList.size();
                break;
            case ROWTYPEPAT:
                selectedPat = track.getItemPatPosition(position);
                break;
            case ROWTYPEPATADD:
                selectedPat = track.patList.size();
                break;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
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

    private View getViewRepeat(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolderRepeat holder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.fragment_track_repeat_row,
                    parent, false);
            holder = new ViewHolderRepeat();
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_repeat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_repeat_info);
            initButtonsRepeat(rowView, holder);
            initLLRepeat(rowView, holder);

            holder.evRepeatList = new EmphasisViewManager("listrepeat", Keys.EVMLIST, rowView, h);
            holder.evRepeatList.useLow = true;

            rowView.setTag(holder);
        } else {
            holder = (ViewHolderRepeat) rowView.getTag();
        }

        int index = track.getItemRepeatPosition(position);

        Repeat repeat = track.repeatList.get(index);
        Pat pat = track.patList.get(repeat.patSelected);
        String patDisplay = (track.patList.size() > 1) ? pat.display(h, repeat.patSelected, false) : "";
        int indexDisplay = (track.patList.size() > 1) ? index : -1;
        String s = repeat.display(h, indexDisplay, patDisplay, index != selectedRepeat);
        holder.info.setText(s);
        holder.evRepeatList.setPattern(pat, false);

        if (index == 0 && track.multi) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setTextColor(Color.BLACK);
            if (track.multi) {
                holder.header.setText(h.getString(R.string.label_repeats));
            } else {
                holder.header.setText(h.getString(R.string.label_repeat));
            }
        } else {
            holder.header.setVisibility(View.GONE);
        }

        holder.rijEmphasis.setVisibility((track.patList.size() > 1) ? View.VISIBLE : View.INVISIBLE);
        holder.rijBody.setBackgroundColor((index == selectedRepeat && track.repeatList.size() > 1) ? lvSelColor : Color.TRANSPARENT);
        if (track.multi) {
            holder.rijToolbar.setVisibility((position == positionToolbar) ? View.VISIBLE : View.GONE);
        } else {
            holder.rijToolbar.setVisibility(View.GONE);
        }
        return rowView;
    }

    private void llClickRepeat(String info, View v) {
        int position = getViewPosition(v);
        if (track.repeatList.size() > 1) {
            if (position >= 0) {
                positionToolbar = (positionToolbar == position) ? -1 : position;
            } else {
                positionToolbar = position;
            }
            notifyDataSetChanged();

        } else {
            frag.lvManager.editRepeat(position, false);
        }
        int index = track.getItemRepeatPosition(position);
        frag.setRepeat(index);
    }

    private void initLLRepeat(View rowView, ViewHolderRepeat holder) {
        holder.rijEmphasis = (LinearLayout) rowView.findViewById(R.id.ll_listrepeat_emphasis);
        holder.rijBody = (LinearLayout) rowView.findViewById(R.id.ll_track_repeat_body);
        holder.rijToolbar = (LinearLayout) rowView.findViewById(R.id.ll_track_repeat_toolbar);

        holder.rijEmphasis.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                llClickRepeat("listemphasis", v);
            }
        });
        holder.rijBody.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                llClickRepeat("body", v);
            }
        });
    }

    private void initButtonsRepeat(View rowView, ViewHolderRepeat holder) {
        holder.play = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_play);
        holder.edit = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_edit);
        holder.delete = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_delete);
        holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_add);
        holder.up = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_up);
        holder.down = (ImageButton) rowView.findViewById(R.id.imb_track_repeat_down);

        holder.play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();

                int index = track.getItemRepeatPosition(position);
                frag.setRepeat(index);
                frag.doStartPlayer();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();

                int index = track.getItemRepeatPosition(position);
                frag.setRepeat(index);

                frag.lvManager.editRepeat(position, false);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();

                int index = track.getItemRepeatPosition(position);
                frag.setRepeat(index);

                frag.lvManager.confirmDeleteRepeat(position);
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();

                int index = track.getItemRepeatPosition(position);
                frag.setRepeat(index);

                frag.lvManager.editRepeat(position, true);
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
                    Repeat repeat0 = track.repeatList.get(index - 1);
                    Repeat repeat1 = track.repeatList.get(index);
                    track.repeatList.set(index - 1, repeat1);
                    track.repeatList.set(index, repeat0);
                }
                notifyDataSetChanged();

                index = track.getItemRepeatPosition(selectedRepeat);
                frag.setRepeat(index);
            }
        });

        holder.down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                int index = track.getItemRepeatPosition(position);
                if (index < track.repeatList.size() - 1) {
                    positionToolbar++;
                    selectedRepeat++;
                    Repeat repeat0 = track.repeatList.get(index);
                    Repeat repeat1 = track.repeatList.get(index + 1);
                    track.repeatList.set(index, repeat1);
                    track.repeatList.set(index + 1, repeat0);
                }
                notifyDataSetChanged();

                index = track.getItemRepeatPosition(selectedRepeat);
                frag.setRepeat(index);
            }
        });
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
                    frag.lvManager.editRepeat(position, true);
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
            holder.header = (TextView) rowView.findViewById(R.id.tv_track_pat_header);
            holder.info = (TextView) rowView.findViewById(R.id.tv_track_pat_info);

            initLLPat(rowView, holder);
            initButtonsPat(rowView, holder);
            holder.evPatList = new EmphasisViewManager("listpat", Keys.EVMLIST, rowView, h);
            holder.evPatList.useLow = true;

            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPat) rowView.getTag();
        }

        int index = track.getItemPatPosition(position);
        Pat pat = track.patList.get(index);
        int indexDisplay = (track.patList.size() > 1) ? index : -1;
        String s = (track.patList.size() > 0) ? pat.display(h, indexDisplay, false) : "";
        holder.info.setText(s);
        holder.evPatList.setPattern(pat, false);

        if (index == 0 && track.multi) {
            holder.header.setTextColor(Color.BLACK);
            holder.header.setVisibility(View.VISIBLE);
            if (track.multi) {
                holder.header.setText(h.getString(R.string.label_patterns));
            } else {
                holder.header.setText(h.getString(R.string.label_pattern));
            }
        } else {
            holder.header.setVisibility(View.GONE);
        }

        holder.rijToolbar.setVisibility((position == positionToolbar) ? View.VISIBLE : View.GONE);
        holder.rijBody.setBackgroundColor((position == positionToolbar) ? lvSelColor : Color.TRANSPARENT);
        holder.info.setBackgroundColor((position == positionToolbar) ? lvSelColor : Color.TRANSPARENT);

        return rowView;
    }

    private void llClickPat(String info, View v) {
        int position = getViewPosition(v);
        if (track.patList.size() > 1) {
            if (position >= 0) {
                positionToolbar = (positionToolbar == position) ? -1 : position;
            } else {
                positionToolbar = position;
            }
        } else {
            frag.lvManager.editPattern(position, false);
        }
        notifyDataSetChanged();
    }

    private void initLLPat(View rowView, ViewHolderPat holder) {
        holder.rijBody = (LinearLayout) rowView.findViewById(R.id.ll_track_pat_body);
        holder.rijEmphasis = (LinearLayout) rowView.findViewById(R.id.ll_listpat_emphasis);
        holder.rijToolbar = (LinearLayout) rowView.findViewById(R.id.ll_track_pat_toolbar);

        holder.rijBody.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                llClickPat("body", v);
            }
        });
        holder.rijEmphasis.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                llClickPat("emphasis", v);
            }
        });
        holder.info.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                llClickPat("info", v);
            }
        });

    }

    private void initButtonsPat(View rowView, ViewHolderPat holder) {
        holder.edit = (ImageButton) rowView.findViewById(R.id.imb_track_pat_edit);
        holder.delete = (ImageButton) rowView.findViewById(R.id.imb_track_pat_delete);
        holder.add = (ImageButton) rowView.findViewById(R.id.imb_track_pat_add);
        holder.up = (ImageButton) rowView.findViewById(R.id.imb_track_pat_up);
        holder.down = (ImageButton) rowView.findViewById(R.id.imb_track_pat_down);

        holder.edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.lvManager.editPattern(position, false);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.lvManager.confirmDeletePattern(position);
            }
        });
        holder.add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                positionToolbar = -1;
                notifyDataSetChanged();
                frag.lvManager.editPattern(position, true);
            }
        });
        holder.up.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                int index = track.getItemPatPosition(position);
                if (index >= 1) {
                    positionToolbar--;
                    Pat pat0 = track.patList.get(index - 1);
                    Pat pat1 = track.patList.get(index);
                    track.patList.set(index - 1, pat1);
                    track.patList.set(index, pat0);
                }
                notifyDataSetChanged();
            }
        });
        holder.down.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int position = getViewPosition(v);
                int index = track.getItemPatPosition(position);
                if (index < track.patList.size() - 1) {
                    positionToolbar++;
                    Pat pat0 = track.patList.get(index);
                    Pat pat1 = track.patList.get(index + 1);
                    track.patList.set(index, pat1);
                    track.patList.set(index + 1, pat0);
                }
                notifyDataSetChanged();
            }
        });

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
                    positionToolbar = -1;
                    notifyDataSetChanged();
                    frag.lvManager.editPattern(position, true);
                }
            });
            rowView.setTag(holder);
        } else {
            holder = (ViewHolderPatAdd) rowView.getTag();
        }

        return rowView;
    }

    public static class ViewHolderRepeat {
        public LinearLayout rijEmphasis;
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
        public LinearLayout rijEmphasis;
        public LinearLayout rijToolbar;
        public TextView header;
        public TextView info;
        public EmphasisViewManager evPatList;
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