package com.bitanga.android.lynkactivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;

public class PictureUtils {
//    public static Bitmap postBitMap;
//
//    public static Bitmap getPostBitMap() {
//        return postBitMap;
//    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        /**error here:java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.WindowManager android.app.Activity.getWindowManager()' on a null object reference**/
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);

//        postBitMap = getScaledBitmap(path, size.x, size.y);

        return getScaledBitmap(path, size.x, size.y);
    }
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        //Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        //Figure out how much to scale down by
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale :
                    widthScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        //Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }
}
