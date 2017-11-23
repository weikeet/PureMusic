package io.weicools.puremusic.ui.base;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.PermissionUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午7:47
 */

public abstract class BaseFragment extends Fragment {
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onStart() {
        super.onStart();
        setListener();
    }

    protected void setListener() {

    }

    protected MusicService getPlayService() {
        MusicService musicService = AppCache.getPlayService();
        if (musicService == null) {
            throw new NullPointerException("play service is null");
        }
        return musicService;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
