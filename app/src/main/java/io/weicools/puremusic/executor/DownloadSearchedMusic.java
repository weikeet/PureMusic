package io.weicools.puremusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;

import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.DownloadInfo;
import io.weicools.puremusic.data.Lrc;
import io.weicools.puremusic.data.SearchMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * download search music
 *
 * Author: weicools
 * Time: 2017/11/22 上午11:27
 */

public abstract class DownloadSearchedMusic extends DownloadMusic {
    private SearchMusic.Song mSong;

    public DownloadSearchedMusic(Activity activity, SearchMusic.Song song) {
        super(activity);
        mSong = song;
    }

    @Override
    protected void download() {
        final String artist = mSong.getArtistname();
        final String title = mSong.getSongname();

        // get music download link
        HttpClient.getMusicDownloadInfo(mSong.getSongid(), new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                    onFail(null);
                    return;
                }

                downloadMusic(downloadInfo.getBitrate().getFile_link(), artist, title, null);
                onExecuteSuccess(null);
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });

        // download lrc
        final String lrcFileName = FileUtil.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtil.getLrcDir(), lrcFileName);
        if (!lrcFile.exists()) {
            HttpClient.getLrc(mSong.getSongid(), new HttpCallback<Lrc>() {
                @Override
                public void onSuccess(Lrc lrc) {
                    if (lrc == null || TextUtils.isEmpty(lrc.getLrcContent())) {
                        onFail(null);
                        return;
                    }

                    String filePath = FileUtil.getLrcDir() + lrcFileName;
                    FileUtil.saveLrcFile(filePath, lrc.getLrcContent());
                }

                @Override
                public void onFail(Exception e) {
                    onExecuteFail(e);
                }
            });
        }
    }
}
