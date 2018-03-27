package io.weicools.puremusic.executor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.weicools.puremusic.R;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.interfaze.OnPlayerEventListener;
import io.weicools.puremusic.module.PlayListActivity;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.util.CoverLoader;

/**
 * Create by weicools on 2018/3/4.
 */

public class ControlPanel implements View.OnClickListener, OnPlayerEventListener {
    private ImageView ivPlayBarCover;
    private TextView tvPlayBarTitle;
    private TextView tvPlayBarArtist;
    private ImageView ivPlayBarPlay;
    private ImageView ivPlayBarNext;
    private ImageView vPlayBarPlaylist;
    private ProgressBar mProgressBar;

    public ControlPanel(View view) {
        ivPlayBarCover = view.findViewById(R.id.iv_play_bar_cover);
        tvPlayBarTitle = view.findViewById(R.id.tv_play_bar_title);
        tvPlayBarArtist = view.findViewById(R.id.tv_play_bar_artist);
        ivPlayBarPlay = view.findViewById(R.id.iv_play_bar_play);
        ivPlayBarNext = view.findViewById(R.id.iv_play_bar_next);
        vPlayBarPlaylist = view.findViewById(R.id.v_play_bar_playlist);
        mProgressBar = view.findViewById(R.id.play_progressbar);
        
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
        vPlayBarPlaylist.setOnClickListener(this);
        onChange(AudioPlayer.getInstance().getPlayMusic());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_bar_play:
                AudioPlayer.getInstance().playPause();
                break;
            case R.id.iv_play_bar_next:
                AudioPlayer.getInstance().next();
                break;
            case R.id.v_play_bar_playlist:
                Context context = vPlayBarPlaylist.getContext();
                // TODO: 2018/3/4 start PlayList 
                Intent intent = new Intent(context, PlayListActivity.class);
                context.startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onChange(Music music) {
        if (music == null) {
            return;
        }
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        ivPlayBarPlay.setSelected(AudioPlayer.getInstance().isPlaying() || AudioPlayer.getInstance().isPreparing());
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress((int) AudioPlayer.getInstance().getAudioPosition());
    }

    @Override
    public void onPlayerStart() {
        ivPlayBarPlay.setSelected(true);
    }

    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
    }

    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onBufferingUpdate(int percent) {
    }
}
