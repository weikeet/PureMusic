package io.weicools.puremusic.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/11/27 上午9:48
 *
 * 本地音乐列表适配器
 */

public class PlayListAdapter extends BaseAdapter {
    private List<Music> musicList;
    private OnMoreClickListener listener;
    private boolean isPlaylist;

    public PlayListAdapter(List<Music> musicList) {
        this.musicList = musicList;
    }

    public void setIsPlaylist(boolean isPlaylist) {
        this.isPlaylist = isPlaylist;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.vPlaying.setVisibility((isPlaylist && position == AudioPlayer.getInstance().getPlayPosition()) ? View.VISIBLE : View.INVISIBLE);
        Music music = musicList.get(position);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(music.getTitle());
        String artist = FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum());
        holder.tvArtist.setText(artist);
        holder.ivMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreClick(position);
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private boolean isShowDivider(int position) {
        return position != musicList.size() - 1;
    }

    private static class ViewHolder {
        private View vPlaying;
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvArtist;
        private ImageView ivMore;
        private View vDivider;

        ViewHolder(View view) {
            vPlaying = view.findViewById(R.id.view_playing);
            ivCover = view.findViewById(R.id.iv_cover);
            tvTitle = view.findViewById(R.id.tv_title);
            tvArtist = view.findViewById(R.id.tv_artist);
            ivMore = view.findViewById(R.id.iv_more);
            vDivider = view.findViewById(R.id.view_divider);
        }
    }
}
