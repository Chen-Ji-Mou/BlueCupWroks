package com.chenjimou.bluecupwroks.jetpack.room;

import com.chenjimou.bluecupwroks.model.PictureBean;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PictureDao
{
    @Insert(entity = PictureBean.class)
    void insert(PictureBean...data);
    @Update(entity = PictureBean.class)
    void update(PictureBean...data);
    @Delete(entity = PictureBean.class)
    void delete(PictureBean...data);
    @Query("SELECT * FROM picturebean")
    List<PictureBean> findAll();
}
