package com.example.streaming_test_app_tv.model;

import android.graphics.Color;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class DrawingPath {
    List<PointF> points = new ArrayList<>();
    int color;
    long timeStamp = System.currentTimeMillis();

    // Constructors
    public DrawingPath() {
        this.color = Color.BLACK; // Default color
    }

    public DrawingPath(int color) {
        this.color = color;
    }

    // Add a point to the path
    public void addPoint(float x, float y) {
        points.add(new PointF(x, y));
    }

    // Getters and setters
    public List<PointF> getPoints() {
        return points;
    }

    public void setPoints(List<PointF> points) {
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
