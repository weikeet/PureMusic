package io.weicools.puremusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.model.OnlineMusic;
import io.weicools.puremusic.model.OnlineMusicList;
import io.weicools.puremusic.model.SongListInfo;

/**
 * Author: weicools
 * Time: 2017/11/27 上午9:48
 */

public class PlayListAdapter extends BaseAdapter {
    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_MUSIC_LIST = 1;

    private Context mContext;
    private List<SongListInfo> mData;

    public PlayListAdapter(List<SongListInfo> data) {
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getType().equals("#")) {
            return TYPE_PROFILE;
        } else {
            return TYPE_MUSIC_LIST;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_MUSIC_LIST;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        mContext = parent.getContext();
        ViewHolderProfile holderProfile;
        ViewHolderMusicList holderMusicList;

        SongListInfo songListInfo = mData.get(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case TYPE_PROFILE:
                if (view == null) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.item_play_list_profile, parent, false);
                    holderProfile = new ViewHolderProfile(view);
                    view.setTag(holderProfile);
                } else {
                    holderProfile = (ViewHolderProfile) view.getTag();
                }
                holderProfile.tvProfile.setText(songListInfo.getTitle());
                break;
            case TYPE_MUSIC_LIST:
                if (view == null) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.item_play_music_list, parent, false);
                    holderMusicList = new ViewHolderMusicList(view);
                    view.setTag(holderMusicList);
                } else {
                    holderMusicList = (ViewHolderMusicList) view.getTag();
                }
                getMusicListInfo(songListInfo, holderMusicList);
                holderMusicList.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
                break;
            default:
                break;
        }
        return null;
    }

    private boolean isShowDivider(int position) {
        return position != mData.size() - 1;
    }

    private void getMusicListInfo(final SongListInfo songListInfo, final ViewHolderMusicList holderMusicList) {
        if (songListInfo.getCoverUrl() == null) {
            holderMusicList.tvMusic1.setTag(songListInfo.getTitle());
            holderMusicList.ivCover.setImageResource(R.drawable.default_cover);
            holderMusicList.tvMusic1.setText("1.加载中…");
            holderMusicList.tvMusic2.setText("2.加载中…");
            holderMusicList.tvMusic3.setText("3.加载中…");

            HttpClient.getSongListInfo(songListInfo.getType(), 3, 0, new HttpCallback<OnlineMusicList>() {
                @Override
                public void onSuccess(OnlineMusicList onlineMusicList) {
                    if (onlineMusicList == null || onlineMusicList.getSong_list() == null) {
                        return;
                    }
                    if (!songListInfo.getTitle().equals(holderMusicList.tvMusic1.getTag())) {
                        return;
                    }
                    parse(onlineMusicList, songListInfo);
                    setData(songListInfo, holderMusicList);
                }

                @Override
                public void onFail(Exception e) {

                }
            });
        } else {
            holderMusicList.tvMusic1.setTag(null);
            setData(songListInfo, holderMusicList);
        }
    }

    private void parse(OnlineMusicList onlineMusicList, SongListInfo songListInfo) {
        List<OnlineMusic> onlineMusics = onlineMusicList.getSong_list();
        songListInfo.setCoverUrl(onlineMusicList.getBillboard().getPic_s260());
        if (onlineMusics.size() >= 1) {
            songListInfo.setMusic1(mContext.getString(R.string.song_list_item_title_1,
                    onlineMusics.get(0).getTitle(), onlineMusics.get(0).getArtist_name()));
        } else {
            songListInfo.setMusic1("");
        }
        if (onlineMusics.size() >= 2) {
            songListInfo.setMusic2(mContext.getString(R.string.song_list_item_title_2,
                    onlineMusics.get(1).getTitle(), onlineMusics.get(1).getArtist_name()));
        } else {
            songListInfo.setMusic2("");
        }
        if (onlineMusics.size() >= 3) {
            songListInfo.setMusic3(mContext.getString(R.string.song_list_item_title_3,
                    onlineMusics.get(2).getTitle(), onlineMusics.get(2).getArtist_name()));
        } else {
            songListInfo.setMusic3("");
        }
    }

    private void setData(SongListInfo songListInfo, ViewHolderMusicList holderMusicList) {
        holderMusicList.tvMusic1.setText(songListInfo.getMusic1());
        holderMusicList.tvMusic2.setText(songListInfo.getMusic2());
        holderMusicList.tvMusic3.setText(songListInfo.getMusic3());

        // TODO: 2017/11/27 error and preload 
        Glide.with(mContext)
                .load(songListInfo.getCoverUrl())
                .into(holderMusicList.ivCover);
    }

    private static class ViewHolderProfile {
        private TextView tvProfile;

        ViewHolderProfile(View view) {
            super();

            tvProfile = view.findViewById(R.id.tv_profile);
        }
    }

    private static class ViewHolderMusicList {
        private ImageView ivCover;
        private TextView tvMusic1;
        private TextView tvMusic2;
        private TextView tvMusic3;
        private View vDivider;

        ViewHolderMusicList(View view) {
            super();

            ivCover = view.findViewById(R.id.iv_cover);
            tvMusic1 = view.findViewById(R.id.tv_music_1);
            tvMusic2 = view.findViewById(R.id.tv_music_2);
            tvMusic3 = view.findViewById(R.id.tv_music_3);
            vDivider = view.findViewById(R.id.v_divider);
        }
    }
}
