package com.chenjimou.bluecupwroks.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.drawable.LoadingDrawable;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.ui.activity.PictureDetailsActivity;
import com.chenjimou.bluecupwroks.utils.DisplayUtils;

import org.litepal.LitePal;

import java.util.List;

public class HomeFragmentRecyclerViewAdapter extends RecyclerView.Adapter<HomeFragmentRecyclerViewAdapter.NormalViewHolder>
{
    final Context context;
    final List<PictureBean> list;
    final RecyclerView recyclerView;

    private static final String TAG = "HomeFragmentRecyclerVie";

    public HomeFragmentRecyclerViewAdapter(Context context, List<PictureBean> list, RecyclerView recyclerView)
    {
        this.context = context;
        this.list = list;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public NormalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new NormalViewHolder(layoutInflater.inflate(R.layout.recycler_view_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NormalViewHolder holder, int position)
    {
        // 如果命中 if，说明当前 viewHolder 是空白的
        if (holder.imageView.getTag(R.id.imageView_HomeFragmentListItem) == null
                || holder.imageView.getTag(R.id.imageView_HomeFragmentListItem) == (Integer) position)
        {
            // 设置 Tag
            holder.imageView.setTag(R.id.imageView_HomeFragmentListItem, position);
            // 根据当前 position 绑定视图
            holder.bindView(position);
        }
        else // 如果命中 else，说明当前 viewHolder 是复用的
        {
            // 清除当前存在的视图状态
            holder.clearView();
            // 根据当前 position 重新绑定视图
            holder.bindView(position);
            // 重新设置 Tag
            holder.imageView.setTag(R.id.imageView_HomeFragmentListItem, position);
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        ImageButton collectionView;

        NormalViewHolder(@NonNull View itemView)
        {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_HomeFragmentListItem);
            collectionView = itemView.findViewById(R.id.collectionView_HomeFragmentListItem);
            CardView cardView = itemView.findViewById(R.id.cardView_HomeFragmentListItem);

            cardView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    PictureBean pictureBean = list.get(position);
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

            collectionView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    PictureBean pictureBean = list.get(position);
                    if (pictureBean.isCollection()){
                        collectionView.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                        LitePal.deleteAll(PictureBean.class,"picture_id = ?", pictureBean.getPicture_id());
                        pictureBean.setCollection(false);
                    }else{
                        collectionView.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                        PictureBean pictureBean1 = new PictureBean();
                        pictureBean1.setPicture_id(list.get(position).getPicture_id());
                        pictureBean1.setAuthor(list.get(position).getAuthor());
                        pictureBean1.setWidth(list.get(position).getWidth());
                        pictureBean1.setHeight(list.get(position).getHeight());
                        pictureBean1.setUrl(list.get(position).getUrl());
                        pictureBean1.setDownload_url(list.get(position).getDownload_url());
                        pictureBean1.save();
                        pictureBean.setCollection(true);
                    }
                    ((MainActivity)context).getModel().setHomeList(list);
                    List<PictureBean> database_data = LitePal.findAll(PictureBean.class);
                    for (PictureBean p1:database_data) {
                        for (PictureBean p2:list) {
                            if (p2.getPicture_id().equals(p1.getPicture_id())){
                                p1.setPicture_data(p2.getPicture_data());
                                p1.setCollection(p2.isCollection());
                            }
                        }
                    }
                    ((MainActivity)context).getModel().setGalleryList(database_data);
                }
            });
        }

        public void bindView(int position)
        {
            int screenWidth = DisplayUtils.getScreenWidth((Activity)context);
            int imageWidth = (screenWidth - DisplayUtils.dip2px(context, 24)) / 2;
            int imageHeight = (int) (imageWidth * (1.0f * list.get(position).getHeight() / list.get(position).getWidth()));

            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
            layoutParams.height = imageHeight;
            itemView.setLayoutParams(layoutParams);

            Glide.with(context)
                    .load(list.get(position).getPicture_data())
                    .override(imageWidth, imageHeight)
                    .placeholder(R.drawable.ic_loading)
                    .into(imageView);

            if (list.get(position).isCollection())
            {
                collectionView.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            }
            else
            {
                collectionView.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }

        public void clearView()
        {
            imageView.setImageDrawable(null);
            collectionView.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }
}
