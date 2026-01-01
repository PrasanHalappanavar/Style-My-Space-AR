package com.example.uploadingdata;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Copies the file represented by Uri to a temporary file and returns the absolute path.
     */
    public static String getPath(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            String fileName = getFileName(context, uri);
            File tempFile = new File(context.getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buf = new byte[4096];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return tempFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("FileUtils", "Failed to get file path: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the display name (filename) from a Uri.
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(
                            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (result == null) {
            result = new File(uri.getPath()).getName();
        }

        return result;
    }
}
