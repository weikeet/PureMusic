package io.weicools.puremusic.executor;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import io.weicools.puremusic.R;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.service.QuitTimer;
import io.weicools.puremusic.ui.activity.MainActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ToastUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午7:25
 */

public class NavMenuExecutor {
    private MainActivity activity;

    public NavMenuExecutor(MainActivity activity) {
        this.activity = activity;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                // TODO: 2018/3/4 strat setting
//                startActivity(SettingActivity.class);
                return true;
            case R.id.action_night:
                nightMode();
                break;
            case R.id.action_timer:
                timerDialog();
                return true;
            case R.id.action_exit:
                activity.finish();
                MusicService.startCommand(activity, ConstantUtil.ACTION_STOP);
                return true;
            case R.id.action_about:
                // TODO: 2018/3/4 start about
//                startActivity(AboutActivity.class);
                return true;
        }
        return false;
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
    }

    private void nightMode() {
        Preferences.saveNightMode(!Preferences.isNightMode());
        activity.recreate();
    }

    private void timerDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), (dialog, which) -> {
                    int[] times = activity.getResources().getIntArray(R.array.timer_int);
                    startTimer(times[which]);
                })
                .show();
    }

    private void startTimer(int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtil.showShort(activity.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtil.showShort(R.string.timer_cancel);
        }
    }
}
