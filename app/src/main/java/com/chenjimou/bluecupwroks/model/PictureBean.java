package com.chenjimou.bluecupwroks.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class PictureBean extends LitePalSupport {

    @SerializedName(value = "id")
    private String picture_id;
    private String author;
    private int width;
    private int height;
    private String url;
    private String download_url;
    @Expose(serialize = false, deserialize = false)
    private byte[] picture_data;
    @Expose(serialize = false, deserialize = false)
    private boolean isCollection;

    public PictureBean() { }

    public String getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(String picture_id) {
        this.picture_id = picture_id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public byte[] getPicture_data() {
        return picture_data;
    }

    public void setPicture_data(byte[] picture_data) {
        this.picture_data = picture_data;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public void setCollection(boolean collection) {
        isCollection = collection;
    }
}