package io.weicools.puremusic.executor;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.MimeTypeMap;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.util.FileUtil;
import io.weicools.puremusic.util.NetworkUtil;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ToastUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 上午10:44
 */

public abstract class DownloadMusic implements IExecutor<Void> {
    private Activity mActivity;

    public DownloadMusic(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void execute() {

    }

    private void checkNetwork() {
        boolean mobileNetworkDownload = Preferences.enableMobileNetworkDownload();
        if (NetworkUtil.isActiveNetworkMobile(mActivity) && !mobileNetworkDownload) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.download_tips);
            builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    downloadWrapper();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            downloadWrapper();
        }
    }

    private void downloadWrapper() {
        onPrepare();
        download();
    }

    protected abstract void download();

    protected void downloadMusic(String url, String artist, String title, String coverPath) {
        try {
            String fileName = FileUtil.getMp3FileName(artist, title);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            // request.setDescription(mActivity.getString(R.string.downloading));
            request.setDescription("正在下载……");
            request.setDestinationInExternalPublicDir(FileUtil.getRelativeMusicDir(), fileName);
            request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(false);
            DownloadManager downloadManager = (DownloadManager) AppCache.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                long id = downloadManager.enqueue(request);
                String musicAbsPath = FileUtil.getMusicDir().concat(fileName);
                DownloadMusicInfo musicInfo = new DownloadMusicInfo(title, musicAbsPath, coverPath);
                AppCache.getDownloadList().put(id, musicInfo);
            }
        } catch (Throwable ex) {
            Log.e("downloadMusic", "error: " + ex.getMessage());
            ToastUtil.showShort(mActivity, "下载失败");
        }
    }
}
