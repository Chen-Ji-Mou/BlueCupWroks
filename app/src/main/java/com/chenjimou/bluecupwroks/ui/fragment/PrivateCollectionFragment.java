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
import com.chenjimou.bluecupwroks.model.PictureBean;
import com.chenjimou.bluecupwroks.ui.adapter.PrivateCollectionAdapter;
import com.chenjimou.bluecupwroks.widget.CustomItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PrivateCollectionFragment extends Fragment
{
    FragmentGalleryBinding mBinding;

    final List<PictureBean> dataOnUI = new ArrayList<>();

    PrivateCollectionAdapter mAdapter;
    ProgressDialog mDialog;

    Disposable disposable;

    boolean alreadyLoad = false;
    boolean isError = false;

    private static final String TAG = "PrivateCollectionFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mBinding = FragmentGalleryBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        init();
    }

    void init()
    {
        mBinding.rvGallery.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mBinding.rvGallery.addItemDecoration(new CustomItemDecoration());
        mAdapter = new PrivateCollectionAdapter(getActivity(), dataOnUI);
        mBinding.rvGallery.setAdapter(mAdapter);

        mDialog = new ProgressDialog(getContext());
        mDialog.setTitle("图片加载中，请稍后...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (!alreadyLoad)
            loadFromDatabase();
    }

    void loadFromDatabase()
    {
        Observable.create(new ObservableOnSubscribe<List<PictureBean>>()
        {
            @Override
            public void subscribe(@NotNull ObservableEmitter<List<PictureBean>> emitter) throws Exception
            {
                try
                {
                    List<PictureBean> dataFromDatabase = PictureDatabase.getInstance().getPictureDao().findAll();
                    emitter.onNext(dataFromDatabase);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                finally
                {
                    emitter.onComplete();
                }
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
                                mBinding.rvGallery.setVisibility(View.VISIBLE);
                                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.GONE);
                            }
                            else
                            {
                                mBinding.rvGallery.setVisibility(View.GONE);
                                mBinding.getRoot().findViewById(R.id.layout_no_data).setVisibility(View.VISIBLE);
                            }

                            mAdapter.notifyDataSetChanged();

                            alreadyLoad = true;
                        }
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
