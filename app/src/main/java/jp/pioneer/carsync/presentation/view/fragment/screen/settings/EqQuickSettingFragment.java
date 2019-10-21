package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.christophesmet.android.views.maskableframelayout.MaskableFrameLayout;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.EqQuickSettingPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.EqQuickSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import timber.log.Timber;

/**
 * EQ Quick Setting画面
 */

public class EqQuickSettingFragment extends AbstractScreenFragment<EqQuickSettingPresenter, EqQuickSettingView> implements EqQuickSettingView {

    @Inject EqQuickSettingPresenter mPresenter;
    @BindView(R.id.graph_layout) RelativeLayout mGraphLayout;
    @BindView(R.id.frm_mask) MaskableFrameLayout mMaskFrame;
    @BindView(R.id.cursor_low) RelativeLayout mCursorLow;
    @BindView(R.id.cursor_mid) RelativeLayout mCursorMid;
    @BindView(R.id.cursor_hi) RelativeLayout mCursorHi;
    @BindView(R.id.cursor_low_circle) ImageView mCursorLowCircle;
    @BindView(R.id.cursor_mid_circle) ImageView mCursorMidCircle;
    @BindView(R.id.cursor_hi_circle) ImageView mCursorHiCircle;
    @BindView(R.id.text_low_value) TextView mTextLow;
    @BindView(R.id.text_mid_value) TextView mTextMid;
    @BindView(R.id.text_hi_value) TextView mTextHi;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private Unbinder mUnbinder;
    private static final int EQ_SETTING_RANGE = 25; //EQ設定値の範囲(-12～12)
    private static final int EQ_SETTING_MAX = 12; //EQ設定値の最大値
    private static final int EQ_SETTING_MIN = -12; //EQ設定値の最小値
    private static final int SPLINE_DATA_COUNT = 5; //スプライン曲線データ個数
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private static final int BAND_LOW_INDEX = 5;  //Lowのカーソル地点
    private static final int BAND_MID_INDEX = 15;  //Midのカーソル地点
    private static final int BAND_HI_INDEX = 25;  //Hiのカーソル地点
    private static final int BAND_LOW_START = 0; //LowのBand域(1～10)
    private static final int BAND_LOW_END = 9; //LowのBand域(1～10)
    private static final int BAND_MID_START = 10; //MidのBand域(11～21)
    private static final int BAND_MID_END = 20; //MidのBand域(11～21)
    private static final int BAND_HI_START = 21; //HiのBand域(21～31)
    private static final int BAND_HI_END = 30; //HiのBand域(21～31)
    private static final float CURSOR_CIRCLE_MIN = 0.5f; //カーソル円の最小比率
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private GraphView mGraphView;
    //    private float mDensity; //画面解像度
    private int mContainerWidth; //グラフエリアの横幅
    private int mContainerHeight; //グラフエリアの縦幅
    private float mSliderLength; //スライダーの長さ
    private float mSliderStart; //スライダー上端Y座標
    private float mSliderEnd; //スライダー下端Y座標
    private boolean mIsFirstDrawn = false;
    private Bitmap mBaseEqImage; //ベースEQ画像
    private double[] mDataX = new double[SPLINE_DATA_COUNT]; //スプライン曲線X座標配列
    private double[] mDataY = new double[SPLINE_DATA_COUNT]; //スプライン曲線Y座標配列
    private float[] mBands = new float[BAND_DATA_COUNT];//BAND設定値配列
    private int mOrientation;
    private PolynomialSplineFunction mPolySplineF;//多項式スプライン関数

    public EqQuickSettingFragment() {

    }

