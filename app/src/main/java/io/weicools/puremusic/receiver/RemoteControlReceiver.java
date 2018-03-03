package io.weicools.puremusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.ConstantUtil;

/**
 * Author: weicools
 * Time: 2017/11/23 上午11:37
 *
 * 耳机线控，仅在5.0以下有效，5.0以上被{@link MediaSessionCompat}接管。
 */

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                AudioPlayer.getInstance().playPause();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                AudioPlayer.getInstance().next();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                AudioPlayer.getInstance().prev();
                break;
        }
    }
}
