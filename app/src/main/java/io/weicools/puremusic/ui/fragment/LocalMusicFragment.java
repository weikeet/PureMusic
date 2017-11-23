package io.weicools.puremusic.ui.fragment;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.LocalMusicAdapter;
import io.weicools.puremusic.adapter.OnMoreClickListener;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.ui.base.BaseFragment;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends BaseFragment implements OnMoreClickListener, AdapterView.OnItemClickListener {

    private ListView mLvLocalMusic;
    private TextView mTvEmpty;

    private LocalMusicAdapter mAdapter;

    public LocalMusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        mLvLocalMusic = view.findViewById(R.id.lv_local_music);
        mTvEmpty = view.findViewById(R.id.tv_empty);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new LocalMusicAdapter();
        mAdapter.setOnMoreClickListener(this);
        mLvLocalMusic.setAdapter(mAdapter);
        if (getMusicService().getPlayingMusic() != null && getMusicService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            mLvLocalMusic.setSelection(getMusicService().getPlayingPosition());
        }

        updateView();
    }

    @Override
    protected void setListener() {
        mLvLocalMusic.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        getMusicService().play(position);
    }

    public void onItemPlay() {
        updateView();
        if (getMusicService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            mLvLocalMusic.smoothScrollToPosition(getMusicService().getPlayingPosition());
        }
    }

    public void onMusicListUpdate() {
        updateView();
    }

    private void updateView() {
        if (AppCache.getMusicList().isEmpty()) {
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mTvEmpty.setVisibility(View.GONE);
        }

        mAdapter.updatePlayingPosition(getMusicService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMoreClick(int position) {
        final Music music = AppCache.getMusicList().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(music.getTitle());
        builder.setItems(R.array.local_music_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:// 分享
                        shareMusic(music);
                        break;
                    case 1:// 设为铃声
                        requestSetRingtone(music);
                        break;
                    case 2:// 查看歌曲信息
                        //MusicInfoActivity.start(getContext(), music);
                        break;
                    case 3:// 删除
                        deleteMusic(music);
                        break;
                }
            }
        });
        builder.show();
    }

    private void shareMusic(Music music) {
        File file = new File(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void requestSetRingtone(Music music) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getContext())) {
            ToastUtil.showShort(getContext(), getContext().getString(R.string.no_permission_setting));
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, ConstantUtil.REQUEST_WRITE_SETTINGS);
        } else {
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getPath());
            Cursor cursor = getContext().getContentResolver().query(uri, null,
                    MediaStore.MediaColumns.DATA + "?", new String[]{music.getPath()}, null);
            if (cursor == null) {
                return;
            }

            if (cursor.moveToFirst() && cursor.getCount() > 0) {
                String _id = cursor.getString(0);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.IS_MUSIC, true);
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                values.put(MediaStore.Audio.Media.IS_PODCAST, false);

                getContext().getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?",
                        new String[]{music.getPath()});
                Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
                RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
                ToastUtil.showShort(getContext(), getContext().getString(R.string.setting_ringtone_success));
            }

            cursor.close();
        }
    }

    private void deleteMusic(final Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String msg = getString(R.string.delete_music, music.getTitle());
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(music.getPath());
                if (file.delete()) {
                    boolean playing = (music == getMusicService().getPlayingMusic());
                    AppCache.getMusicList().remove(music);
                    if (playing) {
                        getMusicService().stop();
                        getMusicService().playPause();
                    } else {
                        getMusicService().updatePlayingPosition();
                    }
                    updateView();

                    // 刷新媒体库
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://".concat(music.getPath())));
                    getContext().sendBroadcast(intent);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantUtil.REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getContext())) {
                ToastUtil.showShort(getContext(), getContext().getString(R.string.grant_permission_setting));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = mLvLocalMusic.getFirstVisiblePosition();
        int offset = (mLvLocalMusic.getChildAt(0) == null) ? 0 : mLvLocalMusic.getChildAt(0).getTop();
        outState.putInt(ConstantUtil.LOCAL_MUSIC_POSITION, position);
        outState.putInt(ConstantUtil.LOCAL_MUSIC_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        mLvLocalMusic.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(ConstantUtil.LOCAL_MUSIC_POSITION);
                int offset = savedInstanceState.getInt(ConstantUtil.LOCAL_MUSIC_OFFSET);
                mLvLocalMusic.setSelectionFromTop(position, offset);
            }
        });
    }
}
