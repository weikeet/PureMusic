package io.weicools.puremusic.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.SongSheetAdapter;
import io.weicools.puremusic.app.AppCache;
import io.weicools.puremusic.model.SongSheetInfo;
import io.weicools.puremusic.ui.activity.OnlineMusicActivity;
import io.weicools.puremusic.util.ConstantUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongSheetFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView mLvPlaylist;

    private List<SongSheetInfo> mSongList;

    public SongSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_sheet, container, false);
        mLvPlaylist = view.findViewById(R.id.lv_sheet);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSongList = AppCache.getInstance().getSongSheetList();
        if (mSongList.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongSheetInfo info = new SongSheetInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongList.add(info);
            }
        }
        SongSheetAdapter adapter = new SongSheetAdapter(mSongList);
        mLvPlaylist.setAdapter(adapter);
        mLvPlaylist.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongSheetInfo songSheetInfo = mSongList.get(position);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        intent.putExtra(ConstantUtil.MUSIC_LIST_TYPE, songSheetInfo);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = mLvPlaylist.getFirstVisiblePosition();
        int offset = (mLvPlaylist.getChildAt(0) == null) ? 0 : mLvPlaylist.getChildAt(0).getTop();

        outState.putInt(ConstantUtil.PLAYLIST_POSITION, position);
        outState.putInt(ConstantUtil.PLAYLIST_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        // FIXME: 2018/3/4 null
        if (mLvPlaylist != null) {
            mLvPlaylist.post(new Runnable() {
                @Override
                public void run() {
                    int position = savedInstanceState.getInt(ConstantUtil.PLAYLIST_POSITION);
                    int offset = savedInstanceState.getInt(ConstantUtil.PLAYLIST_OFFSET);
                    mLvPlaylist.setSelectionFromTop(position, offset);
                }
            });
        }
    }
}
