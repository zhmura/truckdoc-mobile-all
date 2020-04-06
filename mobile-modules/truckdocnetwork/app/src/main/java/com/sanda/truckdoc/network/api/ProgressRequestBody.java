package com.sanda.truckdoc.network.api;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by k.natallie on 16.11.2016.
 */

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private UploadCallbacks mListener;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
    }

    public ProgressRequestBody(final File file, final ProgressRequestBody.UploadCallbacks listener) {
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("application/octet-stream");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;

            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
                mListener.onProgressUpdate((int) (100 * uploaded / fileLength));
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Log.e(ProgressRequestBody.class.getName(), "Error write to");
            }
        }


    }

}
