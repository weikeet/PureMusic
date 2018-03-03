package io.weicools.puremusic.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;

import io.weicools.puremusic.app.AppCache;
import io.weicools.puremusic.util.ConstantUtil;

/**
 * Author: weicools
 * Time: 2017/10/30 下午7:37
 */

public class QuitTimer {
    private Context mContext;
    private Handler mHandler;
    private long mTimerRemain;
    private OnTimerListener mListener;

    private QuitTimer() {
    }

    private static class QuitTimerHolder {
        private static final QuitTimer INSTANCE = new QuitTimer();
    }

    public static QuitTimer getInstance() {
        return QuitTimerHolder.INSTANCE;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void setOnTimerListener(OnTimerListener listener) {
        this.mListener = listener;
    }

    public void start(long milli) {
        stop();
        if (milli > 0) {
            mTimerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            mHandler.post(mQuitRunnable);
        } else {
            mTimerRemain = 0;
            if (mListener != null) {
                mListener.onTimer(mTimerRemain);
            }
        }
    }

    public void stop() {
        mHandler.removeCallbacks(mQuitRunnable);
    }

    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerRemain -= DateUtils.SECOND_IN_MILLIS;
            if (mTimerRemain > 0) {
                if (mListener != null) {
                    mListener.onTimer(mTimerRemain);
                }
                mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            } else {
                AppCache.getInstance().clearStack();
                MusicService.startCommand(mContext, ConstantUtil.ACTION_STOP);
            }
        }
    };

    public interface OnTimerListener {
        /**
         * 更新定时停止播放时间
         */
        void onTimer(long remain);
    }
}
