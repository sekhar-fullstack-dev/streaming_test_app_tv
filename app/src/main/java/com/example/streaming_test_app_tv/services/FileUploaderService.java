package com.example.streaming_test_app_tv.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.streaming_test_app_tv.apis.FileUploadAPI;
import com.example.streaming_test_app_tv.apis.RetrofitClient;
import com.example.streaming_test_app_tv.interfaces.OnCallbackListener;
import com.example.streaming_test_app_tv.model.FileUploadResponse;
import com.example.streaming_test_app_tv.utils.ProgressRequestBody;
import com.example.streaming_test_app_tv.utils.Utils;

import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploaderService extends Service {
    private FileUploadAPI service;
    private UploadCallback uploadCallback;
    private final IBinder binder = new LocalBinder();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class LocalBinder extends Binder {
        public FileUploaderService getService() {
            return FileUploaderService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (service==null){
            service = RetrofitClient.getInstance().getUploadAPI();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void registerCallback(UploadCallback callback) {
        this.uploadCallback = callback;
    }

    public void uploadBitmapAndGetUrl(Bitmap bitmap){
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), Utils.bitmapToByteArray(bitmap));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image", requestFile);

        // Making the call
        Call<FileUploadResponse> call = service.uploadFile(body);
        call.enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<FileUploadResponse> call, @NonNull Response<FileUploadResponse> response) {
                uploadCallback.onUploadComplete(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<FileUploadResponse> call, @NonNull Throwable t) {
                Toast.makeText(FileUploaderService.this, "Something went wrong 1234ASF3", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void uploadFileFromUri(Uri fileUri, OnCallbackListener onCallbackListener) {
        try {
            // Get a ContentResolver instance
            ContentResolver contentResolver = getContentResolver();

            // Determine the file's content type
            String contentType = contentResolver.getType(fileUri);
            if (contentType == null) {
                // Default to octet-stream if type cannot be determined
                contentType = "application/octet-stream";
            }

            // Create a RequestBody instance from the input stream and content type
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            assert inputStream != null;
            ProgressRequestBody requestFile = new ProgressRequestBody(inputStream, contentType, new ProgressRequestBody.UploadCallbacks() {
                @Override
                public void onProgressUpdate(int percentage) {
                    Log.d("TAG", "onProgressUpdate: "+percentage);
                }
            });
            // Create MultipartBody.Part using file name and request body
            String fileName = Utils.getFileName(fileUri, contentResolver); // Implement this method to get file name from Uri
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            // Making the call
            Call<FileUploadResponse> call = service.uploadFile(body);
            call.enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(@NonNull Call<FileUploadResponse> call, @NonNull Response<FileUploadResponse> response) {
                    onCallbackListener.callBack(response.body());
                }

                @Override
                public void onFailure(@NonNull Call<FileUploadResponse> call, @NonNull Throwable t) {
                    Toast.makeText(FileUploaderService.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (FileNotFoundException e) {
            Toast.makeText(FileUploaderService.this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public interface UploadCallback{
        void onUploadComplete(FileUploadResponse fileUploadResponse);
    }


}
