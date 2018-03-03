package io.weicools.puremusic.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Author: weicools
 * Time: 2017/10/30 下午4:59
 */

public class ToastUtil {
    public static void showShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private static Context sContext;
    private static Toast sToast;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void showShort(int resId) {
        showShort(sContext.getString(resId));
    }

    public static void showShort(String text) {
        if (sToast == null) {
            sToast = Toast.makeText(sContext, text, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(text);
        }
        sToast.show();
    }
}
