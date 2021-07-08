package com.chenjimou.bluecupwroks.widget;

import android.graphics.Rect;
import android.view.View;

import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainItemDecoration extends RecyclerView.ItemDecoration
{
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
            @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if (position % 2 == 0)
        {
            if (position != parent.getAdapter().getItemCount() - 2)
            {
                outRect.set(
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        0);
            }
            else
            {
                outRect.set(
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8));
            }
        }
        else
        {
            if (position != parent.getAdapter().getItemCount() - 1)
            {
                outRect.set(
                        0,
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        0);
            }
            else
            {
                outRect.set(
                        0,
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8),
                        DisplayUtils.dip2px(MainActivity.sApplication,8));
            }
        }
    }
}
