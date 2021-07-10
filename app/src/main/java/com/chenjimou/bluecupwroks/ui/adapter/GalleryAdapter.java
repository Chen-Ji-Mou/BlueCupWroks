package com.chenjimou.bluecupwroks.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.RecyclerViewItemBinding;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.ui.activity.PictureDetailsActivity;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;

import org.litepal.LitePal;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>
{
    RecyclerViewItemBinding mBinding;

    final Context context;
    final List<PictureBean> data;
    int itemViewType;
    final int NO_DATA = 0, //无数据
            NORMAL_VIEW = 1; //正常布局

    public GalleryAdapter(Context context, List<PictureBean> data)
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
                .load(data.get(position).getPicture_data())
                .placeholder(R.drawable.ic_loading)
                .override(imageWidth, imageHeight)
                .into(holder.ivPicture);
    }

    @Override
    public int getItemCount()
    {
        return data.size();
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
                    int position = getAdapterPosition();
                    PictureBean pictureBean = data.get(position);
                    Intent intent = new Intent(context, PictureDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("picture_id", pictureBean.getPicture_id());
                    bundle.putString("author", pictureBean.getAuthor());
                    bundle.putInt("width", pictureBean.getWidth());
                    bundle.putInt("height", pictureBean.getHeight());
                    bundle.putString("download_url", pictureBean.getDownload_url());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            ibCollection.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int position = getAdapterPosition();
                    PictureBean pictureBean = data.get(position);
                    if (pictureBean.isCollection())
                    {
                        ibCollection.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                        LitePal.deleteAll(PictureBean.class,"picture_id = ?", pictureBean.getPicture_id());
                        pictureBean.setCollection(false);
                    }
                    else
                    {
                        ibCollection.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                        PictureBean pictureBean1 = new PictureBean();
                        pictureBean1.setPicture_id(data.get(position).getPicture_id());
                        pictureBean1.setAuthor(data.get(position).getAuthor());
                        pictureBean1.setWidth(data.get(position).getWidth());
                        pictureBean1.setHeight(data.get(position).getHeight());
                        pictureBean1.setUrl(data.get(position).getUrl());
                        pictureBean1.setDownload_url(data.get(position).getDownload_url());
                        pictureBean1.save();
                        pictureBean.setCollection(true);
                    }
                    ((MainActivity)context).getModel().setGalleryList(data);
                    List<PictureBean> viewModel_data = ((MainActivity)context).getModel().getHomeList().getValue();
                    assert viewModel_data != null;
                    for (PictureBean p1:viewModel_data)
                    {
                        for (PictureBean p2: data)
                        {
                            if (p2.getPicture_id().equals(p1.getPicture_id()))
                            {
                                p1.setCollection(p2.isCollection());
                            }
                        }
                    }
                    ((MainActivity)context).getModel().setHomeList(viewModel_data);
                    data.remove(pictureBean);
                }
            });
        }
    }
}
