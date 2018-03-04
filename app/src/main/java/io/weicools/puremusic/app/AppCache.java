package io.weicools.puremusic.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.executor.DownloadMusicInfo;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.data.SongSheetInfo;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ScreenUtil;
import io.weicools.puremusic.util.ToastUtil;

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
    private final List<SongSheetInfo> mSongSheetList = new ArrayList<>();
    private final List<Activity> mActivityStack = new ArrayList<>();
    private final LongSparseArray<DownloadMusicInfo> mDownloadList = new LongSparseArray<>();

    private AppCache() {
    }

    private static class AppCacheHolder {
        private static AppCache INSTANCE = new AppCache();
    }

    public static AppCache getInstance() {
        return AppCacheHolder.INSTANCE;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
        ToastUtil.init(mContext);
        Preferences.init(mContext);
        ScreenUtil.init(mContext);
        //CrashHandler.getInstance().init();
        CoverLoader.getInstance().init(mContext);
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    public Context getContext() {
        return mContext;
    }

    public MusicService getMusicService() {
        return mMusicService;
    }

    public void setMusicService(MusicService service) {
        mMusicService = service;
    }

    public List<Music> getMusicList() {
        return mMusicList;
    }

    public List<SongSheetInfo> getSongSheetList() {
        return mSongSheetList;
    }

    public void clearStack() {
        List<Activity> activityStack = mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityStack.clear();
    }

    public LongSparseArray<DownloadMusicInfo> getDownloadList() {
        return mDownloadList;
    }

    private class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private static final String TAG = "Activity";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate: " + activity.getClass().getSimpleName());
            mActivityStack.add(activity);
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
            mActivityStack.remove(activity);
        }
    }
}
