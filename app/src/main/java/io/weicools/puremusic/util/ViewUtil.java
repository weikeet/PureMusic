package io.weicools.puremusic.util;

import android.view.View;

import io.weicools.puremusic.model.enums.LoadStateEnum;

/**
 * Author: weicools
 * Time: 2017/10/30 下午7:14
 */

public class ViewUtil {
    public static void changeViewState(View loadSuccess, View loading, View loadFail, LoadStateEnum state) {
        switch (state) {
            case LOADING:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_SUCCESS:
                loadSuccess.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_FAIL:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.VISIBLE);
                break;
        }
    }
}
