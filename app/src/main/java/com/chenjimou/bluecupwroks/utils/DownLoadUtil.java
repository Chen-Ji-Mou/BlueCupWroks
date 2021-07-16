package com.chenjimou.bluecupwroks.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.chenjimou.bluecupwroks.inter.RetrofitRequest;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownLoadUtil {

    private static final String TAG = "DownLoadUtil";

    @SuppressLint("CheckResult")
    public static void asynchronousDownload(String picture_id, String download_url, final Activity activity){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://picsum.photos/")
                .client(new OkHttpClient.Builder()
                        .callTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        String[] strings = download_url.split("/id/");

        RetrofitRequest retrofitRequest = retrofit.create(RetrofitRequest.class);
        retrofitRequest.loadPicture(strings[1])
                .subscribeOn(Schedulers.io())// 事件产生的线程，IO异步
                .observeOn(Schedulers.io())// 事件消费的线程，IO异步
                .map(new Function<ResponseBody, Bitmap>() {
                    @Override
                    public Bitmap apply(ResponseBody responseBody) throws Exception {
                        byte[] bitmapData = responseBody.bytes();
                        return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                    }
                })
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
//                        String imageUri = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, pictureBean.getId(),"");
                        addPictureToAlbum(activity, bitmap, "ab"+ picture_id,"image/jpg");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"图片下载完成，请到相册里查看！", Toast.LENGTH_SHORT).show();
                            }
                        });
//                        Log.d(TAG, "6666  图片下载完成！");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d(TAG, "6666  onError: "+e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private static Uri addPictureToAlbum(Context context, Bitmap bitmap, String fileName, String mime_type) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mime_type);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return uri;
    }
}
