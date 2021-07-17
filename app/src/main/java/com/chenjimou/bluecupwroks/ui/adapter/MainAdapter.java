package com.chenjimou.bluecupwroks.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.chenjimou.bluecupwroks.Constants;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.RecyclerViewItemBinding;
import com.chenjimou.bluecupwroks.jetpack.room.PictureDatabase;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.ui.activity.PictureDetailsActivity;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>
{
    RecyclerViewItemBinding mBinding;

    final Context context;
    final List<PictureBean> data;

    private static final String TAG = "MainAdapter";

    public MainAdapter(Context context, List<PictureBean> data)
    {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        mBinding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if (data.get(position).isCollection())
        {
            holder.ibCollection.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
        }
        else
        {
            holder.ibCollection.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
        }

        int screenWidth = DisplayUtils.getScreenWidth((Activity)context);
        int imageWidth = (screenWidth - DisplayUtils.dip2px(context, 24)) / 2;
        int imageHeight = (int) (imageWidth * (1.0f * data.get(position).getHeight() / data.get(position).getWidth()));

        StaggeredGridLayoutManager.LayoutParams layoutParams =
                (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.height = imageHeight;
        holder.itemView.setLayoutParams(layoutParams);

        Glide.with(context)
                .load(data.get(position).getDownload_url())
                .placeholder(R.drawable.ic_loading)
                .override(imageWidth, imageHeight)
                .into(holder.ivPicture);
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    /**
     * ViewHolder 移出界面时的回调
     */
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder)
    {
        super.onViewRecycled(holder);
        // 清空当前显示图片
        holder.ivPicture.setImageResource(R.drawable.ic_loading);
        // 取消加载请求
        Glide.with(context).clear(holder.ivPicture);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivPicture;
        ImageButton ibCollection;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            ivPicture = mBinding.ivPicture;
            ibCollection = mBinding.ibCollection;

            mBinding.cvItem.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    PictureBean pictureBean = data.get(getAdapterPosition());
                    Intent intent = new Intent(context, PictureDetailsActivity.class);
                    intent.putExtra(Constants.PICTURE_BEAN, pictureBean);
                    context.startActivity(intent);
                }
            });

            ibCollection.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    PictureBean pictureBean = data.get(getAdapterPosition());

                    boolean isCollection = pictureBean.isCollection();

                    pictureBean.setCollection(!isCollection);

                    if (isCollection)
                    {
                        ibCollection.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                        PictureDatabase.getInstance().getPictureDao().delete(pictureBean);
                    }
                    else
                    {
                        ibCollection.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                        PictureDatabase.getInstance().getPictureDao().insert(pictureBean);
                    }

                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}
