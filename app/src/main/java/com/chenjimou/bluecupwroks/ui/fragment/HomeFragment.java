package com.chenjimou.bluecupwroks.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.jetpack.MainActivityViewModel;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.myInterface.Pictures;
import com.chenjimou.bluecupwroks.ui.adapter.HomeFragmentRecyclerViewAdapter;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.utils.RecyclerViewScrollControlUtil;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements RecyclerViewScrollControlUtil.OnRecycleRefreshListener {

    private List<PictureBean> list = new ArrayList<>();
    private HomeFragmentRecyclerViewAdapter recyclerViewAdapter;
    private MainActivityViewModel model;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ProgressDialog progressDialog;
    private Disposable disposable;
    private Pictures pictures;
    private int position = 0;
    private RecyclerViewScrollControlUtil mController;
    private Retrofit retrofit;
    private int limit = 10;// 初始请求的图片数量，默认为10

    private static final String TAG = "HomeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_HomeFragment);

        mController = new RecyclerViewScrollControlUtil(this);// 创建控制器，同时使当前 activity 实现数据监听回调接口

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);// 防止瀑布流中图片错乱排序
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerViewAdapter = new HomeFragmentRecyclerViewAdapter(getActivity(), list);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnScrollListener(mController);// 将控制器绑定 RecyclerView

        /* 从 ViewModel 中读取数据 */
        if (isAdded()) {
            model = ((MainActivity)requireActivity()).getModel();
            List<PictureBean> list1 = model.getHomeList().getValue();
            if (list1 != null && list1.size() > 0) {
                list.addAll(list1);
                if(list.size() > 0){
                    staggeredGridLayoutManager.setSpanCount(2);
                }
                position = list.size();
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }

        /* 初始化 Retrofit */
        retrofit = new Retrofit.Builder()
                .baseUrl("https://picsum.photos/")
                .client(new OkHttpClient.Builder()
                        .callTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        /* 若 ViewModel 没有缓存数据则进行网络请求 */
        if (list.size() == 0) {
            /* 对请求返回的数据使用 RxJava 进行进一步处理 */
            pictures = retrofit.create(Pictures.class);
            pictures.getPictures(1, limit)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Function<List<PictureBean>, ObservableSource<PictureBean>>() {
                        @Override
                        public ObservableSource<PictureBean> apply(List<PictureBean> pictureBeans) throws Exception {
                            list.addAll(pictureBeans);
//                            Log.d(TAG, "6666  fromIterable: "+Thread.currentThread().getId());
                            return Observable.fromIterable(pictureBeans);
                        }
                    })
                    .map(new Function<PictureBean, PictureBean>() {
                        @Override
                        public PictureBean apply(PictureBean pictureBean) throws Exception {
                            List<PictureBean> pictureBeans = LitePal.where("picture_id = ?", pictureBean.getPicture_id()).find(PictureBean.class);
                            if (pictureBeans.size() > 0){
                                pictureBean.setCollection(true);
                            }else {
                                pictureBean.setCollection(false);
                            }
                            return pictureBean;
                        }
                    })
                    .observeOn(Schedulers.newThread())
                    .flatMap(new Function<PictureBean, ObservableSource<ResponseBody>>() {
                        @Override
                        public ObservableSource<ResponseBody> apply(PictureBean pictureBean) throws Exception {
                            String[] strings = pictureBean.getDownload_url().split("/id/");
//                            Log.d(TAG, "6666  getPicture: "+Thread.currentThread().getId());
                            return pictures.getPicture(strings[1]);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Function<ResponseBody, byte[]>() {
                        @Override
                        public byte[] apply(ResponseBody responseBody) throws Exception {
                            return responseBody.bytes();
                        }
                    })
                    .subscribe(new Observer<byte[]>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            progressDialog = new ProgressDialog(requireActivity());
                            progressDialog.setTitle("图片加载中，请稍后...");
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            disposable = d;
                        }

                        @Override
                        public void onNext(byte[] bytes) {
                            if (bytes != null){
                                list.get(position).setPicture_data(bytes);
                                position++;
//                                Log.d(TAG, "6666  position: "+position);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "6666  onError: "+e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            if(list.size() > 0){
                                staggeredGridLayoutManager.setSpanCount(2);
                            }
                            recyclerViewAdapter.notifyItemRangeChanged(0,10);
                            /* 保存数据到 ViewModel */
                            model.setHomeList(list);
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    @Override
    public void loadMore() {
        /* 每次新获取10张图片 */
        limit = limit + 10;
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        pictures = retrofit.create(Pictures.class);
        pictures.getPictures(1, limit)
                .subscribeOn(Schedulers.io())// 事件产生的线程，IO异步
                .observeOn(Schedulers.io())// 事件消费的线程，IO异步
                .flatMap(new Function<List<PictureBean>, ObservableSource<PictureBean>>() {
                    @Override
                    public ObservableSource<PictureBean> apply(List<PictureBean> pictureBeans) throws Exception {
                        List<PictureBean> list1 = pictureBeans.subList(position,position + 10);
                        list.addAll(list1);
                        return Observable.fromIterable(list1);
                    }
                })
                .map(new Function<PictureBean, PictureBean>() {
                    @Override
                    public PictureBean apply(PictureBean pictureBean) throws Exception {
                        List<PictureBean> pictureBeans = LitePal.where("picture_id = ?", pictureBean.getPicture_id()).find(PictureBean.class);
                        if (pictureBeans.size() > 0){
                            pictureBean.setCollection(true);
                        }else {
                            pictureBean.setCollection(false);
                        }
                        return pictureBean;
                    }
                })
                .flatMap(new Function<PictureBean, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(PictureBean pictureBean) throws Exception {
                        String[] strings = pictureBean.getDownload_url().split("/id/");
                        return pictures.getPicture(strings[1]);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())// 事件消费的线程，切换主线程
                .map(new Function<ResponseBody, byte[]>() {
                    @Override
                    public byte[] apply(ResponseBody responseBody) throws Exception {
                        return responseBody.bytes();
                    }
                })
                .subscribe(new Observer<byte[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        progressDialog = new ProgressDialog(requireActivity());
                        progressDialog.setTitle("图片加载中，请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        disposable = d;
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        if (bytes != null){
                            list.get(position).setPicture_data(bytes);
                            position++;
//                            Log.d(TAG, "6666  position: "+position);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        mController.setLoadDataStatus(false);
                        Log.d(TAG, "6666  onError: "+e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        if(list.size() > 0){
                            staggeredGridLayoutManager.setSpanCount(2);
                        }
                        recyclerViewAdapter.notifyItemRangeChanged(position - 10,10);
                        mController.setLoadDataStatus(false);
                        /* 保存数据到 ViewModel */
                        model.setHomeList(list);
                    }
                });
    }
}
