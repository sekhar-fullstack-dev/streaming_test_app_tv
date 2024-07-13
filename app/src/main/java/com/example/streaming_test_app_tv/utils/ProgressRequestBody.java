package com.example.streaming_test_app_tv.utils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ProgressRequestBody extends RequestBody {
    private final InputStream inputStream;
    private final String contentType;
    private final UploadCallbacks listener;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
    }

    public ProgressRequestBody(InputStream inputStream, String contentType, UploadCallbacks listener) {
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() throws IOException {
        return inputStream.available();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(inputStream);
            long total = 0;
            long read;
            long contentLength = contentLength(); // Try to get content length
            long updateInterval = contentLength / 100; // Update progress at each percent interval
            long nextUpdate = updateInterval;

            while ((read = source.read(sink.buffer(), 2048)) != -1) {
                total += read;
                sink.flush();

                // Only update progress if content length is known and greater than 0
                if (contentLength > 0 && total >= nextUpdate) {
                    this.listener.onProgressUpdate((int) (100 * total / contentLength));
                    nextUpdate += updateInterval;
                }
            }

            // Ensure final progress is reported
            if (contentLength > 0) {
                this.listener.onProgressUpdate(100);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }

}
