package io.weicools.puremusic.executor;

import android.content.Context;
import android.content.Intent;

import io.weicools.puremusic.R;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.DownloadInfo;
import io.weicools.puremusic.util.ToastUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午7:18
 */

public abstract class ShareOnlineMusic implements IExecutor<Void> {
    private Context mContext;
    private String mSongId;
    private String mTitle;

    public ShareOnlineMusic(Context ctx, String songId, String title) {
        mContext = ctx;
        mSongId = songId;
        mTitle = title;
    }

    @Override
    public void execute() {
        onPrepare();
        share();
    }

    private void share() {
        // get music play link
        HttpClient.getMusicDownloadInfo(mSongId, new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                    onFail(null);
                    return;
                }

                onExecuteSuccess(null);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_music, mContext.getString(R.string.app_name),
                        mTitle, downloadInfo.getBitrate().getFile_link()));
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
                ToastUtil.showShort(mContext, mContext.getString(R.string.unable_to_share));
            }
        });
    }
}
