package com.chenjimou.bluecupwroks.utils;

import android.content.Context;

public class DisplayUtils
{
    public static int dip2px(Context context, float dipValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}
