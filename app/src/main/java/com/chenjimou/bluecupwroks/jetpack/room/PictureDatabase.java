package com.chenjimou.bluecupwroks.jetpack.room;

import com.chenjimou.bluecupwroks.base.BaseApplication;
import com.chenjimou.bluecupwroks.model.PictureBean;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PictureBean.class}, version = 1, exportSchema = false)
public abstract class PictureDatabase extends RoomDatabase
{
    public abstract PictureDao getPictureDao();

    static volatile PictureDatabase mInstance;

    public static PictureDatabase getInstance()
    {
        if (mInstance == null)
        {
            synchronized (PictureDatabase.class)
            {
                if (mInstance == null)
                {
                    mInstance = Room.databaseBuilder(BaseApplication.sApplication,
                            PictureDatabase.class, "picture_database.db")
                            .allowMainThreadQueries() // 允许在主线程创建数据库
                            .build();
                }
            }
        }
        return mInstance;
    }
}
