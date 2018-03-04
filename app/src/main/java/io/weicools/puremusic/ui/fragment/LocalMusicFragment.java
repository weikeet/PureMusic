package io.weicools.puremusic.ui.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;

import java.io.File;
import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.OnMoreClickListener;
import io.weicools.puremusic.adapter.PlayListAdapter;
import io.weicools.puremusic.app.AppCache;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.ui.activity.MusicInfoActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.MusicUtil;
import io.weicools.puremusic.util.PermissionUtil;
import io.weicools.puremusic.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends Fragment implements OnMoreClickListener, AdapterView.OnItemClickListener {

    private ListView mLvLocalMusic;
    private TextView mTvEmpty;

    private PlayListAdapter mAdapter;

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

        mAdapter = new PlayListAdapter(AppCache.getInstance().getMusicList());
        mAdapter.setOnMoreClickListener(this);
        mLvLocalMusic.setAdapter(mAdapter);
        mLvLocalMusic.setOnItemClickListener(this);

        if (AppCache.getInstance().getMusicList().isEmpty()) {
            scanMusic();
        }
    }

    @Subscribe(tags = {@Tag(ConstantUtil.SCAN_MUSIC)})
    public void scanMusic() {
        mLvLocalMusic.setVisibility(View.GONE);
        mTvEmpty.setVisibility(View.VISIBLE);
        PermissionUtil.with(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .result(new PermissionUtil.Result() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onGranted() {
                        new AsyncTask<Void, Void, List<Music>>() {
                            @Override
                            protected List<Music> doInBackground(Void... params) {
                                return MusicUtil.scanMusic(getContext());
                            }

                            @Override
                            protected void onPostExecute(List<Music> musicList) {
                                AppCache.getInstance().getMusicList().clear();
                                AppCache.getInstance().getMusicList().addAll(musicList);
                                mLvLocalMusic.setVisibility(View.VISIBLE);
                                mTvEmpty.setVisibility(View.GONE);
                                mAdapter.notifyDataSetChanged();
                            }
                        }.execute();
                    }

                    @Override
                    public void onDenied() {
                        ToastUtil.showShort(R.string.no_permission_storage);
                        mLvLocalMusic.setVisibility(View.VISIBLE);
                        mTvEmpty.setVisibility(View.GONE);
                    }
                })
                .request();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music music = AppCache.getInstance().getMusicList().get(position);
        AudioPlayer.getInstance().addAndPlay(music);
        ToastUtil.showShort("已添加到播放列表");
    }

    @Override
    public void onMoreClick(final int position) {
        Music music = AppCache.getInstance().getMusicList().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(music.getTitle());
        builder.setItems(R.array.local_music_dialog, (dialog1, which) -> {
            switch (which) {
                case 0:// 分享
                    shareMusic(music);
                    break;
                case 1:// 设为铃声
                    requestSetRingtone(music);
                    break;
                case 2:// 查看歌曲信息
                    Intent intent = new Intent(getContext(), MusicInfoActivity.class);
                    intent.putExtra(ConstantUtil.MUSIC, music);
                    startActivity(intent);
                    break;
                case 3:// 删除
                    deleteMusic(music);
                    break;
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
                    AppCache.getInstance().getMusicList().remove(music);
                    mAdapter.notifyDataSetChanged();
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
        // FIXME: 2018/3/4 null
        if (mLvLocalMusic != null) {
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
}
