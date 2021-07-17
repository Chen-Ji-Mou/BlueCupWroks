package com.chenjimou.bluecupwroks.widget;

import android.graphics.Rect;
import android.view.View;

import com.chenjimou.bluecupwroks.base.BaseApplication;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class CustomItemDecoration extends RecyclerView.ItemDecoration
{
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
            @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
        int spanIndex = layoutParams.getSpanIndex();
        int position = parent.getChildAdapterPosition(view);
        int divider = DisplayUtils.dip2px(BaseApplication.sApplication,8);

        outRect.bottom = divider;
        if (position == 0 || position == 1)
        {
            outRect.top = divider;
        }
        else
        {
            outRect.top = 0;
        }

        // 偶数项
        if (spanIndex % 2 == 0)
        {
            outRect.left = divider;
            outRect.right = divider / 2;
        }
        else
        {
            outRect.left = divider / 2;
            outRect.right = divider;
        }
    }
}
