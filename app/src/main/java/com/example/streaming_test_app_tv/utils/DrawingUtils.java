package com.example.streaming_test_app_tv.utils;

import com.example.streaming_test_app_tv.model.DrawingPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DrawingUtils {

    public static String serializeDrawing(List<DrawingPath> paths) {
        Gson gson = new Gson();
        return gson.toJson(paths);
    }

    public static List<DrawingPath> deserializeDrawing(String json) {
        Gson gson = new Gson();
        Type drawingPathListType = new TypeToken<List<DrawingPath>>(){}.getType();
        return gson.fromJson(json, drawingPathListType);
    }
}
