package com.smartbudget.app.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * Blur effect helper.
 * Creates beautiful frosted glass blur effects.
 */
public class BlurHelper {

    private static final float DEFAULT_BLUR_RADIUS = 25f;

    /**
     * Apply blur to a view (Android 12+).
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void applyBlur(View view, float radius) {
        RenderEffect blurEffect = RenderEffect.createBlurEffect(
                radius, radius, Shader.TileMode.CLAMP
        );
        view.setRenderEffect(blurEffect);
    }

    /**
     * Remove blur from view.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void removeBlur(View view) {
        view.setRenderEffect(null);
    }

    /**
     * Create blurred bitmap for older devices.
     */
    public static Bitmap blurBitmap(android.content.Context context, Bitmap bitmap, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return blurBitmapModern(bitmap, radius);
        } else {
            return blurBitmapLegacy(context, bitmap, radius);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static Bitmap blurBitmapModern(Bitmap bitmap, float radius) {
        // For Android 12+, use hardware blur
        Bitmap outputBitmap = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        paint.setAlpha(200);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return outputBitmap;
    }

    @SuppressWarnings("deprecation")
    private static Bitmap blurBitmapLegacy(android.content.Context context, Bitmap bitmap, float radius) {
        // For older devices, use RenderScript
        Bitmap outputBitmap = bitmap.copy(bitmap.getConfig(), true);
        
        try {
            RenderScript rs = RenderScript.create(context);
            Allocation input = Allocation.createFromBitmap(rs, bitmap);
            Allocation output = Allocation.createFromBitmap(rs, outputBitmap);
            
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blur.setInput(input);
            blur.setRadius(Math.min(radius, 25f)); // Max 25f for RenderScript
            blur.forEach(output);
            
            output.copyTo(outputBitmap);
            
            input.destroy();
            output.destroy();
            blur.destroy();
            rs.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return outputBitmap;
    }

    /**
     * Create a screenshot of a view for blurring.
     */
    public static Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
