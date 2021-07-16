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

import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.FragmentHomeBinding;
import com.chenjimou.bluecupwroks.jetpack.viewmodel.MainActivityViewModel;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.inter.RetrofitRequest;
import com.chenjimou.bluecupwroks.ui.adapter.HomeAdapter;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.widget.MainItemDecoration;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment
{
    FragmentHomeBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    HomeAdapter mAdapter;
    MainActivityViewModel mViewModel;
    ProgressDialog mDialog;

    Disposable disposable;
    Retrofit retrofit;

    int currentPage = 1;

    int lastLoadPosition = 0;

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
        if (!loadFromModel())
        {
            loadFromInternet();
        }
    }

    void init()
    {
        /* 初始化 RecyclerView */

        // 设置 LayoutManager
        mBinding.rvHome.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        // 设置 ItemDecoration
        mBinding.rvHome.addItemDecoration(new MainItemDecoration());
        // 创建适配器
        mAdapter = new HomeAdapter(getActivity(), dataOnUI);
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
                mBinding.rvHome.setVisibility(View.VISIBLE);
                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
            }
            else
            {
                mBinding.rvHome.setVisibility(View.GONE);
                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
            }

            lastLoadPosition = dataOnUI.size();

            mAdapter.notifyDataSetChanged();

            result = true;
        }
        else
        {
            result = false;
        }
        return result;
    }

    void loadFromInternet()
    {
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        RetrofitRequest retrofitRequest = retrofit.create(RetrofitRequest.class);
        retrofitRequest.loadPictures(currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PictureBean>>()
                {
                    @Override
                    public void onSubscribe(@NotNull Disposable d)
                    {
                        mDialog.show();
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NotNull List<PictureBean> dataFromInternet)
                    {
                        dataOnUI.addAll(dataFromInternet);
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
                            mBinding.rvHome.setVisibility(View.VISIBLE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
                        }
                        else
                        {
                            mBinding.rvHome.setVisibility(View.GONE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
                        }

                        mAdapter.notifyDataSetChanged();

                        lastLoadPosition = dataOnUI.size();

                        mViewModel.setHomeList(dataOnUI);
                    }
                });
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
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        RetrofitRequest retrofitRequest = retrofit.create(RetrofitRequest.class);
        retrofitRequest.loadPictures(++currentPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PictureBean>>()
                {
                    @Override
                    public void onSubscribe(@NotNull Disposable d)
                    {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NotNull List<PictureBean> dataFromInternet)
                    {
                        dataOnUI.addAll(dataFromInternet);
                    }

                    @Override
                    public void onError(@NotNull Throwable e)
                    {
                        if (mBinding.srlHome.isLoading())
                            mBinding.srlHome.finishLoadMore();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        if (mBinding.srlHome.isLoading())
                            mBinding.srlHome.finishLoadMore();

                        if(dataOnUI.size() > 0)
                        {
                            mBinding.rvHome.setVisibility(View.VISIBLE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
                        }
                        else
                        {
                            mBinding.rvHome.setVisibility(View.GONE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
                        }

                        mAdapter.notifyItemInserted(lastLoadPosition);

                        lastLoadPosition = dataOnUI.size();

                        mViewModel.setHomeList(dataOnUI);
                    }
                });
    }
}
