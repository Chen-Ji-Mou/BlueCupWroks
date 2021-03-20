package com.chenjimou.bluecupwroks.ui.adapter;

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

public class HomeFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PictureBean> list;
    private final int
            NORMAL_VIEW = 1,// 正常布局
            BOTTOM_VIEW = 2;// 上拉刷新布局

    private static final String TAG = "HomeFragmentRecyclerVie";

    public HomeFragmentRecyclerViewAdapter(Context context, List<PictureBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position){
//        if (position >= list.size()){
//            return BOTTOM_VIEW;
//        }
        return NORMAL_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//        if (viewType == NORMAL_VIEW){
//
//        } else {
//            return new BottomViewHolder(layoutInflater.inflate(R.layout.recycler_view_bottom_item, parent,false));
//        }
        return new NormalViewHolder(layoutInflater.inflate(R.layout.recycler_view_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NormalViewHolder) {
            PictureBean pictureBean = list.get(position);
            NormalViewHolder viewHolder = (NormalViewHolder) holder;

            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            assert windowManager != null;
            int width = windowManager.getDefaultDisplay().getWidth();
            int imageWidth = (width - 20)/2;

            Glide.with(context)
                    .load(pictureBean.getPicture_data())
                    .override(imageWidth, (imageWidth * pictureBean.getHeight() / pictureBean.getWidth()))
                    .into(viewHolder.imageView);

            if (pictureBean.isCollection()){
                viewHolder.collectionView.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            }else {
                viewHolder.collectionView.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    @Override
    public int getItemCount() {
//        if (list.size() > 0){
//            return list.size() + 1;
//        }
//        return 0;
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageButton collectionView;

        NormalViewHolder(@NonNull View itemView) {
            super(itemView);

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
    }
}
