package com.chenjimou.bluecupwroks.utils;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

public class DisplayUtils
{
    public static int dip2px(Context context, float dipValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int getScreenWidth(Activity activity)
    {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }
}
