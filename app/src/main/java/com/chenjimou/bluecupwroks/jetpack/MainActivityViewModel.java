package com.chenjimou.bluecupwroks.jetpack;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.chenjimou.bluecupwroks.model.PictureBean;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private MediatorLiveData<List<PictureBean>> homeList;
    private MediatorLiveData<List<PictureBean>> galleryList;

    public MainActivityViewModel() {
        homeList = new MediatorLiveData<>();
        galleryList = new MediatorLiveData<>();
    }

    public LiveData<List<PictureBean>> getHomeList() {
        return homeList;
    }

    public void setHomeList(List<PictureBean> list) {
        this.homeList.setValue(list);
    }

    public LiveData<List<PictureBean>> getGalleryList() {
        return galleryList;
    }

    public void setGalleryList(List<PictureBean> galleryList) {
        this.galleryList.setValue(galleryList);
    }
}
