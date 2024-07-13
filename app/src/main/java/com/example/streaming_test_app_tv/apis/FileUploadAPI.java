package com.example.streaming_test_app_tv.apis;



import com.example.streaming_test_app_tv.model.FileUploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadAPI {
    @Multipart
    @POST("/file/upload")
    Call<FileUploadResponse> uploadFile(@Part MultipartBody.Part filePart);

}
