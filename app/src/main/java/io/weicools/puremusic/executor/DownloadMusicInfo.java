package io.weicools.puremusic.executor;

/**
 * Author: weicools
 * Time: 2017/10/31 上午10:14
 */

public class DownloadMusicInfo {
    private String title;
    private String musicPath;
    private String coverPath;

    public DownloadMusicInfo(String title, String musicPath, String coverPath) {
        this.title = title;
        this.musicPath = musicPath;
        this.coverPath = coverPath;
    }

    public String getTitle() {
        return title;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public String getCoverPath() {
        return coverPath;
    }
}
