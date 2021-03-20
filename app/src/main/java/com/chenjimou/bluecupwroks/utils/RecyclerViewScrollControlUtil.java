package com.chenjimou.bluecupwroks.utils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chenjimou.bluecupwroks.myEnum.LayoutManagerType;

public class RecyclerViewScrollControlUtil extends RecyclerView.OnScrollListener {

    private LayoutManagerType mLayoutManagerType;// 当前布局管理器的类型
    private int mLastVisibleItemPosition;// 当前 RecycleView 显示的最大条目
    private int[] mLastPostions;// 每列的最后一个条目
    private boolean isLoadData = false;// 是否正在加载数据，包括刷新和向上加载更多
    private OnRecycleRefreshListener mListener;// 回调接口

    public RecyclerViewScrollControlUtil(OnRecycleRefreshListener onRecycleRefreshListener) {
        this.mListener = onRecycleRefreshListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        /* 获取布局参数 */
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        /* 如果为null，第一次运行，确定布局类型 */
        if (mLayoutManagerType == null) {
            if (layoutManager instanceof LinearLayoutManager) {
                mLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT;
            } else if (layoutManager instanceof GridLayoutManager) {
                mLayoutManagerType = LayoutManagerType.GRID_LAYOUT;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                mLayoutManagerType = LayoutManagerType.STAGGERED_GRID_LAYOUT;
            } else {
                throw new RuntimeException("LayoutManager should be LinearLayoutManager,GridLayoutManager,StaggeredGridLayoutManager");
            }
        }

        /* 对于不太能够的布局参数，不同的方法获取到当前显示的最后一个条目数 */
        switch (mLayoutManagerType) {
            case LINEAR_LAYOUT:
                mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case GRID_LAYOUT:
                mLastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case STAGGERED_GRID_LAYOUT:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (mLastPostions == null) {
                    mLastPostions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(mLastPostions);
                mLastVisibleItemPosition = findMax(mLastPostions);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager)layoutManager).invalidateSpanAssignments(); //防止第一行到顶部有空白区域
        }

        /* RecycleView 显示的条目数 */
        int visibleCount = layoutManager.getChildCount();

        /* 显示数据总数 */
        int totalCount = layoutManager.getItemCount();

        /* 四个条件，分别是是否有数据，状态是否是滑动停止状态，显示的最大条目是否大于整个数据（注意偏移量），是否正在加载数据 */
        if(visibleCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE
                && mLastVisibleItemPosition >= totalCount - 1
                && !isLoadData){

            /* 可以加载数据 */
            if(mListener!=null){
                isLoadData = true;
                mListener.loadMore();
            }
        }
    }

    /**
     * 当是瀑布流时，获取到的是每一个瀑布最下方显示的条目，通过条目进行对比
     */
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public void setLoadDataStatus(boolean isLoadData){
        this.isLoadData = isLoadData;
    }

    /**
     * 数据加载接口回调
     */
    public interface OnRecycleRefreshListener{
        void loadMore();
    }
}
