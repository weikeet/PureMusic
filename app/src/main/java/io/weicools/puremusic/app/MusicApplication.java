package io.weicools.puremusic.app;

import android.app.Application;
import android.content.Intent;

import com.squareup.leakcanary.LeakCanary;

import io.weicools.puremusic.data.database.DBManager;
import io.weicools.puremusic.service.MusicService;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:20
 */

public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCache.getInstance().init(this);
        ActivityObserver.init(this);
        DBManager.getInstance().init(this);

        Intent intent = new Intent(this, MusicService.class);
        startService(intent);

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }
}
