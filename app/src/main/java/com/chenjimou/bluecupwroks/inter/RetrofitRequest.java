package com.chenjimou.bluecupwroks.inter;

import com.chenjimou.bluecupwroks.model.PictureBean;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitRequest
{
    @GET("v2/list")
    Observable<List<PictureBean>> loadPictures(@Query("page") int page);

    @GET("id/{path}")
    Observable<ResponseBody> loadPicture(@Path("path") String path);
}
