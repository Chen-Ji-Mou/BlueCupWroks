package com.chenjimou.bluecupwroks.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.ui.activity.PictureDetailsActivity;

import org.litepal.LitePal;

import java.util.List;

public class GalleryFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PictureBean> list;
    private int itemViewType;
    private final int
            NO_DATA = 0, //无数据
            NORMAL_VIEW = 1; //正常布局

    public GalleryFragmentRecyclerViewAdapter(Context context, List<PictureBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position){
        if(list.size() <= 0){
            return NO_DATA;//无数据处理
        }
        return NORMAL_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder = null;
        switch(viewType){
            case NO_DATA:
                holder = new ViewHolder(layoutInflater.inflate(R.layout.item_no_data, parent,false));
                break;
            case NORMAL_VIEW:
                holder = new ViewHolder(layoutInflater.inflate(R.layout.recycler_view_item, parent,false));
                break;
        }
        assert holder != null;
        return holder;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "CheckResult"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(list.size() <= 0){
            return;//无数据的情况
        }
        if (itemViewType == NORMAL_VIEW) {
            PictureBean pictureBean = list.get(position);
            ViewHolder viewHolder = (ViewHolder) holder;

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            assert windowManager != null;
            int width = windowManager.getDefaultDisplay().getWidth();
            int imageWidth = (width-10)/2;

            Glide.with(context)
                    .load(pictureBean.getPicture_data())
                    .override(imageWidth, (imageWidth * pictureBean.getHeight() / pictureBean.getWidth()))
                    .into(viewHolder.imageView);

            viewHolder.collectionView.setVisibility(View.GONE);

            if (pictureBean.isCollection()){
                viewHolder.collectionView.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            }else {
                viewHolder.collectionView.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
            }

            viewHolder.collectionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        int result;
        if (list.size() > 0){
            result = list.size();
            itemViewType = NORMAL_VIEW;
        }else {
            result = 1;
            itemViewType = NO_DATA;
        }
        return result;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageButton collectionView;

        @SuppressLint("CheckResult")
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemViewType == NORMAL_VIEW){
                imageView = itemView.findViewById(R.id.imageView_HomeFragmentListItem);
                collectionView = itemView.findViewById(R.id.collectionView_HomeFragmentListItem);
                CardView cardView = itemView.findViewById(R.id.cardView_HomeFragmentListItem);

                cardView.setOnClickListener(new View.OnClickListener() {
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

                collectionView.setOnClickListener(new View.OnClickListener() {
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
                        ((MainActivity)context).getModel().setGalleryList(list);
                        List<PictureBean> viewModel_data = ((MainActivity)context).getModel().getHomeList().getValue();
                        assert viewModel_data != null;
                        for (PictureBean p1:viewModel_data) {
                            for (PictureBean p2:list) {
                                if (p2.getPicture_id().equals(p1.getPicture_id())){
                                    p1.setCollection(p2.isCollection());
                                }
                            }
                        }
                        ((MainActivity)context).getModel().setHomeList(viewModel_data);
                        list.remove(pictureBean);
                    }
                });
            }
        }
    }
}
