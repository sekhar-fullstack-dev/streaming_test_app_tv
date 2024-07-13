package com.example.streaming_test_app_tv.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.streaming_test_app_tv.model.DrawingPath;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DrawingWebSocketClient extends WebSocketClient {

    private final String TAG = this.getClass().getName();
    private final MessageListener messageListener;

    public DrawingWebSocketClient(String serverUri, MessageListener messageListener) throws URISyntaxException {
        super(new URI(serverUri));
        this.messageListener = messageListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // Connection opened
        Log.d(TAG, "onOpen: "+handshakedata);
    }

    @Override
    public void onMessage(String message) {
        // Message received from server
        Log.d(TAG, "onMessage: "+message);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        super.onMessage(message);
        byte[] bytes;
        if (message.hasArray()) {
            bytes = message.array();
        } else {
            bytes = new byte[message.remaining()];
            message.get(bytes);
        }
        String messageString = new String(bytes, StandardCharsets.UTF_8);
        try{
            JSONObject jsonObject = new JSONObject(messageString);
            String action = jsonObject.getString("action");
            switch (action){
                case "image":
                    messageListener.onImageDragged(messageString);
                    break;
                case "path":
                    DrawingPath d = new Gson().fromJson(messageString, DrawingPath.class);
                    messageListener.onPathAdded(d);
                    break;
                case "removePath":
                    messageListener.onPathRemoved(jsonObject.getLong("timeStamp"));
                    break;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // Connection closed
        Log.d(TAG, "onClose: "+reason);
    }

    @Override
    public void onError(Exception ex) {
        // Error occurred
        Log.d(TAG, "onError: "+ex);
    }

    public void sendImageDraggedAction(String url, float x, float y){
        @SuppressLint("DefaultLocale")
        String message = String.format("{\"action\": \"image\", \"x\": %f, \"y\": %f, \"imageUrl\": \"%s\"}", x, y, url);
        send(message);
    }

    public void sendPath(DrawingPath currentPath) {
        JsonObject jsonObject = new Gson().toJsonTree(currentPath).getAsJsonObject();
        jsonObject.addProperty("action","path");
        send(jsonObject.toString());
    }

    public void removePath(long pathTimeStamp) {
        String message = String.format("{\"action\": \"removePath\", \"timeStamp\":%s}", pathTimeStamp);
        send(message);
    }

    public interface MessageListener{
        void onMessage(String message);
        void onImageDragged(String message);
        void onPathAdded(DrawingPath drawingPath);
        void onPathRemoved(long timeStamp);
    }
}
