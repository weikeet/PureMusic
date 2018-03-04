package io.weicools.puremusic.service;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import io.weicools.puremusic.app.AppCache;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.util.CoverLoader;

/**
 * Author: weicools
 * Time: 2017/10/30 下午7:35
 */

public class MediaSessionManager {
    private static final String TAG = "MediaSessionManager";
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private MusicService mMusicService;
    private MediaSessionCompat mMediaSession;

    private MediaSessionManager() {
    }

    private static class MediaSessionManagerHolder {
        private static MediaSessionManager INSTANCE = new MediaSessionManager();
    }

    public static MediaSessionManager getInstance() {
        return MediaSessionManagerHolder.INSTANCE;
    }

    public void init(MusicService service) {
        this.mMusicService = service;
        setupMediaSession();
    }

    private void setupMediaSession() {
        mMediaSession = new MediaSessionCompat(mMusicService, TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setCallback(callback);
        mMediaSession.setActive(true);
    }

    public void updatePlaybackState() {
        int state = (AudioPlayer.getInstance().isPlaying() || AudioPlayer.getInstance().isPreparing()) ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mMediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(state, AudioPlayer.getInstance().getAudioPosition(), 1)
                        .build());
    }

    public void updateMetaData(Music music) {
        if (music == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, music.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, CoverLoader.getInstance().loadThumbnail(music));

        metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, AppCache.getInstance().getMusicList().size());

        mMediaSession.setMetadata(metaData.build());
    }

    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            AudioPlayer.getInstance().playPause();
        }

        @Override
        public void onPause() {
            AudioPlayer.getInstance().playPause();
        }

        @Override
        public void onSkipToNext() {
            AudioPlayer.getInstance().next();
        }

        @Override
        public void onSkipToPrevious() {
            AudioPlayer.getInstance().prev();
        }

        @Override
        public void onStop() {
            AudioPlayer.getInstance().stopPlayer();
        }

        @Override
        public void onSeekTo(long pos) {
            AudioPlayer.getInstance().seekTo((int) pos);
        }
    };
}
