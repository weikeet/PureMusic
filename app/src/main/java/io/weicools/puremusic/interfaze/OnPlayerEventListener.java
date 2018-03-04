package io.weicools.puremusic.interfaze;

import io.weicools.puremusic.data.Music;

/**
 * 播放进度监听器
 * Author: weicools
 * Time: 2017/10/30 下午5:29
 */

public interface OnPlayerEventListener {
    /**
     * 切换歌曲
     */
    void onChange(Music music);

    /**
     * 继续播放
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 更新进度
     */
    void onPublish(int progress);

    /**
     * 缓冲百分比
     */
    void onBufferingUpdate(int percent);
}
