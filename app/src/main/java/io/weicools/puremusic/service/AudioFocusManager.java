package io.weicools.puremusic.service;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:24
 */

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {
    private AudioManager mAudioManager;
    private boolean isPausedByFocusLossTransient;

    public AudioFocusManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
    }

    public boolean requestAudioFocus() {
        return mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int audioFocus) {
        switch (audioFocus) {
            // 重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (isPausedByFocusLossTransient) {
                    // 通话结束，恢复播放
                    AudioPlayer.getInstance().startPlayer();
                }

                // 恢复音量
                AudioPlayer.getInstance().getMediaPlayer().setVolume(1f, 1f);

                isPausedByFocusLossTransient = false;
                break;
            // 永久丢失焦点，如被其他播放器抢占
            case AudioManager.AUDIOFOCUS_LOSS:
                AudioPlayer.getInstance().pausePlayer();
                break;
            // 短暂丢失焦点，如来电
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                AudioPlayer.getInstance().pausePlayer(false);
                isPausedByFocusLossTransient = true;
                break;
            // 瞬间丢失焦点，如通知
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // 音量减小为一半
                AudioPlayer.getInstance().getMediaPlayer().setVolume(0.5f, 0.5f);
                break;
            default:
                break;
        }
    }
}
