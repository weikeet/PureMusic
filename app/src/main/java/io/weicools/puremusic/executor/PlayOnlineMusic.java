package io.weicools.puremusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;

import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.DownloadInfo;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.data.OnlineMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午2:39
 */

public abstract class PlayOnlineMusic extends PlayMusic {
    private OnlineMusic mOnlineMusic;

    public PlayOnlineMusic(Activity activity, OnlineMusic onlineMusic) {
        super(activity, 3);
        mOnlineMusic = onlineMusic;
    }

    @Override
    protected void getPlayInfo() {
        String artist = mOnlineMusic.getArtist_name();
        String title = mOnlineMusic.getTitle();

        mMusic = new Music();
        mMusic.setType(Music.Type.ONLINE);
        mMusic.setTitle(title);
        mMusic.setArtist(artist);
        mMusic.setAlbum(mOnlineMusic.getAlbum_title());

        // download lrc
        String lrcFileName = FileUtil.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtil.getLrcDir(), lrcFileName);
        if (!lrcFile.exists() && !TextUtils.isEmpty(mOnlineMusic.getLrc_link())) {
            HttpClient.downloadFile(mOnlineMusic.getLrc_link(), FileUtil.getLrcDir(), lrcFileName, new HttpCallback<File>() {
                @Override
                public void onSuccess(File file) {
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

        // download cover
        String albumFileName = FileUtil.getAlbumFileName(artist, title);
        File albumFile = new File(FileUtil.getAlbumDir(), albumFileName);
        String picUrl = mOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mOnlineMusic.getPic_small();
        }
        if (!albumFile.exists() && !TextUtils.isEmpty(picUrl)) {
            HttpClient.downloadFile(picUrl, FileUtil.getAlbumDir(), albumFileName, new HttpCallback<File>() {
                @Override
                public void onSuccess(File file) {
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
        mMusic.setCoverPath(albumFile.getPath());

        // get music play link
        HttpClient.getMusicDownloadInfo(mOnlineMusic.getSong_id(), new HttpCallback<DownloadInfo>() {
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
