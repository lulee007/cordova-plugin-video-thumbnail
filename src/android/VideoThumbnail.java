package com.lulee007.cordova.videothumbnail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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
            final String saveFolder = args.getString(4);

            if (videoPath == null || videoPath.isEmpty()) {
                callbackContext.error("videoPath was wrong");
                return true;
            }
            if (saveFolder == null || saveFolder.isEmpty()) {
                callbackContext.error("saveFolder was wrong");
                return true;
            }
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
                        String filePath = saveFolder.endsWith("/") ? saveFolder : saveFolder + "/";
                        filePath += "thumbnail_" + now.getTime() + ".jpg";
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
        // 获取视频的缩略图
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}
