package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by NSW00_008316 on 2017/05/18.
 */

public class ImageViewUtil {

    public static Drawable setTintColor(Context context, int image, @ColorRes int color) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, image));
        DrawableCompat.setTint(d, ContextCompat.getColor(context, color));
        DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
        return d;
    }

    public static Drawable setTintColor(Context context, int image, int red, int green, int blue) {
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(context, image));
        DrawableCompat.setTint(d, Color.rgb(red, green, blue));
        DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
        return d;
    }
}
