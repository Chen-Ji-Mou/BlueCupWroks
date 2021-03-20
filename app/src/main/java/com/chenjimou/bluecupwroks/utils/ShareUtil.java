package com.chenjimou.bluecupwroks.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chenjimou.bluecupwroks.myInterface.Pictures;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShareUtil {

    private static final String TAG = "ShareUtil";

    /**
     * 分享图片
     */
    @SuppressLint("CheckResult")
    public static void shareImage(String picture_id, String download_url, String packageName, String className, Activity activity){

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("加载中，请稍后...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "6666  图片id: "+pictureBean.getId());
                Uri imageUri = getImageFile(activity,"ab"+ picture_id);
                if (imageUri != null) {

                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    if(stringCheck(packageName) && stringCheck(className)){
                        intent.setComponent(new ComponentName(packageName, className));
                    }else if (stringCheck(packageName)) {
                        intent.setPackage(packageName);
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    Intent chooserIntent = Intent.createChooser(intent, "分享到:");
                    activity.startActivity(chooserIntent);
                } else {
//                    Log.d(TAG, "6666  有图片！");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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

                            retrofit.create(Pictures.class).getPicture(strings[1])
                                    .subscribeOn(Schedulers.io())// 事件产生的线程，IO异步
                                    .observeOn(Schedulers.io())// 事件消费的线程，IO异步
                                    .map(new Function<ResponseBody, Bitmap>() {
                                        @Override
                                        public Bitmap apply(ResponseBody responseBody) throws Exception {
                                            byte[] bitmapData = responseBody.bytes();
                                            return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                                        }
                                    })
                                    .map(new Function<Bitmap, Uri>() {
                                        @Override
                                        public Uri apply(Bitmap bitmap) throws Exception {
//                                            return Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, pictureBean.getId(),""));
                                            return addPictureToAlbum(activity, bitmap, "ab"+ picture_id,"image/jpg");
                                        }
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())// 事件消费的线程，切换主线程
                                    .subscribe(new Observer<Uri>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onNext(Uri imageUri) {
//                                            Log.d(TAG, "6666  下载好的图片URI: "+imageUri);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_SEND);
                                            intent.setType("image/*");
                                            if(stringCheck(packageName) && stringCheck(className)){
                                                intent.setComponent(new ComponentName(packageName, className));
                                            }else if (stringCheck(packageName)) {
                                                intent.setPackage(packageName);
                                            }
                                            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                            Intent chooserIntent = Intent.createChooser(intent, "分享到:");
                                            activity.startActivity(chooserIntent);
                                        }

                                        @Override
                                        public void onError(final Throwable e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "6666  onError: "+e.getMessage());
                                        }

                                        @Override
                                        public void onComplete() {
                                            progressDialog.dismiss();
                                        }
                                    });
                        }
                    });
                }
            }
        }).start();
    }

    private static boolean stringCheck(String str){
        return null != str && !TextUtils.isEmpty(str);
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

    private static Uri getImageFile(Context context, String filename)  {
        String[] projection = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Thumbnails.DATA
        };
        List<InputStream> insList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";//根据日期降序查询
        String selection = MediaStore.Images.Media.DISPLAY_NAME + "='" + filename + "'";   //查询条件 “显示名称为？”
        Cursor cursor =  resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder);
        Uri itemUri = null;
        if (cursor != null && cursor.moveToFirst()) {
            //媒体数据库中查询到的文件id
            int columnId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            do {
                //通过mediaId获取它的uri
                int mediaId = cursor.getInt(columnId);
//                String tPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)); //获取图片路径
                itemUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + mediaId );
//                try {
//                    //通过uri获取到inputStream
//                    ContentResolver cr = context.getContentResolver();
//                    InputStream ins=cr.openInputStream(itemUri);
//                    insList.add(ins);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
            } while (cursor.moveToNext());
        }
        return itemUri;
    }
}
