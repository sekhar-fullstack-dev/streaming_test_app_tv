package com.example.streaming_test_app_tv.components;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.streaming_test_app_tv.BuildConfig;
import com.example.streaming_test_app_tv.model.DrawingBitmap;
import com.example.streaming_test_app_tv.model.DrawingPath;
import com.example.streaming_test_app_tv.model.FileUploadResponse;
import com.example.streaming_test_app_tv.services.FileUploaderService;
import com.example.streaming_test_app_tv.utils.DrawingUtils;
import com.example.streaming_test_app_tv.utils.DrawingWebSocketClient;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandwritingView extends View {
    private Paint paint;
    private ArrayList<Path> paths = new ArrayList<>();
    private final String TAG = getClass().getName();
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 16;
    private Canvas canvas;
    private DrawingPath currentPath;
    private ArrayList<DrawingPath> currentPaths = new ArrayList<>();
    private ArrayList<DrawingBitmap> drawingBitmapList = new ArrayList<>();
    private boolean isAdmin;
    private DrawingWebSocketClient webSocketClient;
    private boolean isEraserMode = false;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public HandwritingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        currentPath = new DrawingPath(paint.getColor());
    }

    public void setIsEraserMode(boolean isEraserMode){
        this.isEraserMode = isEraserMode;
    }


    public void setIsAdmin(boolean isAdmin){
        this.isAdmin = isAdmin;
        startSyncingWithUsers();
    }

    private void startSyncingWithUsers() {
        try {
            webSocketClient = new DrawingWebSocketClient(BuildConfig.SOCKET_ADDRESS, new DrawingWebSocketClient.MessageListener() {
                @Override
                public void onMessage(String message) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        float x = (float) jsonObject.getDouble("x");
                        float y = (float) jsonObject.getDouble("y");
                        if (!isAdmin){
                            drawExternalPath(x, y);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onImageDragged(String message) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        float x = (float) jsonObject.getDouble("x");
                        float y = (float) jsonObject.getDouble("y");
                        String imageUrl = jsonObject.getString("imageUrl");
                        imageUrl = BuildConfig.BASE_URL+"/file/"+imageUrl;
                        if (!isAdmin){
                            Glide.with(getContext()).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    drawBitmap(resource, x, y);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPathAdded(DrawingPath drawingPath) {
                    currentPaths.add(drawingPath);
                    invalidate();
                }

                @Override
                public void onPathRemoved(long timeStamp) {
                    Iterator<DrawingPath> iterator = currentPaths.iterator();
                    while (iterator.hasNext()) {
                        DrawingPath path = iterator.next();
                        if (timeStamp == path.getTimeStamp()) {
                            iterator.remove();
                            break;
                        }
                    }
                    invalidate();
                }
            });
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void drawExternalPath(float x, float y) {
        currentPath.addPoint(x,y);
        invalidate(); // Redraw the view
    }

    public void drawBitmap(Bitmap bitmap, float x, float y) {
        DrawingBitmap drawingBitmap = new DrawingBitmap(bitmap,x,y);
        drawingBitmapList.add(drawingBitmap);
        invalidate(); // Trigger onDraw
        if (isAdmin){
            //syncBitmapWithClients(bitmap,x,y);
        }
    }

    private Bitmap bitmapToUpload;
    private float bitmapX,bitmapY;
    private void syncBitmapWithClients(Bitmap bitmap, float x, float y) {
        this.bitmapToUpload = bitmap;
        this.bitmapX = x;
        this.bitmapY = y;
        if (uploadService==null){
            Intent intent = new Intent(getContext(), FileUploaderService.class);
            getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        else{
            uploadService.uploadBitmapAndGetUrl(bitmap);
        }

    }

    private FileUploaderService uploadService;
    private boolean isBound;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            FileUploaderService.LocalBinder binder = (FileUploaderService.LocalBinder)service;
            uploadService = binder.getService();
            isBound = true;
            uploadService.registerCallback(new FileUploaderService.UploadCallback() {
                @Override
                public void onUploadComplete(FileUploadResponse fileUploadResponse) {
                    if (fileUploadResponse.getStatus().equalsIgnoreCase("200") && fileUploadResponse.getData()!=null){
                        webSocketClient.sendImageDraggedAction(fileUploadResponse.getData().getFileId(),bitmapX,bitmapY);
                    }
                    else{
                        Log.d(TAG, "onUploadComplete: "+new Gson().toJson(fileUploadResponse));
                    }
                }
            });
            uploadService.uploadBitmapAndGetUrl(bitmapToUpload);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        for (DrawingBitmap mBitmap:drawingBitmapList){
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap.getBitmap(), mBitmap.getBitmapX(), mBitmap.getBitmapY(), null);
            }
        }
        for (DrawingPath path : currentPaths) {
            // Convert points to Path and draw
            @SuppressLint("DrawAllocation")
            Path drawPath = new Path();
            if (!path.getPoints().isEmpty()) {
                mX = path.getPoints().get(0).x;
                mY = path.getPoints().get(0).y;
                drawPath.moveTo(path.getPoints().get(0).x, path.getPoints().get(0).y);
                for (int i = 1; i < path.getPoints().size(); i++) {
                    PointF point = path.getPoints().get(i);
                    float dx = Math.abs(point.x - mX);
                    float dy = Math.abs(point.y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        drawPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
                        mX = point.x;
                        mY = point.y;
                    }
                }
            }
            paint.setColor(path.getColor()); // Set the paint color
            paint.setColor(path.getColor());
            canvas.drawPath(drawPath, paint);
        }
        @SuppressLint("DrawAllocation")
        Path drawPath = new Path();
        if (!currentPath.getPoints().isEmpty()) {
            mX = currentPath.getPoints().get(0).x;
            mY = currentPath.getPoints().get(0).y;
            drawPath.moveTo(mX, mY);
            for (int i = 1; i < currentPath.getPoints().size(); i++) {
                PointF point = currentPath.getPoints().get(i);
                float dx = Math.abs(point.x - mX);
                float dy = Math.abs(point.y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    drawPath.quadTo(mX, mY, (point.x + mX) / 2, (point.y + mY) / 2);
                    mX = point.x;
                    mY = point.y;
                }
            }
        }
        paint.setColor(currentPath.getColor()); // Set the paint color
        canvas.drawPath(drawPath, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: "+new Gson().toJson(event));
        float x = event.getX();
        float y = event.getY();
        int pointerCount = event.getPointerCount();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (pointerCount == 1) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    currentPath = new DrawingPath(paint.getColor());
                    if (isEraserMode){
                        checkAndRemovePath(x,y);
                    }
                    mX = x;
                    mY = y;
                } else if (pointerCount > 1) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (isEraserMode){
                        checkAndRemovePath(x,y);
                    }
                    else{
                        float dx = Math.abs(x - mX);
                        float dy = Math.abs(y - mY);
                        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                            mX = x;
                            mY = y;
                        }
                    }
                } else if (pointerCount > 1) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isEraserMode && isAdmin){
                    currentPaths.add(currentPath);
                    if (webSocketClient!=null){
                        webSocketClient.sendPath(currentPath);
                    }
                    //analyzeShape(currentPath);
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                return false;
        }
        if (!isEraserMode && isAdmin){
            currentPath.addPoint(mX,mY);
            invalidate();
        }
        return true;
    }

    /*private void analyzeShape(DrawingPath currentPath) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean isTriangle = ShapeRecognizer.isTriangle(currentPath);
                Log.d(TAG, "run: "+isTriangle);
            }
        });
    }*/

    private void checkAndRemovePath(float x, float y) {
        Iterator<DrawingPath> iterator = currentPaths.iterator();
        while (iterator.hasNext()) {
            DrawingPath path = iterator.next();
            if (isPathIntersecting(path, x, y)) {
                iterator.remove();
//                webSocketClient.removePath(path.getTimeStamp());
                break; // Remove this break to delete all intersecting paths at once
            }
        }
        invalidate(); // Redraw after paths have been modified
    }

    private boolean isPathIntersecting(DrawingPath path, float x, float y) {
        RectF bounds = new RectF();
        Path measurePath = new Path();
        for (int i = 0; i < path.getPoints().size(); i++) {
            PointF point = path.getPoints().get(i);
            if (i == 0) {
                measurePath.moveTo(point.x, point.y);
            } else {
                measurePath.lineTo(point.x, point.y);
            }
        }
        measurePath.computeBounds(bounds, true);
        return bounds.contains(x, y);
    }

    public void saveScreenContent(SharedPreferences pref){
        String path = DrawingUtils.serializeDrawing(currentPaths);
        pref.edit().putString("paths",path).apply();
    }

    public void loadSavedScreenContent(SharedPreferences pref){
        String s = pref.getString("paths","");
        if (!s.isEmpty()){
            currentPaths = (ArrayList<DrawingPath>) DrawingUtils.deserializeDrawing(s);
            invalidate();
        }
    }

    public void setPaintColor(int color){
        paint.setColor(color);
    }

}

