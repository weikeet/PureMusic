package io.weicools.puremusic;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.executor.DownloadMusicInfo;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.model.SongListInfo;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ScreenUtil;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:30
 */

public class AppCache {
    private Context mContext;
    private MusicService mMusicService;
    // 本地歌曲列表
    private final List<Music> mMusicList = new ArrayList<>();
    // 歌单列表
    private final List<SongListInfo> mSongListInfo = new ArrayList<>();
    private final List<Activity> mActivityStack = new ArrayList<>();
    private final LongSparseArray<DownloadMusicInfo> mDownloadList = new LongSparseArray<>();

    private AppCache() {}

    private static class AppCacheHolder {
        private static final AppCache sAppCache = new AppCache();
    }

    private static AppCache getInstance() {
        return AppCacheHolder.sAppCache;
    }

    public static void init(Application application) {
        getInstance().onInit(application);
    }

    private void onInit(Application application) {
        mContext = application.getApplicationContext();

        Preferences.init(mContext);
        ScreenUtil.init(mContext);
        //CrashHandler.getInstance().init();
        CoverLoader.getInstance().init(mContext);
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    public static Context getContext() {
        return getInstance().mContext;
    }

    public static MusicService getPlayService() {
        return getInstance().mMusicService;
    }

    public static void setPlayService(MusicService service) {
        getInstance().mMusicService = service;
    }

    public static List<Music> getMusicList() {
        return getInstance().mMusicList;
    }

    public static List<SongListInfo> getSongListInfos() {
        return getInstance().mSongListInfo;
    }

    public static void clearStack() {
        List<Activity> activityStack = getInstance().mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityStack.clear();
    }

    public static LongSparseArray<DownloadMusicInfo> getDownloadList() {
        return getInstance().mDownloadList;
    }

    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private static final String TAG = "Activity";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate: " + activity.getClass().getSimpleName());
            getInstance().mActivityStack.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onDestroy: " + activity.getClass().getSimpleName());
            getInstance().mActivityStack.remove(activity);
        }
    }
}
