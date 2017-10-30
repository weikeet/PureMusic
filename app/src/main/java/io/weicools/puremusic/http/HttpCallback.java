package io.weicools.puremusic.http;

/**
 * Author: weicools
 * Time: 2017/10/30 上午9:48
 */

public abstract class HttpCallback<T> {
    public abstract void onSuccess(T t);

    public abstract void onFail(Exception e);

    public void onFinish() {
    }
}
