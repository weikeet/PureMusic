package io.weicools.puremusic.service;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.weicools.puremusic.app.Notifier;
import io.weicools.puremusic.data.database.DBManager;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.enums.PlayModeEnum;
import io.weicools.puremusic.interfaze.OnPlayerEventListener;
import io.weicools.puremusic.receiver.NoisyAudioStreamReceiver;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ToastUtil;

/**
 * Create by weicools on 2018/3/4.
 */

public class AudioPlayer {
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private static final long TIME_UPDATE = 300L;

    private Context mContext;
    private Handler mHandler;
    private List<Music> mMusicList;

    private MediaPlayer mMediaPlayer;
    private IntentFilter mIntentFilter;
    private AudioFocusManager mFocusManager;
    private NoisyAudioStreamReceiver mStreamReceiver;
    private final List<OnPlayerEventListener> mListenerList = new ArrayList<>();

    private int state = STATE_IDLE;

    private AudioPlayer() {

    }

    private static class AudioPlayerHolder {
        private static final AudioPlayer INSTANCE = new AudioPlayer();
    }

    public static AudioPlayer getInstance() {
        return AudioPlayerHolder.INSTANCE;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mMusicList = DBManager.getInstance().getMusicDao().queryBuilder().build().list();
        mFocusManager = new AudioFocusManager(context);
        mMediaPlayer = new MediaPlayer();
        mHandler = new Handler(Looper.getMainLooper());
        mStreamReceiver = new NoisyAudioStreamReceiver();
        mIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> next());
        mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
            if (isPreparing()) {
                startPlayer();
            }
        });
        mMediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            for (OnPlayerEventListener listener : mListenerList) {
                listener.onBufferingUpdate(percent);
            }
        });
    }

    public void addOnPlayEventListener(OnPlayerEventListener listener) {
        if (!mListenerList.contains(listener)) {
            mListenerList.add(listener);
        }
    }

    public void removeOnPlayEventListener(OnPlayerEventListener listener) {
        mListenerList.remove(listener);
    }

    public void addAndPlay(Music music) {
        int position = mMusicList.indexOf(music);
        if (position < 0) {
            mMusicList.add(music);
            position = mMusicList.size() - 1;
        }

        play(position);
    }

    public void play(int position) {
        if (mMusicList.isEmpty()) {
            return;
        }

        if (position < 0) {
            position = mMusicList.size() - 1;
        } else if (position >= mMusicList.size()) {
            position = 0;
        }

        setPlayPosition(position);
        Music music = getPlayMusic();

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(music.getPath());
            mMediaPlayer.prepareAsync();
            state = STATE_PREPARING;
            for (OnPlayerEventListener listener : mListenerList) {
                listener.onChange(music);
            }
            Notifier.getInstance().showPlay(music);
            MediaSessionManager.getInstance().updateMetaData(music);
            MediaSessionManager.getInstance().updatePlaybackState();
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.showShort("当前歌曲无法播放");
        }
    }

    public void delete(int position) {
        int playPosition = getPlayPosition();
        Music music = mMusicList.remove(position);
        DBManager.getInstance().getMusicDao().delete(music);
        if (playPosition > position) {
            setPlayPosition(playPosition - 1);
        } else if (playPosition == position) {
            if (isPlaying() || isPreparing()) {
                setPlayPosition(playPosition - 1);
                next();
            } else {
                stopPlayer();
                for (OnPlayerEventListener listener : mListenerList) {
                    listener.onChange(getPlayMusic());
                }
            }
        }
    }

    public void playPause() {
        if (isPreparing()) {
            stopPlayer();
        } else if (isPlaying()) {
            pausePlayer();
        } else if (isPausing()) {
            startPlayer();
        } else {
            play(getPlayPosition());
        }
    }

    public void startPlayer() {
        if (!isPreparing() && !isPausing()) {
            return;
        }

        if (mFocusManager.requestAudioFocus()) {
            mMediaPlayer.start();
            state = STATE_PLAYING;
            mHandler.post(mPublishRunnable);
            Notifier.getInstance().showPlay(getPlayMusic());
            MediaSessionManager.getInstance().updatePlaybackState();
            mContext.registerReceiver(mStreamReceiver, mIntentFilter);

            for (OnPlayerEventListener listener : mListenerList) {
                listener.onPlayerStart();
            }
        }
    }

    public void pausePlayer() {
        pausePlayer(true);
    }

    public void pausePlayer(boolean abandonAudioFocus) {
        if (!isPlaying()) {
            return;
        }

        mMediaPlayer.pause();
        state = STATE_PAUSE;
        mHandler.removeCallbacks(mPublishRunnable);
        Notifier.getInstance().showPause(getPlayMusic());
        MediaSessionManager.getInstance().updatePlaybackState();
        mContext.unregisterReceiver(mStreamReceiver);
        if (abandonAudioFocus) {
            mFocusManager.abandonAudioFocus();
        }

        for (OnPlayerEventListener listener : mListenerList) {
            listener.onPlayerPause();
        }
    }

    public void stopPlayer() {
        if (isIdle()) {
            return;
        }

        pausePlayer();
        mMediaPlayer.reset();
        state = STATE_IDLE;
    }

    public void next() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                play(new Random().nextInt(mMusicList.size()));
                break;
            case SINGLE:
                play(getPlayPosition());
                break;
            case LOOP:
            default:
                play(getPlayPosition() + 1);
                break;
        }
    }

    public void prev() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                play(new Random().nextInt(mMusicList.size()));
                break;
            case SINGLE:
                play(getPlayPosition());
                break;
            case LOOP:
            default:
                play(getPlayPosition() - 1);
                break;
        }
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mMediaPlayer.seekTo(msec);
            MediaSessionManager.getInstance().updatePlaybackState();
            for (OnPlayerEventListener listener : mListenerList) {
                listener.onPublish(msec);
            }
        }
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                for (OnPlayerEventListener listener : mListenerList) {
                    listener.onPublish(mMediaPlayer.getCurrentPosition());
                }
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

    public long getAudioPosition() {
        if (isPlaying() || isPausing()) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public Music getPlayMusic() {
        if (mMusicList.isEmpty()) {
            return null;
        }
        return mMusicList.get(getPlayPosition());
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public List<Music> getMusicList() {
        return mMusicList;
    }

    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    public boolean isPausing() {
        return state == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return state == STATE_PREPARING;
    }

    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    public int getPlayPosition() {
        int position = Preferences.getPlayPosition();
        if (position < 0 || position >= mMusicList.size()) {
            position = 0;
            Preferences.savePlayPosition(position);
        }
        return position;
    }

    private void setPlayPosition(int position) {
        Preferences.savePlayPosition(position);
    }
}
