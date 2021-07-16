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
import com.chenjimou.bluecupwroks.databinding.FragmentGalleryBinding;
import com.chenjimou.bluecupwroks.jetpack.room.PictureDatabase;
import com.chenjimou.bluecupwroks.jetpack.viewmodel.MainActivityViewModel;
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.inter.RetrofitRequest;
import com.chenjimou.bluecupwroks.ui.adapter.GalleryAdapter;
import com.chenjimou.bluecupwroks.ui.activity.MainActivity;
import com.chenjimou.bluecupwroks.widget.MainItemDecoration;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryFragment extends Fragment
{
    FragmentGalleryBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    MainActivityViewModel mViewModel;
    GalleryAdapter mAdapter;
    ProgressDialog mDialog;

    Disposable disposable;

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
            loadFromDatabase();
        }
    }

    void init()
    {
        mViewModel = ((MainActivity) getContext()).getModel();

        mBinding.rvGallery.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mBinding.rvGallery.addItemDecoration(new MainItemDecoration());
        mAdapter = new GalleryAdapter(getActivity(), dataOnUI);
        mBinding.rvGallery.setAdapter(mAdapter);

        mDialog = new ProgressDialog(getContext());
        mDialog.setTitle("图片加载中，请稍后...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
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
                mBinding.rvGallery.setVisibility(View.VISIBLE);
                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
            }
            else
            {
                mBinding.rvGallery.setVisibility(View.GONE);
                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
            }

            mAdapter.notifyDataSetChanged();

            result = true;
        }
        else
        {
            result = false;
        }
        return result;
    }

    void loadFromDatabase()
    {
        Observable.create(new ObservableOnSubscribe<List<PictureBean>>()
        {
            @Override
            public void subscribe(@NotNull ObservableEmitter<List<PictureBean>> emitter) throws Exception
            {
                List<PictureBean> dataFromDatabase = PictureDatabase.getInstance().getPictureDao().findAll();
                dataOnUI.addAll(dataFromDatabase);
                emitter.onNext(dataOnUI);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
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
                    public void onNext(@NotNull List<PictureBean> dataFromDatabase)
                    {
                        dataOnUI.addAll(dataFromDatabase);
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
                            mBinding.rvGallery.setVisibility(View.VISIBLE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
                        }
                        else
                        {
                            mBinding.rvGallery.setVisibility(View.GONE);
                            mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
                        }

                        mAdapter.notifyDataSetChanged();

                        mViewModel.setGalleryList(dataOnUI);
                    }
                });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }
}