    public static EqQuickSettingFragment newInstance(Bundle args) {
        EqQuickSettingFragment fragment = new EqQuickSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //mDensity = res.getDisplayMetrics().density;
        View view = inflater.inflate(R.layout.fragment_setting_eq_quick, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mBaseEqImage = BitmapFactory.decodeResource(getResources(), R.drawable.p0109_baseeq);
        }else{
            mBaseEqImage = BitmapFactory.decodeResource(getResources(), R.drawable.p0112_baseeq);
        }

        getPresenter().setCustomType(getArguments());
        mIsFirstDrawn = false;
        mGlobalLayoutListener = () -> {
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mGraphLayout.getWidth()) + ", " +
                    "Height = " + String.valueOf(mGraphLayout.getHeight()));
            if (!mIsFirstDrawn) {
                Resources res = getResources();
                mContainerWidth = mGraphLayout.getWidth();
                mContainerHeight = mGraphLayout.getHeight();

                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    mMaskFrame.setMask(R.drawable.p0108_maskeq);
                    mSliderStart = res.getDimension(R.dimen.eq_quick_slider_start_portrait);
                } else {
                    //mask画像の設定
                    Bitmap maskEqImage = BitmapFactory.decodeResource(res, R.drawable.p0113_maskeq);
                    //mask画像をグラフエリアのサイズにトリミング
                    int maskTop = (int) (maskEqImage.getHeight() * res.getDimension(R.dimen.eq_quick_header_height_px) / res.getDimension(R.dimen.eq_quick_mask_image_height_px));//ヘッダー部分の画面縦サイズに対する比率
                    Bitmap maskEqImage2 = Bitmap.createBitmap(maskEqImage, 0, maskTop, maskEqImage.getWidth(), maskEqImage.getHeight() - maskTop);
                    Drawable maskResized = new BitmapDrawable(res, maskEqImage2);
                    mMaskFrame.setMask(maskResized);
                    mSliderStart = res.getDimension(R.dimen.eq_quick_slider_start);
                }
                mSliderLength = mContainerHeight - mSliderStart * 2;
                mSliderEnd = mSliderStart + mSliderLength;
                mGraphView = new GraphView(getActivity());
                mMaskFrame.addView(mGraphView, new MaskableFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                mDataY[0] = (double) mContainerHeight / 2;
                mDataY[4] = (double) mContainerHeight / 2;
                mDataX[1] = ((double) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_LOW_INDEX;
                mDataX[2] = ((double) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_MID_INDEX;
                mDataX[3] = ((double) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_HI_INDEX;
                double interval = mDataX[2] - mDataX[1];
                mDataX[0] = mDataX[1] - interval;
                mDataX[4] = mDataX[3] + interval;
                mCursorLow.setX((int) (mDataX[1] - (float) mCursorLow.getWidth() / 2));
                mCursorMid.setX((int) (mDataX[2] - (float) mCursorMid.getWidth() / 2));
                mCursorHi.setX((int) (mDataX[3] - (float) mCursorHi.getWidth() / 2));
                int lowValue, midValue, hiValue;
                lowValue = (int)mBands[BAND_LOW_INDEX];
                midValue = (int)mBands[BAND_MID_INDEX];
                hiValue = (int)mBands[BAND_HI_INDEX];
                //Band域のの平均値から各店の設定値を求める
/*                    for(int i=0;i<=BAND_LOW_END;i++){
                        lowValue += mBands[i];
                    }
                    lowValue = Math.round((float)lowValue/(BAND_LOW_END-BAND_LOW_START+1));
                    for(int i=BAND_MID_START;i<=BAND_MID_END;i++){
                        midValue += mBands[i];
                    }
                    midValue = Math.round((float)midValue/(BAND_MID_END-BAND_MID_START+1));
                    for(int i=BAND_HI_START;i<=BAND_HI_END;i++){
                        hiValue += mBands[i];
                    }
                    hiValue = Math.round((float)hiValue/(BAND_HI_END-BAND_HI_START+1));*/
                setLowValueText(lowValue);
                setMidValueText(midValue);
                setHiValueText(hiValue);
                setLowCursor(lowValue);
                setMidCursor(midValue);
                setHiCursor(hiValue);

                DragViewListener listenerLow = new DragViewListener(mCursorLow);
                DragViewListener listenerMid = new DragViewListener(mCursorMid);
                DragViewListener listenerHi = new DragViewListener(mCursorHi);
                mCursorLow.setOnTouchListener(listenerLow);
                mCursorMid.setOnTouchListener(listenerMid);
                mCursorHi.setOnTouchListener(listenerHi);

                mGraphView.invalidate();

                mIsFirstDrawn = true;
            }
            // removeOnGlobalLayoutListener()の削除
            mGraphLayout.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        mGraphLayout.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected EqQuickSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EQ_QUICK_SETTING;
    }

    /**
     * Band値のSplineデータへの反映
     *
     * @param bands 31Bandの配列
     */
    @Override
    public void setBandData(@Size(31) float[] bands) {
        if (!Arrays.equals(bands, mBands)) {
            mBands = bands;
            //System.arraycopy(bands, 0, mBands, 0, bands.length);
            //onGlobalLayout()前は描画しない
            if (mIsFirstDrawn) {
                setLowValueText((int)mBands[BAND_LOW_INDEX]);
                setMidValueText((int)mBands[BAND_MID_INDEX]);
                setHiValueText((int)mBands[BAND_HI_INDEX]);
                setLowCursor((int)mBands[BAND_LOW_INDEX]);
                setMidCursor((int)mBands[BAND_MID_INDEX]);
                setHiCursor((int)mBands[BAND_HI_INDEX]);
                mGraphView.invalidate();
            }
        }
    }

    /**
     * Lowの設定値表示
     *
     * @param value 設定値
     */
    @Override
    public void setLowValueText(int value) {
        CharSequence text;
        if (value > 0) {
            text = "+" + String.valueOf(value);
        } else {
            text = String.valueOf(value);
        }
        TextViewUtil.setTextIfChanged(mTextLow, text);
    }

    /**
     * Midの設定値表示
     *
     * @param value 設定値
     */
    @Override
    public void setMidValueText(int value) {
        CharSequence text;
        if (value > 0) {
            text = "+" + String.valueOf(value);
        } else {
            text = String.valueOf(value);
        }
        TextViewUtil.setTextIfChanged(mTextMid, text);
    }

    /**
     * Hiの設定値表示
     *
     * @param value 設定値
     */
    @Override
    public void setHiValueText(int value) {
        CharSequence text;
        if (value > 0) {
            text = "+" + String.valueOf(value);
        } else {
            text = String.valueOf(value);
        }
        TextViewUtil.setTextIfChanged(mTextHi, text);
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mCursorLowCircle.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0111_selecteq, color));
        mCursorMidCircle.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0111_selecteq, color));
        mCursorHiCircle.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0111_selecteq, color));
    }

    private void setLowCursor(int value) {
        float positionY = (mSliderLength / (float) (EQ_SETTING_RANGE - 1)) * (EQ_SETTING_MAX - value) + mSliderStart;
        mCursorLow.setY((int) (positionY - (float) mCursorLow.getHeight() / 2));
        float scale = getScale(value);
        mCursorLowCircle.setScaleX(scale);
        mCursorLowCircle.setScaleY(scale);
        mDataY[1] = positionY;
    }

    private void setMidCursor(int value) {
        float positionY = (mSliderLength / (float) (EQ_SETTING_RANGE - 1)) * (EQ_SETTING_MAX - value) + mSliderStart;
        mCursorMid.setY((int) (positionY - (float) mCursorMid.getHeight() / 2));
        float scale = getScale(value);
        mCursorMidCircle.setScaleX(scale);
        mCursorMidCircle.setScaleY(scale);
        mDataY[2] = positionY;
    }

    private void setHiCursor(int value) {
        double positionY = (mSliderLength / (double) (EQ_SETTING_RANGE - 1)) * (EQ_SETTING_MAX - value) + mSliderStart;
        mCursorHi.setY((int) (positionY - (float) mCursorHi.getHeight() / 2));
        float scale = getScale(value);
        mCursorHiCircle.setScaleX(scale);
        mCursorHiCircle.setScaleY(scale);
        mDataY[3] = positionY;
    }

    private float getScale(int value) {
        return CURSOR_CIRCLE_MIN + (1 - CURSOR_CIRCLE_MIN) / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX + value);
    }

    private void setBand() {
        //スプライン曲線から設定値抽出
        float[] bands = new float[BAND_DATA_COUNT];
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            double x = (double) mContainerWidth / (BAND_DATA_COUNT - 1) * i;
            double y = mPolySplineF.value(x);
            int value = EQ_SETTING_MAX - Math.round((EQ_SETTING_RANGE - 1) * (((float) y - mSliderStart) / mSliderLength));
            if (value > EQ_SETTING_MAX) {
                value = EQ_SETTING_MAX;
            } else if (value < EQ_SETTING_MIN) {
                value = EQ_SETTING_MIN;
            }
            bands[i] = value;
        }
        getPresenter().onChangeBandValueAction(bands);
    }

    private class DragViewListener implements View.OnTouchListener {
        // ドラッグ対象のView
        private RelativeLayout dragView;
        // ドラッグ中に移動量を取得するための変数
        private int mOldX;
        private int mOldY;
        private int settingValue;

        public DragViewListener(RelativeLayout dragView) {
            this.dragView = dragView;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしている位置取得
            //int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            // 今回イベントでのView移動先の位置
            //int left = dragView.getLeft();// + (x - mOldX);
            int top;
            if(mOldY == 0) {
                top = (int) dragView.getY();
            }else{
                top = (int) dragView.getY() + (y - mOldY);
            }
            float positionY = top + dragView.getHeight() / 2;
            //座標から設定値を求める
            settingValue = EQ_SETTING_MAX - Math.round((EQ_SETTING_RANGE - 1) * ((positionY - mSliderStart) / mSliderLength));
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (positionY >= mSliderStart && positionY <= mSliderEnd) {
                        // Viewを移動する
                        dragView.setY(top);
                        float scale = getScale(settingValue);
                        if (dragView == mCursorLow) {
                            setLowValueText(settingValue);
                            mCursorLowCircle.setScaleX(scale);
                            mCursorLowCircle.setScaleY(scale);
                            mDataY[1] = positionY;
                        } else if (dragView == mCursorMid) {
                            setMidValueText(settingValue);
                            mCursorMidCircle.setScaleX(scale);
                            mCursorMidCircle.setScaleY(scale);
                            mDataY[2] = positionY;
                        } else if (dragView == mCursorHi) {
                            setHiValueText(settingValue);
                            mCursorHiCircle.setScaleX(scale);
                            mCursorHiCircle.setScaleY(scale);
                            mDataY[3] = positionY;
                        }
                        mGraphView.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //設定値から座標を求める
                    if (dragView == mCursorLow) {
                        setLowCursor(settingValue);
                    } else if (dragView == mCursorMid) {
                        setMidCursor(settingValue);
                    } else if (dragView == mCursorHi) {
                        setHiCursor(settingValue);
                    }
                    mGraphView.invalidate();
                    setBand();
                    break;
                default:
                    break;
            }

            // 今回のタッチ位置を保持
            //mOldX = x;
            mOldY = y;
            // イベント処理完了
            return true;
        }
    }

    private class GraphView extends View {

        private Path mPath;
        private Paint mPaint;
        private Paint mLinePaint;
        private Paint mCircleBlPaint, mCircleWhPaint;
        private SplineInterpolator mSplineInterP;
        private Rect mRectBase;
        private RectF mRectfBase;
        private float mCircleBlRound;
        private float mCircleWhRound;

        public GraphView(Context context) {
            super(context);
            init();
        }

        public GraphView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            Resources res = getResources();
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mRectBase = new Rect(0, 0, mBaseEqImage.getWidth(), mBaseEqImage.getHeight());
                mRectfBase = new RectF(0, 0, mContainerWidth, mContainerHeight);
            } else {
                //base画像をグラフエリアのサイズにトリミング
                int baseTop = (int) (mBaseEqImage.getHeight() * res.getDimension(R.dimen.eq_quick_header_height_px) / res.getDimension(R.dimen.eq_quick_mask_image_height_px));
                mRectBase = new Rect(0, baseTop, mBaseEqImage.getWidth(), mBaseEqImage.getHeight());
                mRectfBase = new RectF(0, 0, mContainerWidth, mContainerHeight);
            }
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setAlpha(255 * 80 / 100);
            mLinePaint = new Paint();
            mLinePaint.setStyle(Paint.Style.STROKE);
            mLinePaint.setStrokeWidth(res.getDimension(R.dimen.eq_quick_spline_stroke_width));
            mLinePaint.setStrokeCap(Paint.Cap.ROUND);
            mLinePaint.setStrokeJoin(Paint.Join.ROUND);
            mLinePaint.setColor(Color.WHITE);

            mPath = new Path();
            mSplineInterP = new SplineInterpolator();

            mCircleBlPaint = new Paint();
            mCircleBlPaint.setColor(Color.BLACK);
            mCircleBlPaint.setAlpha(76);
            mCircleWhPaint = new Paint();
            mCircleWhPaint.setColor(Color.WHITE);
            mCircleWhPaint.setAlpha(76);

            mCircleBlRound = res.getDimension(R.dimen.eq_quick_black_circle_round);
            mCircleWhRound = res.getDimension(R.dimen.eq_quick_white_circle_round);
        }

        @Override
        public void onDraw(Canvas canvas) {
            mPath.reset();
            mPath.moveTo(0, mContainerHeight);
            mPolySplineF = mSplineInterP.interpolate(mDataX, mDataY);
            for (double x = 0; x <= mContainerWidth; x++) {
                double y = mPolySplineF.value(x);
                mPath.lineTo((float) x, (float) y);
            }
            mPath.lineTo(mContainerWidth, mContainerHeight);
            mPath.close();
            canvas.save();
            //パスでベースイメージをクリップ
            canvas.clipPath(mPath);
            canvas.drawBitmap(mBaseEqImage, mRectBase, mRectfBase, mPaint);
            canvas.restore();
            //パスのストロークを描画
            canvas.drawPath(mPath, mLinePaint);

            int i;
            float x1, x2, x3, y;

            //黒丸を描画
            x1 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_LOW_INDEX;
            x2 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_MID_INDEX;
            x3 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_HI_INDEX;
            for (i = 0; i <= EQ_SETTING_MAX; i++) {
                y = mSliderStart + mSliderLength / EQ_SETTING_MAX * i;
                canvas.drawCircle(x1, y, mCircleBlRound, mCircleBlPaint);
                canvas.drawCircle(x2, y, mCircleBlRound, mCircleBlPaint);
                canvas.drawCircle(x3, y, mCircleBlRound, mCircleBlPaint);
            }
            //白丸を描画
            y = (float) mContainerHeight / 2;
            x1 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_LOW_INDEX;
            canvas.drawCircle(x1, y, mCircleWhRound, mCircleWhPaint);
            x2 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_MID_INDEX;
            canvas.drawCircle(x2, y, mCircleWhRound, mCircleWhPaint);
            x3 = ((float) (mContainerWidth) / (BAND_DATA_COUNT - 1)) * BAND_HI_INDEX;
            canvas.drawCircle(x3, y, mCircleWhRound, mCircleWhPaint);

        }
    }

    @Override
    public void setEnable(boolean isEnabled) {
        if(isEnabled) {
            mDisableLayer.setVisibility(View.GONE);
            mDisableLayer.setOnTouchListener(null);
        }else{
            mDisableLayer.setVisibility(View.VISIBLE);
            mDisableLayer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //some code....
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }
}
