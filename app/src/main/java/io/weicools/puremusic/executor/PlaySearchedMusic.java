package io.weicools.puremusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;

import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.model.DownloadInfo;
import io.weicools.puremusic.model.Lrc;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.model.SearchMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午5:41
 */

public abstract class PlaySearchedMusic extends PlayMusic {
    private SearchMusic.Song mSong;

    public PlaySearchedMusic(Activity activity, SearchMusic.Song song) {
        super(activity, 2);
        mSong = song;
    }

    @Override
    protected void getPlayInfo() {
        String lrcFileName = FileUtil.getLrcFileName(mSong.getArtistname(), mSong.getSongname());
        final File lrcFile = new File(FileUtil.getLrcDir() + lrcFileName);
        if (!lrcFile.exists()) {
            HttpClient.getLrc(mSong.getSongid(), new HttpCallback<Lrc>() {
                @Override
                public void onSuccess(Lrc lrc) {
                    if (lrc == null || TextUtils.isEmpty(lrc.getLrcContent())) {
                        return;
                    }

                    FileUtil.saveLrcFile(lrcFile.getPath(), lrc.getLrcContent());
                }

                @Override
                public void onFail(Exception e) {
                }

                @Override
                public void onFinish() {
                    checkCounter();
                }
            });
        } else {
            mCounter++;
        }

        mMusic = new Music();
        mMusic.setType(Music.Type.ONLINE);
        mMusic.setTitle(mSong.getSongname());
        mMusic.setArtist(mSong.getArtistname());

        // get music play link
        HttpClient.getMusicDownloadInfo(mSong.getSongid(), new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                    onFail(null);
                    return;
                }

                mMusic.setPath(downloadInfo.getBitrate().getFile_link());
                mMusic.setDuration(downloadInfo.getBitrate().getFile_duration() * 1000);
                checkCounter();
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }
}
