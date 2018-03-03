package io.weicools.puremusic.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.FileUtil;

/**
 * local music list adapter
 *
 * Author: weicools
 * Time: 2017/11/23 上午11:48
 */

public class LocalMusicAdapter extends BaseAdapter {
    private OnMoreClickListener mListener;
    private int mPlayingPosition;

    @Override
    public int getCount() {
        return AppCache.getInstance().getMusicList().size();
    }

    @Override
    public Object getItem(int i) {
        return AppCache.getInstance().getMusicList().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == mPlayingPosition) {
            viewHolder.vPlaying.setVisibility(View.VISIBLE);
        } else {
            viewHolder.vPlaying.setVisibility(View.GONE);
        }

        Music music = AppCache.getInstance().getMusicList().get(position);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        String artist = FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum());

        viewHolder.ivCover.setImageBitmap(cover);
        viewHolder.tvTitle.setText(music.getTitle());
        viewHolder.tvArtist.setText(artist);
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onMoreClick(position);
                }
            }
        });
        viewHolder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private boolean isShowDivider(int position) {
        return position != AppCache.getInstance().getMusicList().size() - 1;
    }

    public void updatePlayingPosition(MusicService musicService) {
        if (musicService.getPlayingMusic() != null && musicService.getPlayingMusic().getType() == Music.Type.LOCAL) {
            mPlayingPosition = musicService.getPlayingPosition();
        } else {
            mPlayingPosition = -1;
        }
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
