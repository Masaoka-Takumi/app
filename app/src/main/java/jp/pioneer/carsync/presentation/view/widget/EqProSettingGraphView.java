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

import java.util.Arrays;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.OnCustomEventListener;

/**
 * EQ Pro SettingのGraphView
 */

public class EqProSettingGraphView extends View {
    private static final int EQ_SETTING_RANGE = 25; //EQ設定値の個数
    private static final int EQ_SETTING_MAX = 12; //EQ設定値の最大値
    private static final int EQ_SETTING_MIN = -12; //EQ設定値の最小値
    private static final int BAND_DATA_COUNT = 31; //全Band数
    //private static final int SPLINE_DATA_COUNT = 9; //スプライン曲線のポイント数
    private static final int SPLINE_DATA_COUNT = 31; //スプライン曲線のポイント数
    private static final double NULL_DATA_VALUE = -9999;
    OnCustomEventListener myCustomEventListener;

    private int mGraphWidth; //グラフエリアの横幅
    private int mGraphHeight; //グラフエリアの縦幅
    private float mGraphXLength; //グラフのX座標幅
    private float mGraphYLength; //グラフのY座標幅
    private float mGraphYTop; //グラフのY座標Top
    private float mGraphXStart; //グラフのX座標Start
    private Bitmap mBaseEqImage; //ベースEQ画像

    private double[] mDataX = new double[SPLINE_DATA_COUNT + 2]; //スプライン曲線X座標配列
    private double[] mDataY = new double[SPLINE_DATA_COUNT + 2]; //スプライン曲線Y座標配列
    private float[] mBands = new float[BAND_DATA_COUNT];//BAND設定値配列

    private Path mPath;
    private Paint mPaint;
    private Paint mLinePaint;
    private SplineInterpolator mSplineInterP;
    private Rect mRectBase;
    private RectF mRectfBase;
    private boolean mIsChanged = false;


    public EqProSettingGraphView(Context context) {
        super(context);
        init();
    }

    public EqProSettingGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqProSettingGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
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

    public float[] getBands() {
        return mBands;
    }

    public void setBandData(float[] bands) {
        if(!Arrays.equals(bands ,mBands)) {
            mBands = bands;
            for (int i = 1; i < BAND_DATA_COUNT + 1; i++) {
                mDataY[i] = mGraphYTop + mGraphYLength / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX - mBands[i - 1]);
            }
            mIsChanged = true;
            invalidate();
        }
    }

    public double[] getDataX() {
        return mDataX;
    }

    public void setDataY(double[] dataY) {
        for(int i=0;i<dataY.length;i++){
            if(dataY[i]!=NULL_DATA_VALUE){
                mDataY[i] = dataY[i];
            }
        }
        mIsChanged = true;
        invalidate();
    }

    public void setSize(int graphWidth, int graphHeight) {
        Resources res = getResources();
        mGraphWidth = graphWidth;
        mGraphHeight = graphHeight;
        mGraphXStart = res.getDimension(R.dimen.eq_pro_graph_margin_start);
        mGraphYTop = res.getDimension(R.dimen.eq_pro_graph_margin_top);
        mGraphXLength = mGraphWidth - mGraphXStart * 2;
        mGraphYLength = mGraphHeight - mGraphYTop * 2;
        int baseTop = (int) (mBaseEqImage.getHeight() * res.getDimension(R.dimen.eq_pro_header_height_px) / res.getDimension(R.dimen.eq_quick_mask_image_height_px));
        mRectBase = new Rect(0, baseTop, mBaseEqImage.getWidth(), mBaseEqImage.getHeight());
        mRectfBase = new RectF(0, 0, mGraphWidth, mGraphHeight);
        //スプライン曲線用座標データの設定
        mDataX[0] = 0;
        mDataX[SPLINE_DATA_COUNT + 1] = mGraphWidth;
        mDataY[0] = (double) mGraphHeight / 2;
        mDataY[SPLINE_DATA_COUNT + 1] = (double) mGraphHeight / 2;

        for (int i = 1; i < SPLINE_DATA_COUNT + 1; i++) {
            mDataX[i] = mGraphXStart + mGraphXLength / (BAND_DATA_COUNT - 1) * (i - 1);
            mDataY[i] = mGraphYTop + mGraphYLength / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX - mBands[i - 1]);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mGraphWidth > 0 && mGraphHeight > 0) {
            //ストロークの座標から設定値を求め、座標を変換
            for (int j = 1; j < SPLINE_DATA_COUNT + 1; j++) {
                int value = EQ_SETTING_MAX - (int) Math.round((EQ_SETTING_RANGE - 1) * ((mDataY[j] - mGraphYTop) / mGraphYLength));
                if (value > EQ_SETTING_MAX) {
                    value = EQ_SETTING_MAX;
                } else if (value < EQ_SETTING_MIN) {
                    value = EQ_SETTING_MIN;
                }
                mDataY[j] = (mGraphYLength / (double) (EQ_SETTING_RANGE - 1)) * (EQ_SETTING_MAX - value) + mGraphYTop;
                mBands[j - 1] = value;
            }

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
            //描画終了時・Reset時
            if (mIsChanged) {
                mIsChanged = false;
                if (EqProSettingGraphView.this.myCustomEventListener != null)
                    EqProSettingGraphView.this.myCustomEventListener.onEvent();
                //setBand();
            }
        }
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        myCustomEventListener = eventListener;
    }
}
