package io.weicools.puremusic.module;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.weicools.puremusic.R;
import io.weicools.puremusic.interfaze.OnMoreClickListener;
import io.weicools.puremusic.module.local.PlayListAdapter;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.interfaze.OnPlayerEventListener;
import io.weicools.puremusic.module.base.BaseActivity;

public class PlayListActivity extends BaseActivity implements OnMoreClickListener, OnPlayerEventListener {
    private ListView mLvPlayList;

    private PlayListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        mLvPlayList = findViewById(R.id.lv_playlist);
    }

    @Override
    protected void onServiceBound() {
        mAdapter = new PlayListAdapter(AudioPlayer.getInstance().getMusicList());
        mAdapter.setIsPlaylist(true);
        mAdapter.setOnMoreClickListener(this);

        mLvPlayList.setAdapter(mAdapter);
        mLvPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                AudioPlayer.getInstance().play(position);
            }
        });

        AudioPlayer.getInstance().addOnPlayEventListener(this);
    }

    @Override
    public void onMoreClick(int position) {
        String[] items = new String[]{"移除"};
        Music music = AudioPlayer.getInstance().getMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(music.getTitle());
        dialog.setItems(items, (dialog1, which) -> {
            AudioPlayer.getInstance().delete(position);
            mAdapter.notifyDataSetChanged();
        });
        dialog.show();
    }

    @Override
    public void onChange(Music music) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerStart() {

    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPublish(int progress) {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    protected void onDestroy() {
        AudioPlayer.getInstance().removeOnPlayEventListener(this);
        super.onDestroy();
    }
}
