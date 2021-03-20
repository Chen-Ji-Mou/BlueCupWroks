package com.chenjimou.bluecupwroks.myInterface;

import com.chenjimou.bluecupwroks.model.PictureBean;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Pictures {
    @GET("v2/list")
    Observable<List<PictureBean>> getPictures(@Query("page") int page, @Query("limit") int limit);

    @GET("id/{path}")
    Observable<ResponseBody> getPicture(@Path("path") String path);
}
