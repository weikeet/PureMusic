package io.weicools.puremusic.executor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.service.QuitTimer;
import io.weicools.puremusic.ui.activity.MainActivity;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.ToastUtil;

/**
 * Author: weicools
 * Time: 2017/11/22 下午7:25
 */

public class NavMenuExecutor {
    public static boolean onNavigationItemSelected(MenuItem item, MainActivity activity) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                // startActivity(activity, SettingActivity.class);
                return true;
            case R.id.action_night:
                nightMode(activity);
                break;
            case R.id.action_timer:
                timerDialog(activity);
                return true;
            case R.id.action_exit:
                exit(activity);
                return true;
            case R.id.action_about:
                // startActivity(activity, AboutActivity.class);
                return true;
        }
        return false;
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    private static void nightMode(final MainActivity activity) {
        Preferences.saveNightMode(!Preferences.isNightMode());
        activity.recreate();
    }

    private static void timerDialog(final MainActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] times = activity.getResources().getIntArray(R.array.timer_int);
                        startTimer(activity, times[which]);
                    }
                })
                .show();
    }

    private static void startTimer(Context context, int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtil.showShort(context, context.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtil.showShort(context, context.getString(R.string.timer_cancel));
        }
    }

    private static void exit(MainActivity activity) {
        activity.finish();
        MusicService service = AppCache.getPlayService();
        if (service != null) {
            service.quit();
        }
    }
}
