package io.weicools.puremusic.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.executor.DownloadMusicInfo;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.ToastUtil;
import io.weicools.puremusic.util.id3.ID3TagUtils;
import io.weicools.puremusic.util.id3.ID3Tags;

/**
 * Author: weicools
 * Time: 2017/11/23 上午11:26
 */

public class DownloadReceiver extends BroadcastReceiver {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        DownloadMusicInfo musicInfo = AppCache.getDownloadList().get(id);

        if (musicInfo != null) {
            ToastUtil.showShort(context, context.getString(R.string.download_success, musicInfo.getTitle()));

            String musicPath = musicInfo.getMusicPath();
            String coverPath = musicInfo.getCoverPath();
            if (!TextUtils.isEmpty(musicPath) && !TextUtils.isEmpty(coverPath)) {
                File musicFile = new File(musicPath);
                File coverFile = new File(coverPath);

                if (musicFile.exists() && coverFile.exists()) {
                    ID3Tags id3Tags = new ID3Tags.Builder()
                            .setCoverFile(coverFile)
                            .build();
                    ID3TagUtils.setID3Tags(musicFile, id3Tags, false);
                }
            }

            // 由于系统扫描音乐是异步执行，因此延迟刷新音乐列表
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanMusic();
                }
            }, 1000);
        }
    }

    private void scanMusic() {
        MusicService service = AppCache.getPlayService();
        if (service != null) {
            service.updateMusicList(null);
        }
    }
}
