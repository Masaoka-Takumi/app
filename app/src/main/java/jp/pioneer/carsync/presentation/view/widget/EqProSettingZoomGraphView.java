package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.OnCustomEventListener;

/**
 * EQ Pro Setting ZoomのGraphView
 */

public class EqProSettingZoomGraphView extends View {
    private static final int EQ_SETTING_RANGE = 25; //EQ設定値の個数
    private static final int EQ_SETTING_MAX = 12; //EQ設定値の最大値
    private static final int EQ_SETTING_MIN = -12; //EQ設定値の最小値
    private static final int BAND_DATA_COUNT = 31; //全Band数

    OnCustomEventListener myCustomEventListener;

    private int mGraphWidth; //グラフエリアの横幅
    private int mGraphHeight; //グラフエリアの縦幅

    private Bitmap mBaseEqImage; //ベースEQ画像

    private double[] mDataX = new double[BAND_DATA_COUNT+2]; //スプライン曲線X座標配列
    private double[] mDataY = new double[BAND_DATA_COUNT+2]; //スプライン曲線Y座標配列

    private Path mPath;
    private Paint mPaint;
    private Paint mLinePaint;
    private SplineInterpolator mSplineInterP;
    private Rect mRectBase;
    private RectF mRectfBase;
    private boolean mIsChanged = false;

    public EqProSettingZoomGraphView(Context context) {
        super(context);
        init();
    }

    public EqProSettingZoomGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqProSettingZoomGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Resources res = getResources();
        mBaseEqImage = BitmapFactory.decodeResource(getResources(), R.drawable.p0112_baseeq);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setAlpha(255*80/100);
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(res.getDimension(R.dimen.eq_quick_spline_stroke_width));
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setColor(Color.WHITE);
        mPath = new Path();
        mSplineInterP = new SplineInterpolator();
    }

    public void setSplineData(double[] dataX,double[] dataY){
        mDataX = dataX;
        mDataY = dataY;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed) {
            mIsChanged = true;
        }
    }
    public void setSize(int graphWidth, int graphHeight){
        Resources res = getResources();
        mGraphWidth = graphWidth;
        mGraphHeight = graphHeight;

        int baseTop = (int)(mBaseEqImage.getHeight()*res.getDimension(R.dimen.eq_pro_header_height_px)/res.getDimension(R.dimen.eq_quick_mask_image_height_px));
        mRectBase = new Rect(0, baseTop, mBaseEqImage.getWidth(), mBaseEqImage.getHeight());
        mRectfBase = new RectF(0, 0, mGraphWidth, mGraphHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mGraphWidth > 0 && mGraphHeight > 0) {
            mPath.reset();
            mPath.moveTo(0, mGraphHeight);
            PolynomialSplineFunction polySplineF = mSplineInterP.interpolate(mDataX, mDataY);
            for (double x = 0; x <= mGraphWidth; x++) {
                double y = polySplineF.value(x);
                mPath.lineTo((float) x, (float) y);
            }
            mPath.lineTo(mGraphWidth, mGraphHeight);
            mPath.close();
            canvas.save();
            //パスでベースイメージをクリップ
            canvas.clipPath(mPath);
            canvas.drawBitmap(mBaseEqImage, mRectBase, mRectfBase, mPaint);
            canvas.restore();
            //パスのストロークを描画
            canvas.drawPath(mPath, mLinePaint);
            //レイアウト変更時
            if (mIsChanged) {
                mIsChanged = false;
                if (EqProSettingZoomGraphView.this.myCustomEventListener != null)
                    EqProSettingZoomGraphView.this.myCustomEventListener.onEvent();
            }
        }
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        myCustomEventListener = eventListener;
    }

}
