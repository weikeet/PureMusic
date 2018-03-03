package io.weicools.puremusic.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:45
 */

public class ActivityObserver implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityObserver";
    private static final long CHECK_TASK_DELAY = 500;

    private List<Observer> mObserverList;
    private Handler mHandler;
    private boolean isForeground;
    private int mResumeActivityCount;

    public interface Observer {
        /**
         * 进入前台
         *
         * @param activity 当前处于栈顶的Activity
         */
        void onForeground(Activity activity);

        /**
         * 进入后台
         *
         * @param activity 当前处于栈顶的Activity
         */
        void onBackground(Activity activity);
    }

    private static class SingletonHolder {
        private static ActivityObserver sInstance = new ActivityObserver();
    }

    private ActivityObserver() {
        mObserverList = Collections.synchronizedList(new ArrayList<Observer>());
        mHandler = new Handler(Looper.getMainLooper());
    }

    private static ActivityObserver getInstance() {
        return SingletonHolder.sInstance;
    }

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(getInstance());
    }

    public static void addObserver(Observer observer) {
        if (observer == null) {
            return;
        }

        if (getInstance().mObserverList.contains(observer)) {
            return;
        }

        getInstance().mObserverList.add(observer);
    }

    public static void removeObserver(Observer observer) {
        if (observer == null) {
            return;
        }

        getInstance().mObserverList.remove(observer);
    }

    private void notify(Activity activity, boolean foreground) {
        for (Observer observer : mObserverList) {
            if (foreground) {
                observer.onForeground(activity);
            } else {
                observer.onBackground(activity);
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mResumeActivityCount++;
        if (!isForeground && mResumeActivityCount > 0) {
            isForeground = true;
            // 从后台进入前台
            Log.i(TAG, "app in foreground");
            notify(activity, true);
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        mResumeActivityCount--;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isForeground && mResumeActivityCount == 0) {
                    isForeground = false;
                    // 从前台进入后台
                    Log.i(TAG, "app in background");
                    ActivityObserver.this.notify(activity, false);
                }
            }
        }, CHECK_TASK_DELAY);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
