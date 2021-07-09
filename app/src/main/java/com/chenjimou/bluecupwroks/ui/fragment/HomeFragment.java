package com.chenjimou.bluecupwroks.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chenjimou.bluecupwroks.databinding.FragmentHomeBinding;
import com.chenjimou.bluecupwroks.jetpack.MainActivityViewModel;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.myInterface.Pictures;
import com.chenjimou.bluecupwroks.ui.adapter.HomeFragmentRecyclerViewAdapter;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.widget.MainItemDecoration;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

import org.jetbrains.annotations.NotNull;
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

public class HomeFragment extends Fragment
{
    FragmentHomeBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    HomeFragmentRecyclerViewAdapter mAdapter;
    MainActivityViewModel mViewModel;
    StaggeredGridLayoutManager mLayoutManager;
    ProgressDialog mDialog;

    Disposable disposable;
    Retrofit retrofit;

    int position = 0; // 当前加载到哪一张图片
    int limit = 10; // 初始请求的图片数量，默认为10

    private static final String TAG = "HomeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBinding = FragmentHomeBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        init();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!loadFromModel())
        {
            loadFromInternet();
        }
    }

    private void loadFromInternet()
    {
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        Pictures pictures = retrofit.create(Pictures.class);
        pictures.getPictures(1, limit)
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<PictureBean>, ObservableSource<PictureBean>>()
                {
                    @Override
                    public ObservableSource<PictureBean> apply(@NotNull List<PictureBean> pictureBeans) throws Exception
                    {
                        dataOnUI.addAll(pictureBeans);
                        return Observable.fromIterable(pictureBeans);
                    }
                })
                .map(new Function<PictureBean, PictureBean>()
                {
                    @Override
                    public PictureBean apply(@NotNull PictureBean pictureBean) throws Exception
                    {
                        List<PictureBean> pictureBeans = LitePal
                                .where("picture_id = ?", pictureBean.getPicture_id())
                                .find(PictureBean.class);
                        pictureBean.setCollection(pictureBeans.size() > 0);
                        return pictureBean;
                    }
                })
                .observeOn(Schedulers.newThread())
                .flatMap(new Function<PictureBean, ObservableSource<ResponseBody>>()
                {
                    @Override
                    public ObservableSource<ResponseBody> apply(@NotNull PictureBean pictureBean) throws Exception
                    {
                        String[] strings = pictureBean.getDownload_url().split("/id/");
                        return pictures.getPicture(strings[1]);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
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
                        mDialog.dismiss();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        mDialog.dismiss();
                        if(dataOnUI.size() > 0)
                        {
                            mLayoutManager.setSpanCount(2);
                        }
                        mAdapter.notifyItemRangeChanged(0,10);
                        /* 保存数据到 ViewModel */
                        mViewModel.setHomeList(dataOnUI);
                    }
                });
    }

    /**
     * 从 ViewModel 中读取数据
     */
    boolean loadFromModel()
    {
        boolean result;
        List<PictureBean> dataFromModel = mViewModel.getHomeList().getValue();
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
        /* 初始化 RecyclerView */

        // 创建 LayoutManager
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
//        // 设置布局间隙策略，防止瀑布流中图片错乱
//        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        // 设置 LayoutManager
        mBinding.rvHome.setLayoutManager(mLayoutManager);
        // 设置 ItemDecoration
        mBinding.rvHome.addItemDecoration(new MainItemDecoration());
        // 创建适配器
        mAdapter = new HomeFragmentRecyclerViewAdapter(getActivity(), dataOnUI);
        // 设置适配器
        mBinding.rvHome.setAdapter(mAdapter);

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

        // 初始化 ViewModel
        mViewModel = ((MainActivity)getActivity()).getModel();

        /* 初始化 SmartRefreshLayout */

        // 禁用下拉刷新
        mBinding.srlHome.setEnableRefresh(false);
        mBinding.srlHome.setRefreshFooter(new ClassicsFooter(getContext()));
        mBinding.srlHome.setOnLoadMoreListener(new OnLoadMoreListener()
        {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout)
            {
                loadMore();
            }
        });

        /* 初始化 Dialog */
        mDialog = new ProgressDialog(getActivity());
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

    void loadMore()
    {
        /* 每次新获取10张图片 */
        limit += 10;
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        Pictures pictures = retrofit.create(Pictures.class);
        pictures.getPictures(1, limit)
                .subscribeOn(Schedulers.io())// 事件产生的线程，IO异步
                .observeOn(Schedulers.io())// 事件消费的线程，IO异步
                .flatMap(new Function<List<PictureBean>, ObservableSource<PictureBean>>()
                {
                    @Override
                    public ObservableSource<PictureBean> apply(@NotNull List<PictureBean> pictureBeans) throws Exception
                    {
                        List<PictureBean> list1 = pictureBeans.subList(position,position + 10);
                        dataOnUI.addAll(list1);
                        return Observable.fromIterable(list1);
                    }
                })
                .map(new Function<PictureBean, PictureBean>()
                {
                    @Override
                    public PictureBean apply(@NotNull PictureBean pictureBean) throws Exception
                    {
                        List<PictureBean> pictureBeans = LitePal
                                .where("picture_id = ?", pictureBean.getPicture_id())
                                .find(PictureBean.class);
                        pictureBean.setCollection(pictureBeans.size() > 0);
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
                .observeOn(AndroidSchedulers.mainThread()) // 事件消费的线程，切换主线程
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
                        mBinding.srlHome.finishLoadMore();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        mBinding.srlHome.finishLoadMore();
                        if(dataOnUI.size() > 0)
                        {
                            mLayoutManager.setSpanCount(2);
                        }
                        mAdapter.notifyItemRangeChanged(position - 10,10);
                        /* 保存数据到 ViewModel */
                        mViewModel.setHomeList(dataOnUI);
                    }
                });
    }
}
