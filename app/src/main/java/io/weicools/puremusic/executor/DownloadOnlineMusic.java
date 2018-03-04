package io.weicools.puremusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;

import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.DownloadInfo;
import io.weicools.puremusic.data.OnlineMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * download music
 *
 * Author: weicools
 * Time: 2017/11/22 上午11:11
 */

public abstract class DownloadOnlineMusic extends DownloadMusic {
    private OnlineMusic mOnlineMusic;

    public DownloadOnlineMusic(Activity activity, OnlineMusic onlineMusic) {
        super(activity);
        mOnlineMusic = onlineMusic;
    }

    @Override
    protected void download() {
        final String artist = mOnlineMusic.getArtist_name();
        final String title = mOnlineMusic.getTitle();

        // download lrc
        String lrcFileName = FileUtil.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtil.getLrcDir() + lrcFileName);
        if (!TextUtils.isEmpty(mOnlineMusic.getLrc_link()) && !lrcFile.exists()) {
            HttpClient.downloadFile(mOnlineMusic.getLrc_link(), FileUtil.getLrcDir(), lrcFileName, null);
        }

        // download caver
        String albumFileName = FileUtil.getAlbumFileName(artist, title);
        final File albumFile = new File(FileUtil.getAlbumDir(), albumFileName);
        String picUrl = mOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mOnlineMusic.getPic_small();
        }
        if (!TextUtils.isEmpty(picUrl) && !albumFile.exists()) {
            HttpClient.downloadFile(picUrl, FileUtil.getAlbumDir(), albumFileName, null);
        }

        // get music download link
        HttpClient.getMusicDownloadInfo(mOnlineMusic.getSong_id(), new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                    onFail(null);
                    return;
                }

                downloadMusic(downloadInfo.getBitrate().getFile_link(), artist, title, albumFile.getPath());
                onExecuteSuccess(null);
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }
}
