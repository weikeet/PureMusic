package io.weicools.puremusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.ConstantUtil;

/**
 * Author: weicools
 * Time: 2017/11/23 上午11:37
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
                MusicService.startCommand(context, ConstantUtil.ACTION_MEDIA_PLAY_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                MusicService.startCommand(context, ConstantUtil.ACTION_MEDIA_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                MusicService.startCommand(context, ConstantUtil.ACTION_MEDIA_PREVIOUS);
                break;
        }
    }
}
