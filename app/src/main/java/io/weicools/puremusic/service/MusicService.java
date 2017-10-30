package io.weicools.puremusic.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.Notifier;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.model.enums.PlayModeEnum;
import io.weicools.puremusic.receiver.NoisyAudioStreamReceiver;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.MusicUtil;
import io.weicools.puremusic.util.Preferences;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:10
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = MusicService.class.getSimpleName();
    private static final long TIME_UPDATE = 300L;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final Handler mHandler = new Handler();
    private MediaPlayer mPlayer = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    private MediaSessionManager mMediaSessionManager;
    private OnPlayerEventListener mPlayListener;

    private Music mPlayingMusic;    // 正在播放的歌曲[本地|网络]
    private int mPlayingPosition = -1;  // 正在播放的本地歌曲的序号
    private int mCurrPlayState = STATE_IDLE;    // 播放状态

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mPlayListener != null) {
                mPlayListener.onPublish(mPlayer.getCurrentPosition());
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (isPreparing()) {
                start();
            }
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mPlayListener != null) {
                mPlayListener.onBufferingUpdate(percent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + TAG);
        mAudioFocusManager = new AudioFocusManager(this);
        mMediaSessionManager = new MediaSessionManager(this);
        mPlayer.setOnCompletionListener(this);
        Notifier.init(this);
        QuitTimer.getInstance().init(this, mHandler, new EventCallback<Long>() {
            @Override
            public void onEvent(Long aLong) {
                if (mPlayListener != null) {
                    mPlayListener.onTimer(aLong);
                }
            }
        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ConstantUtil.ACTION_MEDIA_PLAY_PAUSE:

                    break;
                case ConstantUtil.ACTION_MEDIA_NEXT:

                    break;
                case ConstantUtil.ACTION_MEDIA_PREVIOUS:

                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    public OnPlayerEventListener getOnPlayEventListener() {
        return mPlayListener;
    }

    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mPlayListener = listener;
    }

    /**
     * 扫描音乐
     */
    @SuppressLint("StaticFieldLeak")
    public void updateMusicList(final EventCallback<Void> callback) {
        new AsyncTask<Void, Void, List<Music>>() {
            @Override
            protected List<Music> doInBackground(Void... params) {
                return MusicUtil.scanMusic(MusicService.this);
            }

            @Override
            protected void onPostExecute(List<Music> musicList) {
                AppCache.getMusicList().clear();
                AppCache.getMusicList().addAll(musicList);

                if (!AppCache.getMusicList().isEmpty()) {
                    updatePlayingPosition();
                    mPlayingMusic = AppCache.getMusicList().get(mPlayingPosition);
                }

                if (mPlayListener != null) {
                    mPlayListener.onMusicListUpdate();
                }

                if (callback != null) {
                    callback.onEvent(null);
                }
            }
        }.execute();
    }

    public void play(int position) {
        if (AppCache.getMusicList().isEmpty()) {
            return;
        }

        if (position < 0) {
            position = AppCache.getMusicList().size() - 1;
        } else if (position >= AppCache.getMusicList().size()) {
            position = 0;
        }

        mPlayingPosition = position;
        Music music = AppCache.getMusicList().get(mPlayingPosition);
        Preferences.saveCurrentSongId(music.getId());
        play(music);
    }

    public void play(Music music) {
        mPlayingMusic = music;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(music.getPath());
            mPlayer.prepareAsync();
            mCurrPlayState = STATE_PREPARING;
            mPlayer.setOnPreparedListener(mPreparedListener);
            mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

            if (mPlayListener != null) {
                mPlayListener.onChange(music);
            }

            Notifier.showPlay(music);
            mMediaSessionManager.updateMetaData(mPlayingMusic);
            mMediaSessionManager.updatePlaybackState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPause() {
        if (isPreparing()) {
            stop();
        } else if (isPlaying()) {
            pause();
        } else if (isPausing()) {
            start();
        } else {
            play(getPlayingPosition());
        }
    }

    void start() {
        if (!isPreparing() && !isPausing()) {
            return;
        }

        if (mAudioFocusManager.requestAudioFocus()) {
            mPlayer.start();
            mCurrPlayState = STATE_PLAYING;
            mHandler.post(mPublishRunnable);
            Notifier.showPlay(mPlayingMusic);
            mMediaSessionManager.updatePlaybackState();
            registerReceiver(mNoisyReceiver, mNoisyFilter);

            if (mPlayListener != null) {
                mPlayListener.onPlayerStart();
            }
        }
    }

    void pause() {
        if (!isPlaying()) {
            return;
        }

        mPlayer.pause();
        mCurrPlayState = STATE_PAUSE;
        mHandler.removeCallbacks(mPublishRunnable);
        Notifier.showPause(mPlayingMusic);
        mMediaSessionManager.updatePlaybackState();
        unregisterReceiver(mNoisyReceiver);

        if (mPlayListener != null) {
            mPlayListener.onPlayerPause();
        }
    }

    public void stop() {
        if (isIdle()) {
            return;
        }

        pause();
        mPlayer.reset();
        mCurrPlayState = STATE_IDLE;
    }

    public void next() {
        if (AppCache.getMusicList().isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(AppCache.getMusicList().size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition + 1);
                break;
        }
    }

    public void prev() {
        if (AppCache.getMusicList().isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(AppCache.getMusicList().size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition - 1);
                break;
        }
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param t 时间
     */
    public void seekTo(int t) {
        if (isPlaying() || isPausing()) {
            mPlayer.seekTo(t);
            mMediaSessionManager.updatePlaybackState();
            if (mPlayListener != null) {
                mPlayListener.onPublish(t);
            }
        }
    }

    /* judge play state start */
    public boolean isPlaying() {
        return mCurrPlayState == STATE_PLAYING;
    }

    public boolean isPausing() {
        return mCurrPlayState == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return mCurrPlayState == STATE_PREPARING;
    }

    public boolean isIdle() {
        return mCurrPlayState == STATE_IDLE;
    }
    /* judge play state end */

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        for (int i = 0; i < AppCache.getMusicList().size(); i++) {
            if (AppCache.getMusicList().get(i).getId() == id) {
                position = i;
                break;
            }
        }
        mPlayingPosition = position;
        Preferences.saveCurrentSongId(AppCache.getMusicList().get(mPlayingPosition).getId());
    }

    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    public long getCurrentPosition() {
        if (isPlaying() || isPausing()) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public void quit() {
        stop();
        QuitTimer.getInstance().stop();
        stopSelf();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
