package io.weicools.puremusic.module.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.hwangjr.rxbus.RxBus;

/**
 * Created by weicools on 2018.03.27.
 * <p>
 * desc:
 */
public class BaseFragment extends Fragment {
    protected Handler mHandler;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        RxBus.get().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        setListener();
    }

    protected void setListener() {
    }

    @Override
    public void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }
}
