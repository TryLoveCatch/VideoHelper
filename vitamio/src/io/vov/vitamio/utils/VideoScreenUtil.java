package io.vov.vitamio.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by bintou on 16/3/15.
 */
public class VideoScreenUtil {

    public static int getDensity(Context var0, float var1) {
        float var2 = var0.getResources().getDisplayMetrics().density;
        return (int) (var1 * var2 + 0.5F);
    }

    public static int getDisplayHeight(Context var0) {
        Display var1 = ((WindowManager) var0.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return (((Activity) var0).getWindow().getAttributes().flags & 1024) != 1024 ? var1.getHeight() - d(var0) : var1.getHeight();
    }

    private static int d(Context var0) {
        int var1 = -1;

        try {
            Class var2;
            Object var3 = (var2 = Class.forName("com.android.internal.R$dimen")).newInstance();
            int var5 = Integer.parseInt(var2.getField("status_bar_height").get(var3).toString());
            var1 = var0.getResources().getDimensionPixelSize(var5);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var1;
    }

    public static int getDisplayWidth(Context var0) {
        return ((WindowManager) var0.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

}
