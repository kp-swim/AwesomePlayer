package krzysiek.awesomeplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import krzysiek.awesomeplayer.SongFragment.OnListFragmentInteractionListener;

import java.util.List;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mValues;
    private final OnListFragmentInteractionListener mListener;

    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mBandView.setText(mValues.get(position).getBand());
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mDurationView.setText(Song.getFormatTimeFromSeconds(mValues.get(position).getDuration()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mBandView;
        public final TextView mTitleView;
        public final TextView mDurationView;
        public Song mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBandView = (TextView) view.findViewById(R.id.band);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDurationView = view.findViewById(R.id.time);
        }

    }
}
