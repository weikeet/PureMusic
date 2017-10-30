package io.weicools.puremusic.http;

import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Response;

/**
 * Author: weicools
 * Time: 2017/10/30 上午9:38
 */

public abstract class JsonCallback<T> extends Callback<T> {
    private Class<T> mClass;
    private Gson mGson;

    public JsonCallback(Class<T> clazz) {
        this.mClass = clazz;
        mGson = new Gson();
    }

    @Override
    public T parseNetworkResponse(Response response, int id) throws Exception {
        try {
            String jsonStr = response.body().string();
            return mGson.fromJson(jsonStr, mClass);
        } catch (Exception e) {

        }

        return null;
    }
}
