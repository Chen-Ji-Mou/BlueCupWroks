package com.chenjimou.bluecupwroks.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PictureBean implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    public int database_id = 0;
    @ColumnInfo(name = "id")
    String id;
    @ColumnInfo(name = "author")
    String author;
    @ColumnInfo(name = "width")
    int width;
    @ColumnInfo(name = "height")
    int height;
    @ColumnInfo(name = "url")
    String url;
    @ColumnInfo(name = "download_url")
    String download_url;
    @Expose(serialize = false, deserialize = false)
    boolean isCollection;
    public PictureBean() { }
    protected PictureBean(Parcel in)
    {
        id = in.readString();
        author = in.readString();
        width = in.readInt();
        height = in.readInt();
        url = in.readString();
        download_url = in.readString();
        isCollection = in.readByte() != 0;
    }
    public static final Creator<PictureBean> CREATOR = new Creator<PictureBean>()
    {
        @Override
        public PictureBean createFromParcel(Parcel in)
        {
            return new PictureBean(in);
        }

        @Override
        public PictureBean[] newArray(int size)
        {
            return new PictureBean[size];
        }
    };
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getAuthor()
    {
        return author;
    }
    public void setAuthor(String author)
    {
        this.author = author;
    }
    public int getWidth()
    {
        return width;
    }
    public void setWidth(int width)
    {
        this.width = width;
    }
    public int getHeight()
    {
        return height;
    }
    public void setHeight(int height)
    {
        this.height = height;
    }
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public String getDownload_url()
    {
        return download_url;
    }
    public void setDownload_url(String download_url)
    {
        this.download_url = download_url;
    }
    public boolean isCollection()
    {
        return isCollection;
    }
    public void setCollection(boolean collection)
    {
        isCollection = collection;
    }
    @Override
    public String toString()
    {
        return "PictureBean{" + "id=" + id + ", author='" + author + '\'' + ", width=" + width + ", height=" + height + ", url='" + url +
                '\'' + ", download_url='" + download_url + '\'' + ", isCollection=" + isCollection + '}';
    }
    @Override
    public int describeContents()
    {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(url);
        dest.writeString(download_url);
        dest.writeByte((byte)(isCollection ? 1 : 0));
    }
}