package io.weicools.puremusic.executor;

import android.text.TextUtils;

import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.model.Lrc;
import io.weicools.puremusic.model.SearchMusic;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午7:10
 */

public abstract class SearchLrc implements IExecutor<String> {
    private String mArtist;
    private String mTitle;

    public SearchLrc(String artist, String title) {
        mArtist = artist;
        mTitle = title;
    }

    @Override
    public void execute() {
        onPrepare();
        searchLrc();
    }

    private void searchLrc() {
        HttpClient.searchMusic(mTitle + "-" + mArtist, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic searchMusic) {
                if (searchMusic == null || searchMusic.getSong() == null || searchMusic.getSong().isEmpty()) {
                    onFail(null);
                    return;
                }

                downloadLrc(searchMusic.getSong().get(0).getSongid());
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    }

    private void downloadLrc(String songId) {
        HttpClient.getLrc(songId, new HttpCallback<Lrc>() {
            @Override
            public void onSuccess(Lrc lrc) {
                if (lrc == null || TextUtils.isEmpty(lrc.getLrcContent())) {
                    onFail(null);
                    return;
                }

                String filePath = FileUtil.getLrcDir() + FileUtil.getLrcFileName(mArtist, mTitle);
                FileUtil.saveLrcFile(filePath, lrc.getLrcContent());
                onExecuteSuccess(filePath);
            }

            @Override
            public void onFail(Exception e) {

            }
        });
    }
}
