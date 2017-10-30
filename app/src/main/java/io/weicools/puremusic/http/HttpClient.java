package io.weicools.puremusic.http;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import io.weicools.puremusic.model.ArtistInfo;
import io.weicools.puremusic.model.DownloadInfo;
import io.weicools.puremusic.model.Lrc;
import io.weicools.puremusic.model.OnlineMusicList;
import io.weicools.puremusic.model.SearchMusic;
import io.weicools.puremusic.model.Splash;
import okhttp3.Call;

/**
 * Author: weicools
 * Time: 2017/10/30 上午9:48
 */

public class HttpClient {
    public static final  String TAG = HttpClient.class.getSimpleName();
    private static final String BASE_URL = "http://tingapi.ting.baidu.com/v1/restserver/ting";
    private static final String SPLASH_URL = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";

    private static final String METHOD_GET_MUSIC_LIST = "baidu.ting.billboard.billList";
    private static final String METHOD_DOWNLOAD_MUSIC = "baidu.ting.song.play";
    private static final String METHOD_ARTIST_INFO = "baidu.ting.artist.getInfo";
    private static final String METHOD_SEARCH_MUSIC = "baidu.ting.search.catalogSug";
    private static final String METHOD_LRC = "baidu.ting.song.lry";

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SONG_ID = "songid";
    private static final String PARAM_TING_UID = "tinguid";
    private static final String PARAM_QUERY = "query";

    public static void getSplash(@NonNull final HttpCallback<Splash> callback) {
        OkHttpUtils.get().url(SPLASH_URL).build()
                .execute(new JsonCallback<Splash>(Splash.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getSplash onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(Splash response, int id) {
                        Log.i(TAG, "getSplash onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getSplash onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void downloadFile(String url, String destFileDir, String destFileName, @Nullable final HttpCallback<File> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new FileCallBack(destFileDir, destFileName) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callback != null) {
                            Log.e(TAG, "downloadFile onError: " + e.getMessage());
                            callback.onFail(e);
                        }
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        if (callback != null) {
                            Log.i(TAG, "downloadFile onSuccess: " + id);
                            callback.onSuccess(response);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        if (callback != null) {
                            Log.d(TAG, "downloadFile onFinish: " + id);
                            callback.onFinish();
                        }
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {}
                });
    }

    public static void getSongListInfo(String type, int size, int offset, @NonNull final HttpCallback<OnlineMusicList> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_GET_MUSIC_LIST)
                .addParams(PARAM_TYPE, type)
                .addParams(PARAM_SIZE, String.valueOf(size))
                .addParams(PARAM_OFFSET, String.valueOf(offset))
                .build()
                .execute(new JsonCallback<OnlineMusicList>(OnlineMusicList.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getSongListInfo onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(OnlineMusicList response, int id) {
                        Log.i(TAG, "getSongListInfo onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getSongListInfo onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void getMusicDownloadInfo(String songId, @NonNull final HttpCallback<DownloadInfo> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_DOWNLOAD_MUSIC)
                .addParams(PARAM_SONG_ID, songId)
                .build()
                .execute(new JsonCallback<DownloadInfo>(DownloadInfo.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getMusicDownloadInfo onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(DownloadInfo response, int id) {
                        Log.i(TAG, "getMusicDownloadInfo onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getMusicDownloadInfo onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void getBitmap(String url, @NonNull final HttpCallback<Bitmap> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getBitmap onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        Log.i(TAG, "getBitmap onSuccess: " + id);
                        callback.onSuccess(bitmap);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getBitmap onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void getLrc(String songId, @NonNull final HttpCallback<Lrc> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_LRC)
                .addParams(PARAM_SONG_ID, songId)
                .build()
                .execute(new JsonCallback<Lrc>(Lrc.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getLrc onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(Lrc response, int id) {
                        Log.i(TAG, "getLrc onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getLrc onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void searchMusic(String keyword, @NonNull final HttpCallback<SearchMusic> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_SEARCH_MUSIC)
                .addParams(PARAM_QUERY, keyword)
                .build()
                .execute(new JsonCallback<SearchMusic>(SearchMusic.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "searchMusic onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(SearchMusic response, int id) {
                        Log.i(TAG, "searchMusic onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "searchMusic onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }

    public static void getArtistInfo(String tingUid, @NonNull final HttpCallback<ArtistInfo> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_ARTIST_INFO)
                .addParams(PARAM_TING_UID, tingUid)
                .build()
                .execute(new JsonCallback<ArtistInfo>(ArtistInfo.class) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "getArtistInfo onError: " + e.getMessage());
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(ArtistInfo response, int id) {
                        Log.i(TAG, "getArtistInfo onSuccess: " + id);
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onAfter(int id) {
                        Log.d(TAG, "getArtistInfo onFinish: " + id);
                        callback.onFinish();
                    }
                });
    }
}
