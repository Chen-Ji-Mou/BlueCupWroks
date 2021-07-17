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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chenjimou.bluecupwroks.Constants;
import com.chenjimou.bluecupwroks.R;
import com.chenjimou.bluecupwroks.databinding.FragmentHomeBinding;
import com.chenjimou.bluecupwroks.jetpack.room.PictureDatabase;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.inter.RetrofitRequest;
import com.chenjimou.bluecupwroks.ui.adapter.MainAdapter;
import com.chenjimou.bluecupwroks.widget.CustomItemDecoration;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;

import org.jetbrains.annotations.NotNull;


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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainFragment extends Fragment
{
    FragmentHomeBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    MainAdapter mAdapter;
    ProgressDialog mDialog;

    Disposable disposable;
    Retrofit retrofit;

    int currentPage = 1;

    int lastLoadPosition = 0;

    boolean alreadyLoad = false;
    boolean isError = false;

    private static final String TAG = "MainFragment";

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

    void init()
    {
        /* 初始化 RecyclerView */

        // 设置 LayoutManager
        mBinding.rvHome.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        // 设置 ItemDecoration
        mBinding.rvHome.addItemDecoration(new CustomItemDecoration());
        // 创建适配器
        mAdapter = new MainAdapter(getActivity(), dataOnUI);
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
    public void onStart()
    {
        super.onStart();
        if (!alreadyLoad)
            loadFromInternet();
        if (Constants.IS_DATABASE_CHANGE)
            loadFromDatabase();
    }

    void loadFromInternet()
    {
        /* 对请求返回的数据使用 RxJava 进行进一步处理 */
        RetrofitRequest retrofitRequest = retrofit.create(RetrofitRequest.class);
        retrofitRequest.loadPictures(currentPage)
                .map(new Function<List<PictureBean>, List<PictureBean>>()
                {
                    @Override
                    public List<PictureBean> apply(
                            @io.reactivex.annotations.NonNull
                                    List<PictureBean> dataFromInternet) throws Exception
                    {
                        List<PictureBean> dataFromDatabase = PictureDatabase.getInstance().getPictureDao().findAll();
                        for (int i = 0; i < dataFromInternet.size(); i++)
                        {
                            dataFromInternet.get(i).setCollection(false);
                            if (!dataFromDatabase.isEmpty())
                                for (PictureBean bean : dataFromDatabase)
                                    dataFromInternet.set(i, bean);
                        }
                        return dataFromInternet;
                    }
                })
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
                        isError = false;
                    }

                    @Override
                    public void onError(@NotNull Throwable e)
                    {
                        mDialog.dismiss();
                        isError = true;
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        mDialog.dismiss();

                        if (!isError)
                        {
                            if(!dataOnUI.isEmpty())
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

                            alreadyLoad = true;
                        }
                    }
                });
    }

    void loadFromDatabase()
    {
        List<PictureBean> dataFromDatabase = PictureDatabase.getInstance().getPictureDao().findAll();
        for (int i = 0; i < dataOnUI.size(); i++)
        {
            dataOnUI.get(i).setCollection(false);
            if (!dataFromDatabase.isEmpty())
                for (PictureBean bean : dataFromDatabase)
                    dataOnUI.set(i, bean);
        }
        mAdapter.notifyDataSetChanged();
        Constants.IS_DATABASE_CHANGE = false;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (disposable != null && !disposable.isDisposed())
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
                        isError = false;
                    }

                    @Override
                    public void onError(@NotNull Throwable e)
                    {
                        if (mBinding.srlHome.isLoading())
                            mBinding.srlHome.finishLoadMore();
                        isError = true;
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete()
                    {
                        if (mBinding.srlHome.isLoading())
                            mBinding.srlHome.finishLoadMore();

                        if (!isError)
                        {
                            if(!dataOnUI.isEmpty())
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
                        }
                    }
                });
    }
}
