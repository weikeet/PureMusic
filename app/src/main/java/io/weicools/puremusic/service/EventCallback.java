package io.weicools.puremusic.service;

/**
 * Author: weicools
 * Time: 2017/10/30 下午7:35
 */

public interface EventCallback<T> {
    void onEvent(T t);
}
