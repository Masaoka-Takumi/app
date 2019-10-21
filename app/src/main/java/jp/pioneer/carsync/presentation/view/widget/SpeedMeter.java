package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import jp.pioneer.carsync.R;

/**
 * 速度計プログレス部品
 */
public class SpeedMeter extends View {
    // 速度計画像(色付け&マスク済み)
    private Bitmap circle;
    // 描画クラス
    Rect mBounds;
    RectF mBoundsF;
    // 横幅
    int width;
    // 進捗(ここでは速度) 0 ~ 360
    public int seekLevel = 0;
    private int mColor;
    /**
     * Constructor
     *
     * @param context Context
     */
    public SpeedMeter(Context context) {
        this(context, null);
    }

    /**
     * Constructor
     * <p>
     * 画像のロードと色付け、マスクを行う。
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public SpeedMeter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        Bitmap frame = BitmapFactory.decodeResource(getResources(), R.drawable.p0360_speedmeter);
        Bitmap light = BitmapFactory.decodeResource(getResources(), R.drawable.p0361_speedmeterblur);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColor = ContextCompat.getColor(context,outValue.resourceId);
        light = setColor(light, mColor);
        circle = blendBitmap(light, frame);
    }

    /**
     * Viewのサイズに変更があった場合、描画モデルもサイズの変更を行う。
     *
     * @param w    変更後の幅
     * @param h    変更後の高さ
     * @param oldw 変更前の幅
     * @param oldh 変更前の高さ
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;

        mBounds = new Rect(0, 0, w, h);
        mBoundsF = new RectF(mBounds);

        circle = Bitmap.createScaledBitmap(circle, w, h, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        int sc = canvas.saveLayer(mBoundsF, paint);

        // マスクに使用する円形を作成
        int[] colors = {0xFF000000, 0xFF000000, 0x00000000};
        float[] positions = {0.0f, 0.5f, 1.0f};
        positions[1] = 0.07f + (seekLevel / 360.0f) - 0.05f;
        positions[2] = 0.07f + (seekLevel / 360.0f) + 0.02f;
        if(seekLevel==0)positions[2] = 0;
        SweepGradient sg = new SweepGradient(width / (float)2, width / (float)2, colors, positions);
        paint.setDither(true);
        paint.setShader(sg);

        // DST
        canvas.drawArc(mBoundsF, 0, seekLevel + (0.07f+0.02f)*360, true, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // SRC
        canvas.drawBitmap(circle, 0, 0, paint);
        paint.setXfermode(null);

        canvas.saveLayer(mBoundsF, paint);
        super.onDraw(canvas);
        canvas.restoreToCount(sc);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = widthSize <= heightSize ? widthSize : heightSize;

        setMeasuredDimension(size, size);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, heightMode);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 画像に色を付ける
     *
     * @param bitmap 画像データ
     * @param color  色
     * @return Bitmap 画像データ
     */
    private Bitmap setColor(Bitmap bitmap, int color) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();

        Canvas myCanvas = new Canvas(mutableBitmap);

        Paint pnt = new Paint();
        pnt.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        myCanvas.drawBitmap(mutableBitmap, 0, 0, pnt);

        return mutableBitmap;
    }

    /**
     * 画像の合成
     *
     * @param currentBitmap 合成元の画像
     * @param blendBitmap   合成する画像
     * @return Bitmap 画像データ
     */
    private Bitmap blendBitmap(Bitmap currentBitmap, Bitmap blendBitmap) {
        int width = currentBitmap.getWidth();
        int height = currentBitmap.getHeight();
        Bitmap new_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(new_bitmap);
        canvas.drawBitmap(currentBitmap, 0, 0, null);
        int disWidth = (width - blendBitmap.getWidth()) / 2;
        int disHeight = (height - blendBitmap.getHeight()) / 2;
        canvas.drawBitmap(blendBitmap, disWidth, disHeight, null);

        return new_bitmap;
    }
}
