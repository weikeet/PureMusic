package io.weicools.puremusic.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.PlayListAdapter;
import io.weicools.puremusic.model.SongListInfo;
import io.weicools.puremusic.model.enums.LoadStateEnum;
import io.weicools.puremusic.ui.activity.OnlineMusicActivity;
import io.weicools.puremusic.ui.base.BaseFragment;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.NetworkUtil;
import io.weicools.puremusic.util.ViewUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ListView lvPlayList;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;

    private List<SongListInfo> mSongListInfos;

    public PlayListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        lvPlayList = view.findViewById(R.id.lv_play_list);
        llLoading = view.findViewById(R.id.ll_loading);
        llLoadFail = view.findViewById(R.id.ll_load_fail);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!NetworkUtil.isNetworkAvailable(getContext())) {
            ViewUtil.changeViewState(lvPlayList, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }

        mSongListInfos = AppCache.getSongListInfos();
        if (mSongListInfos.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo songListInfo = new SongListInfo();
                songListInfo.setTitle(titles[i]);
                songListInfo.setType(types[i]);
                mSongListInfos.add(songListInfo);
            }
        }

        PlayListAdapter adapter = new PlayListAdapter(mSongListInfos);
        lvPlayList.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        lvPlayList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongListInfo songListInfo = mSongListInfos.get(position);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        intent.putExtra(ConstantUtil.MUSIC_LIST_TYPE, songListInfo);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = lvPlayList.getFirstVisiblePosition();
        int offset = (lvPlayList.getChildAt(0) == null) ? 0 : lvPlayList.getChildAt(0).getTop();

        outState.putInt(ConstantUtil.PLAYLIST_POSITION, position);
        outState.putInt(ConstantUtil.PLAYLIST_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        lvPlayList.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(ConstantUtil.PLAYLIST_POSITION);
                int offset = savedInstanceState.getInt(ConstantUtil.PLAYLIST_OFFSET);
                lvPlayList.setSelectionFromTop(position, offset);
            }
        });
    }
}
