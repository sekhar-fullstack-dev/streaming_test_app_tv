package com.example.streaming_test_app_tv.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;


import com.example.streaming_test_app_tv.components.HandwritingView;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

public class LoadAndDrawImageTask extends AsyncTask<Uri, Void, Bitmap> {
    private WeakReference<HandwritingView> handwritingViewWeakReference;
    private Context context;
    private int maxWidth;
    private float bitmapX,bitmapY;

    public LoadAndDrawImageTask(HandwritingView handwritingView, Context context, int maxWidth, float x, float y) {
        this.handwritingViewWeakReference = new WeakReference<>(handwritingView);
        this.context = context.getApplicationContext();
        this.maxWidth = maxWidth;
        bitmapX = x;
        bitmapY = y;
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        if (uris.length == 0) return null;
        Uri imageUri = uris[0];
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
            // Scale down the bitmap if it's too large
            return scaleBitmapDown(bitmap, maxWidth);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxWidth) {
        float aspectRatio = (float) bitmap.getHeight() / bitmap.getWidth();
        int height = Math.round(maxWidth * aspectRatio);
        bitmapX = bitmapX - (float)(maxWidth/2);
        bitmapY = bitmapY - (float) height/2;
        return Bitmap.createScaledBitmap(bitmap, maxWidth, height, true);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        HandwritingView handwritingView = handwritingViewWeakReference.get();
        if (handwritingView != null && bitmap != null) {
            handwritingView.drawBitmap(bitmap, bitmapX, bitmapY); // Assume (0,0) as the initial drawing position
        }
    }

}
