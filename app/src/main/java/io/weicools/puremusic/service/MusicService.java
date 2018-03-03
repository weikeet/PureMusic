package io.weicools.puremusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.weicools.puremusic.app.Notifier;
import io.weicools.puremusic.util.ConstantUtil;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:10
 */

public class MusicService extends Service {
    private static final String TAG = MusicService.class.getSimpleName();

    public class PlayBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + getClass().getSimpleName());
        AudioPlayer.getInstance().init(this);
        MediaSessionManager.getInstance().init(this);
        Notifier.getInstance().init(this);
        QuitTimer.getInstance().init(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ConstantUtil.ACTION_STOP:
                    stop();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void stop() {
        AudioPlayer.getInstance().stopPlayer();
        QuitTimer.getInstance().stop();
        Notifier.getInstance().cancelAll();
    }
}
