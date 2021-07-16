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
    @Insert
    void insert(PictureBean...data);
    @Update
    void update(PictureBean...data);
    @Delete
    void delete(PictureBean...data);
    @Query("SELECT * FROM picturebean")
    List<PictureBean> findAll();
}
