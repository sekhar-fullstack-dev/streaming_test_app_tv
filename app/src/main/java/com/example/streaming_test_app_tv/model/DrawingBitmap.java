package com.example.streaming_test_app_tv.model;

import android.graphics.Bitmap;

public class DrawingBitmap {
    private Bitmap bitmap;
    private float bitmapX,bitmapY;

    public DrawingBitmap(Bitmap bitmap, float bitmapX, float bitmapY) {
        this.bitmap = bitmap;
        this.bitmapX = bitmapX;
        this.bitmapY = bitmapY;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public float getBitmapX() {
        return bitmapX;
    }

    public void setBitmapX(float bitmapX) {
        this.bitmapX = bitmapX;
    }

    public float getBitmapY() {
        return bitmapY;
    }

    public void setBitmapY(float bitmapY) {
        this.bitmapY = bitmapY;
    }
}
