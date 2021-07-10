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
import com.chenjimou.bluecupwroks.databinding.FragmentGalleryBinding;
import com.chenjimou.bluecupwroks.jetpack.MainActivityViewModel;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.myInterface.Pictures;
import com.chenjimou.bluecupwroks.ui.adapter.GalleryAdapter;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.widget.MainItemDecoration;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
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

public class GalleryFragment extends Fragment
{
    FragmentGalleryBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    MainActivityViewModel mViewModel;
    StaggeredGridLayoutManager mLayoutManager;
    GalleryAdapter mAdapter;
    ProgressDialog mDialog;

    Disposable disposable;
    Retrofit retrofit;

    volatile int position = 0;

    private static final String TAG = "GalleryFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBinding = FragmentGalleryBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        init();
        if (!loadFromModel())
        {
            loadFromInternet();
        }
    }

    void loadFromInternet()
    {
        Pictures pictures = retrofit.create(Pictures.class);
        Observable.create(new ObservableOnSubscribe<List<PictureBean>>()
        {
            @Override
            public void subscribe(@NotNull ObservableEmitter<List<PictureBean>> emitter) throws Exception
            {
                List<PictureBean> database_data = LitePal.findAll(PictureBean.class);
                dataOnUI.addAll(database_data);
                emitter.onNext(dataOnUI);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())// 事件产生的线程，IO异步
                .observeOn(Schedulers.io())// 事件消费的线程，IO异步
                .flatMap(new Function<List<PictureBean>, ObservableSource<PictureBean>>()
                {
                    @Override
                    public ObservableSource<PictureBean> apply(@NotNull List<PictureBean> pictureBeans) throws Exception
                    {
                        return Observable.fromIterable(pictureBeans);
                    }
                })
                .map(new Function<PictureBean, PictureBean>()
                {
                    @Override
                    public PictureBean apply(@NotNull PictureBean pictureBean) throws Exception
                    {
                        pictureBean.setCollection(true);
                        return pictureBean;
                    }
                })
                .flatMap(new Function<PictureBean, ObservableSource<ResponseBody>>()
                {
                    @Override
                    public ObservableSource<ResponseBody> apply(@NotNull PictureBean pictureBean) throws Exception
                    {
                        String[] strings = pictureBean.getDownload_url().split("/id/");
                        return pictures.getPicture(strings[1]);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())// 事件消费的线程，切换主线程
                .map(new Function<ResponseBody, byte[]>()
                {
                    @Override
                    public byte[] apply(@NotNull ResponseBody responseBody) throws Exception
                    {
                        return responseBody.bytes();
                    }
                })
                .subscribe(new Observer<byte[]>()
                {
                    @Override
                    public void onSubscribe(@NotNull Disposable d)
                    {
                        mDialog.show();

                        disposable = d;
                    }

                    @Override
                    public void onNext(@NotNull byte[] bytes)
                    {
                        if (bytes != null)
                        {
                            dataOnUI.get(position).setPicture_data(bytes);
                            position++;
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e)
                    {
                        if (mDialog != null)
                        {
                            mDialog.dismiss();
                        }
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        if(dataOnUI.size() > 0){
                            mLayoutManager.setSpanCount(2);
                        }
                        mAdapter.notifyDataSetChanged();
                        /* 保存数据到 ViewModel */
                        mViewModel.setGalleryList(dataOnUI);
                    }
                });
    }

    boolean loadFromModel()
    {
        boolean result;
        List<PictureBean> dataFromModel = mViewModel.getGalleryList().getValue();
        if (dataFromModel != null && dataFromModel.size() > 0)
        {
            dataOnUI.addAll(dataFromModel);
            if(dataOnUI.size() > 0)
            {
                mLayoutManager.setSpanCount(2);
            }
            position = dataOnUI.size();
            mAdapter.notifyDataSetChanged();
            result = true;
        }
        else
        {
            result = false;
        }
        return result;
    }

    void init()
    {
        mViewModel = ((MainActivity) getContext()).getModel();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://picsum.photos/")
                .client(new OkHttpClient.Builder()
                        .callTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mBinding.rvGallery.setLayoutManager(mLayoutManager);
        mBinding.rvGallery.addItemDecoration(new MainItemDecoration());
        mAdapter = new GalleryAdapter(getActivity(), dataOnUI);
        mBinding.rvGallery.setAdapter(mAdapter);

        mDialog = new ProgressDialog(getContext());
        mDialog.setTitle("图片加载中，请稍后...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }
}
