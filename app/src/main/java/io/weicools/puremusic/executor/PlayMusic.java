package io.weicools.puremusic.executor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import io.weicools.puremusic.R;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.util.NetworkUtil;
import io.weicools.puremusic.util.Preferences;

/**
 * Author: weicools
 * Time: 2017/11/22 上午11:49
 */

public abstract class PlayMusic implements IExecutor<Music> {
    private Activity mActivity;
    protected Music mMusic;
    private int mTotalStep;
    protected int mCounter;

    public PlayMusic(Activity activity, int totalStep) {
        mActivity = activity;
        mTotalStep = totalStep;
    }

    @Override
    public void execute() {
        checkNetwork();
    }

    private void checkNetwork() {
        boolean mobileNetworkPlay = Preferences.enableMobileNetworkPlay();
        if (!NetworkUtil.isActiveNetworkMobile(mActivity) && !mobileNetworkPlay) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.play_tips);
            builder.setPositiveButton(R.string.play_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Preferences.saveMobileNetworkPlay(true);
                    getPlayInfoWrapper();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            getPlayInfoWrapper();
        }
    }

    private void getPlayInfoWrapper() {
        onPrepare();
        getPlayInfo();
    }

    protected abstract void getPlayInfo();

    protected void checkCounter() {
        mCounter++;
        if (mCounter == mTotalStep) {
            onExecuteSuccess(mMusic);
        }
    }
}
