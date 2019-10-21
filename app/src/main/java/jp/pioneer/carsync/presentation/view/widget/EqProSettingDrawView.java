package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.OnCustomEventListener;

/**
 * EQ Pro SettingのDrawView
 */

public class EqProSettingDrawView extends RelativeLayout {
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private static final int SPLINE_DATA_COUNT = 31; //スプライン曲線のポイント数
    private double[] mDataX = new double[SPLINE_DATA_COUNT+2]; //スプライン曲線X座標配列
    private double[] mDataY = new double[SPLINE_DATA_COUNT+2]; //スプライン曲線Y座標配列
    private static final double NULL_DATA_VALUE = -9999;
    OnCustomEventListener myCustomEventListener;

    private int mGraphWidth; //グラフエリアの横幅
    private int mGraphHeight; //グラフエリアの縦幅
    private float mGraphXLength; //グラフのX座標幅
    private float mGraphYLength; //グラフのY座標幅
    private float mGraphYTop; //グラフのY座標Top
    private float mGraphXStart; //グラフのX座標Start

    private View mCursor;
    private ImageView mCursorBack;
    private float mCursorWidth,mCursorHeight;
    private Path mPath;
    private Paint mPaint,mPaintBlur;
    private MaskFilter mBlur;
    private float mLastTouchX;
    private float mLastTouchY;
    private boolean mIsTouchFlg;

    private List<Float> mPathX = new ArrayList<>();//パスのX座標リスト
    private List<Float> mPathY = new ArrayList<>();//パスのY座標リスト

    public EqProSettingDrawView(Context context) {
        this(context, null);
    }

    public EqProSettingDrawView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.layout.element_drawing_cursor);
    }

    public EqProSettingDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View layout = LayoutInflater.from(context).inflate(R.layout.element_drawing_cursor, this);
        mCursor = layout.findViewById(R.id.cursor);
        mCursorBack = (ImageView)layout.findViewById(R.id.cursor_back);
        init();
        //onDraw呼び出しを有効にする
        setWillNotDraw(false);
    }

    public void setUIColor(int color){
        int uiColor = ContextCompat.getColor(getContext(),color);
        mPaintBlur.setColor(uiColor);
        mCursorBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0114_draweq, color));
    }

    private void init(){
        Resources res = getResources();
        mPaint = new Paint();
        mPaint.setStrokeWidth(res.getDimension(R.dimen.eq_pro_drawing_stroke_width));
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaintBlur = new Paint();
        mBlur = new BlurMaskFilter(res.getDimension(R.dimen.eq_pro_drawing_stroke_blur_radius), BlurMaskFilter.Blur.NORMAL);
        mPaintBlur.setStrokeWidth(res.getDimension(R.dimen.eq_pro_drawing_stroke_blur_width));
        mPaintBlur.setAntiAlias(true);
        mPaintBlur.setColor(Color.WHITE);
        mPaintBlur.setStyle(Paint.Style.STROKE);
        mPaintBlur.setStrokeJoin(Paint.Join.ROUND);
        mPaintBlur.setStrokeCap(Paint.Cap.ROUND);
        mPaintBlur.setMaskFilter(mBlur);
        mCursorWidth = res.getDimension(R.dimen.eq_pro_graph_cursor_width);
        mCursorHeight = res.getDimension(R.dimen.eq_pro_graph_cursor_width);
        mPath = new Path();

    }

    public void setDataX(double[] dataX) {
        mDataX = dataX;
    }

    public double[] getDataY(){
        double[] dataY = new double[BAND_DATA_COUNT+2];
        //配列の深いコピー
        System.arraycopy(mDataY,0,dataY,0,mDataY.length);
        return dataY;
    }

    public void setSize(int graphWidth, int graphHeight) {
        Resources res = getResources();
        mGraphWidth = graphWidth;
        mGraphHeight = graphHeight;
        mGraphXStart = res.getDimension(R.dimen.eq_pro_graph_margin_start);
        mGraphYTop = res.getDimension(R.dimen.eq_pro_graph_margin_top);
        mGraphXLength = mGraphWidth - mGraphXStart * 2;
        mGraphYLength = mGraphHeight - mGraphYTop * 2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaintBlur);
        canvas.drawPath(mPath, mPaint);
        if(mIsTouchFlg) {
            mCursor.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x,y;
        x=event.getX();
        y=event.getY();

        //グラフ外への描画もグラフ内に収める
        if(y < mGraphYTop){
            y = mGraphYTop;
        }else if(y > mGraphYTop + mGraphYLength){
            y = mGraphYTop + mGraphYLength;
        }

        mCursor.setX(x - mCursorWidth/2);
        mCursor.setY(y - mCursorHeight/2);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                mCursor.setVisibility(VISIBLE);
                mIsTouchFlg = true;
                break;
            case MotionEvent.ACTION_MOVE:
                //ストロークを滑らかにする
                float middlePointX = (mLastTouchX + x) / 2;
                float middlePointY = (mLastTouchY + y) / 2;
                mPath.quadTo(mLastTouchX, mLastTouchY, middlePointX, middlePointY);

                mPathX.add(x);
                mPathY.add(y);

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mPath.reset();
                mCursor.setVisibility(GONE);
                if(mPathX.size()>0) {
                    //ストロークから各BandのY座標を格納(ストロークの通ったBand域のみ上書き)
                    float minX, maxX;
                    minX = mPathX.get(0);
                    maxX = mPathX.get(0);
                    Arrays.fill(mDataY, NULL_DATA_VALUE);//初期化
                    for (int i = 1; i < mPathX.size() - 1; i++) {
                        if (mPathX.get(i) < minX) {
                            minX = mPathX.get(i);
                        }
                        if (mPathX.get(i) > maxX) {
                            maxX = mPathX.get(i);
                        }
                    }
                    for (int j = 1; j < SPLINE_DATA_COUNT + 1; j++) {
                        boolean isFound = false;
                        for (int i = mPathX.size() - 1; i > 0; i--) {
                            if (mPathX.get(i) > mDataX[j - 1] && mPathX.get(i) <= mDataX[j]) {
                                mDataY[j] = mPathY.get(i);
                                isFound = true;
                                break;
                            }
                        }
                        if (mDataX[j] >= minX && mDataX[j] <= maxX) {
                            if (!isFound) {
                                mDataY[j] = mDataY[j - 1];
                            }
                        }
                    }
                    mPathX.clear();
                    mPathY.clear();
                    //タッチUP時にイベント送信
                    if (EqProSettingDrawView.this.myCustomEventListener != null)
                        EqProSettingDrawView.this.myCustomEventListener.onEvent();
                }
                mIsTouchFlg = false;
                break;
            default:
                break;
        }
        mLastTouchX = x;
        mLastTouchY = y;
        invalidate();
        return true;
    }

    public void setCustomEventListener(OnCustomEventListener eventListener) {
        myCustomEventListener = eventListener;
    }
}