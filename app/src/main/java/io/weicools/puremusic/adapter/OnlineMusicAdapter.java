package io.weicools.puremusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.model.OnlineMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/11/27 上午11:56
 */

public class OnlineMusicAdapter extends BaseAdapter {
    private List<OnlineMusic> mData;
    private OnMoreClickListener mListener;

    public OnlineMusicAdapter(List<OnlineMusic> data) {
        this.mData = data;
    }
    
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        
        OnlineMusic onlineMusic = mData.get(position);
        // TODO: 2017/11/27 set error and pre 
        Glide.with(parent.getContext())
                .load(onlineMusic.getPic_small())
                .into(viewHolder.ivCover);
        viewHolder.tvTitle.setText(onlineMusic.getTitle());
        String artist = FileUtil.getArtistAndAlbum(onlineMusic.getArtist_name(), onlineMusic.getAlbum_title());
        viewHolder.tvArtist.setText(artist);
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoreClick(position);
            }
        });
        viewHolder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return view;
    }

    private boolean isShowDivider(int position) {
        return position != mData.size() - 1;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mListener = listener;
    }
    
    private static class ViewHolder {
        private View vPlaying;
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvArtist;
        private ImageView ivMore;
        private View vDivider;

        ViewHolder(View view) {
            super();

            vPlaying = view.findViewById(R.id.view_playing);
            ivCover = view.findViewById(R.id.iv_cover);
            tvTitle = view.findViewById(R.id.tv_title);
            tvArtist = view.findViewById(R.id.tv_artist);
            ivMore = view.findViewById(R.id.iv_more);
            vDivider = view.findViewById(R.id.view_divider);
        }
    }
}
