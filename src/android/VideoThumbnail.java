package com.lulee007.cordova.videothumbnail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class VideoThumbnail extends CordovaPlugin {

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        Activity activity = this.cordova.getActivity();
        if (action.equals("buildThumbnail")) {
            final String videoPath = args.getString(0);
            final int width = args.getInt(1) > 0 ? args.getInt(1) : 100;
            final int height = args.getInt(2) > 0 ? args.getInt(2) : 100;
            /**
             * MediaStore.Video.Thumbnails.MICRO_KIND
             */

            final int kind = args.getInt(3) > 0 && args.getInt(3) < 4 ? args.getInt(3) : MediaStore.Video.Thumbnails.MINI_KIND;

            if (videoPath == null || videoPath.isEmpty()) {
                callbackContext.error("videoPath was wrong");
                return true;
            }

            final File cacheDir=activity.getExternalCacheDir();
            cordova.getThreadPool().execute(new Runnable() {

                public void run() {
                    Bitmap bitmap = null;
                    bitmap = getVideoThumbnail(videoPath, width, height, kind);
                    if (bitmap == null) {
                        callbackContext.error("get video thumbnail failed,maybe videopath was wrong.");
                        return;
                    }

                    FileOutputStream theOutputStream = null;
                    try {
                        Date now = new Date();
                        String filePath =cacheDir.getAbsolutePath()+"thumbnail_" + now.getTime() + ".jpg";
                        File theOutputFile = new File(filePath);
                        if (!theOutputFile.exists()) {
                            if (!theOutputFile.createNewFile()) {
                                callbackContext.error("Could not save thumbnail.");
                                return;
                            }
                        }
                        if (theOutputFile.canWrite()) {
                            theOutputStream = new FileOutputStream(theOutputFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, theOutputStream);
                            if (theOutputStream != null)
                                theOutputStream.close();
                            callbackContext.success(filePath);
                        }else {
                            callbackContext.error("Could not save thumbnail; target not writeable");
                            return;
                        }
                    } catch ( IOException e ) {
                        e.printStackTrace();
                        callbackContext.error("I/O exception saving thumbnail");
                    } finally {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    }

                }
            });
            return true;
        }

        return false;
    }

    /**
     * @param videoPath 视频路径
     * @param width
     * @param height
     * @param kind      eg:MediaStore.Video.Thumbnails.MICRO_KIND   MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return
     */
    private Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                     int kind) {
        Uri videoUri= Uri.parse(videoPath);
        // 获取视频的缩略图
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(getPath(cordova.getActivity(), videoUri), kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
