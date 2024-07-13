package com.example.streaming_test_app_tv.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FileUploadResponse {
    @Expose
    @SerializedName("status")
    private String status;
    @Expose
    @SerializedName("error")
    private String errorMsg;
    @Expose
    @SerializedName("msg")
    private String message;

    private int progress;

    @Expose
    @SerializedName("data")
    private Data data;

    public FileUploadResponse(){

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public static class Data{
        @Expose
        @SerializedName("fileId")
        private String fileId;

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }
    }


}