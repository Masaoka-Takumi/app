package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.christophesmet.android.views.maskableframelayout.MaskableFrameLayout;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.EqProSettingZoomPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.EqProSettingZoomView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.EqProSettingZoomGraphView;
import jp.pioneer.carsync.presentation.view.widget.ObservableHorizontalScrollView;
import timber.log.Timber;

/**
 * EQ Pro Setting Zoom画面
 */

public class EqProSettingZoomFragment extends AbstractScreenFragment<EqProSettingZoomPresenter, EqProSettingZoomView> implements EqProSettingZoomView {
    @Inject EqProSettingZoomPresenter mPresenter;
    @BindView(R.id.graph_layout) RelativeLayout mGraphLayout;
    @BindView(R.id.frm_mask) MaskableFrameLayout mMaskFrame;
    @BindView(R.id.shrink_image) ImageView mShrinkView;
    @BindView(R.id.shrink_scroll) ImageView mShrinkScrollView;
    @BindView(R.id.graph_view) EqProSettingZoomGraphView mGraphView;
    @BindView(R.id.scroll_view) ObservableHorizontalScrollView mScrollView;
    @BindView(R.id.cursor) RelativeLayout mCursor;
    @BindView(R.id.cursor_back) ImageView mCursorBack;
    @BindView(R.id.edit_line) RelativeLayout mEditLine;
    @BindView(R.id.edit_line_back) View mEditLineBack;
    @BindView(R.id.textView) TextView mTextView;
    @BindView(R.id.graph_line) ImageView mGraphLine;
    @BindView(R.id.graph_line_vertical) ImageView mGraphLineVertical;
    @BindView(R.id.graph_horizontal_value) ImageView mGraphHorizontalValue;
    @BindView(R.id.scroll_view_value) HorizontalScrollView mScrollViewValue;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private Unbinder mUnbinder;
    private static final int EQ_SETTING_RANGE = 25; //EQ設定値の個数
    private static final int EQ_SETTING_MAX = 12; //EQ設定値の最大値
    private static final int EQ_SETTING_MIN = -12; //EQ設定値の最小値
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private static final String[] STR_EQ_HZ = {"20Hz", "25Hz", "31.5Hz", "40Hz", "50Hz", "63Hz", "80Hz",
            "100Hz", "125Hz", "160Hz", "200Hz", "250Hz", "315Hz", "400Hz", "500Hz", "630Hz", "800Hz",
            "1kHz", "1.25kHz", "1.6kHz", "2kHz", "2.5kHz", "3.15kHz", "4kHz", "5kHz", "6.3kHz", "8kHz",
            "10kHz", "12.5kHz", "16kHz", "20kHz"};
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ImageView[] mPointBack = new ImageView[BAND_DATA_COUNT];//選択ポイント画像
    private View[] mPoint = new View[BAND_DATA_COUNT];//選択ポイント
    private int mGraphWidth; //グラフエリアの横幅
    private int mGraphHeight; //グラフエリアの縦幅
    private float mGraphXLength; //グラフのX座標幅
    private float mGraphYLength; //グラフのY座標幅
    private float mGraphYTop; //グラフのY座標開始位置
    private float mGraphXStart; //グラフのX座標開始位置

    private boolean mIsFirstDrawn = false;
    private double[] mDataX = new double[BAND_DATA_COUNT + 2]; //スプライン曲線X座標配列
    private double[] mDataY = new double[BAND_DATA_COUNT + 2]; //スプライン曲線Y座標配列
    private float[] mBands = new float[BAND_DATA_COUNT];//BAND設定値配列
    private int mScrollX;
    private Bitmap mBitmapForGraph;
    private Bitmap mBitmapForDecreaseGraph;
    private Bitmap mBitmapForMask;
    public EqProSettingZoomFragment() {

    }

    public static EqProSettingZoomFragment newInstance(Bundle args) {
        EqProSettingZoomFragment fragment = new EqProSettingZoomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_eq_pro_zoom, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        //選択ポイントの配置
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            mGraphLayout.addView(relativeLayout);
            mPoint[i] = inflater.inflate(R.layout.element_eq_select, relativeLayout);
            mPointBack[i] = (ImageView) mPoint[i].findViewById(R.id.point_back);

            DragViewListener listener = new DragViewListener(mPoint[i]);
            listener.setBandIndex(i + 1);
            mPoint[i].setOnTouchListener(listener);
        }

        getPresenter().setCustomType(getArguments());
        mIsFirstDrawn = false;
        mGlobalLayoutListener = () -> {
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mMaskFrame.getWidth()) + ", " +
                    "Height = " + String.valueOf(mMaskFrame.getHeight()));
            if (!mIsFirstDrawn) {
                if(mMaskFrame.getWidth() > 0 && mMaskFrame.getHeight() > 0) {
                    Resources res = getResources();
                    mGraphHeight = mMaskFrame.getHeight();
                    mGraphXStart = res.getDimension(R.dimen.eq_pro_graph_margin_start);
                    mGraphYTop = res.getDimension(R.dimen.eq_pro_graph_margin_top);
                    mGraphXLength = res.getDimension(R.dimen.eq_pro_zoom_scale_interval) * (BAND_DATA_COUNT - 1);
                    mGraphYLength = mGraphHeight - mGraphYTop * 2;
                    mGraphWidth = (int) (mGraphXStart * 2 + mGraphXLength);
                    //mask画像の設定
                    Bitmap maskEqImage = BitmapFactory.decodeResource(res, R.drawable.p0117_maskeq);
                    //mask画像をグラフエリアのサイズにトリミング
                    int maskTop = (int) (maskEqImage.getHeight() * res.getDimension(R.dimen.eq_pro_header_height_px) / res.getDimension(R.dimen.eq_quick_mask_image_height_px));//ヘッダー部分の画面縦サイズに対する比率
                    mBitmapForMask = Bitmap.createBitmap(maskEqImage, 0, maskTop, maskEqImage.getWidth(), maskEqImage.getHeight() - maskTop);
                    Drawable maskResized = new BitmapDrawable(res, mBitmapForMask);
                    mMaskFrame.setMask(maskResized);
                    maskEqImage.recycle();
                    //maskEqImage2.recycle();
                    mGraphView.setLayoutParams(new RelativeLayout.LayoutParams(mGraphWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                    mGraphHorizontalValue.setLayoutParams(new RelativeLayout.LayoutParams(mGraphWidth, RelativeLayout.LayoutParams.MATCH_PARENT));
                    ViewGroup.LayoutParams lp = mEditLine.getLayoutParams();
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                    mlp.setMargins(0, (int) mGraphYTop, 0, (int) mGraphYTop);
                    mEditLine.setLayoutParams(mlp);

                    mGraphView.requestLayout();
                    mGraphView.setSize(mGraphWidth, mGraphHeight);
                    //スプライン曲線用座標データの設定
                    mDataX[0] = 0;
                    mDataX[BAND_DATA_COUNT + 1] = mGraphWidth;
                    mDataY[0] = (double) mGraphHeight / 2;
                    mDataY[BAND_DATA_COUNT + 1] = (double) mGraphHeight / 2;
                    for (int i = 1; i < BAND_DATA_COUNT + 1; i++) {
                        mDataX[i] = mGraphXStart + mGraphXLength / (BAND_DATA_COUNT - 1) * (i - 1);
                        mDataY[i] = mGraphYTop + mGraphYLength / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX - mBands[i - 1]);
                    }
                    mGraphView.setSplineData(mDataX, mDataY);

                    setPointPosition();
                    drawGraphLine();
                    setShrinkScrollView(0);
                    mIsFirstDrawn = true;
                }
            }
            // removeOnGlobalLayoutListener()の削除
            mMaskFrame.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };

        mMaskFrame.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        //横軸ラベルのタッチでのスクロールを無効にする
        mScrollViewValue.setOnTouchListener((v, event) -> true);
        //画面スクロールリスナ
        mScrollView.setOnScrollViewListener((ObservableHorizontalScrollView scrollView, int x, int y, int oldx, int oldy) -> {
            setShrinkScrollView(x);
            mGraphHorizontalValue.scrollTo(x, 0);
            mScrollX = x;
        });

        //グラフ更新リスナ
        mGraphView.setCustomEventListener(() -> {
            getPresenter().onChangeBandValueAction(mBands);
            setShrinkView();
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMaskFrame.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        if(mBitmapForGraph != null) {
            mBitmapForGraph.recycle();
            mBitmapForGraph = null;
        }
        if(mBitmapForDecreaseGraph != null) {
            mBitmapForDecreaseGraph.recycle();
            mBitmapForDecreaseGraph = null;
        }
        if(mBitmapForMask != null){
            mBitmapForMask.recycle();
            mBitmapForMask = null;
        }
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected EqProSettingZoomPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EQ_PRO_SETTING_ZOOM;
    }

    @OnClick(R.id.zoom_button)
    public void onClickZoom() {
        getPresenter().onZoomAction();
    }

    @OnClick(R.id.reset_button)
    public void onClickReset() {
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            mBands[i] = 0;
        }
        for (int i = 1; i < BAND_DATA_COUNT + 1; i++) {
            mDataY[i] = mGraphYTop + mGraphYLength / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX - mBands[i - 1]);
        }
        mGraphView.invalidate();
        setShrinkView();
        setPointPosition();
        getPresenter().onChangeBandValueAction(mBands);
    }

    /**
     * 選択ポイントの位置設定
     */
    private void setPointPosition() {
        float ｓelectEqRound = getResources().getDimension(R.dimen.eq_pro_graph_select_point_width); //Band選択ポイント直径
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            float x = (float) mDataX[i + 1] - ｓelectEqRound / 2;
            float y = (float) mDataY[i + 1] - ｓelectEqRound / 2;
            mPoint[i].setX(x);
            mPoint[i].setY(y);
        }
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
                for (int i = 1; i < BAND_DATA_COUNT + 1; i++) {
                    mDataY[i] = mGraphYTop + mGraphYLength / (EQ_SETTING_RANGE - 1) * (EQ_SETTING_MAX - mBands[i - 1]);
                }
                mGraphView.invalidate();
                setShrinkView();
            }
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mCursorBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0114_draweq, color));
        mEditLineBack.setBackground(ImageViewUtil.setTintColor(getContext(), R.drawable.p0118_editeq, color));
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            mPointBack[i].setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0116_selecteq, color));
        }
    }

    /**
     * 縮小グラフの表示
     */
    private void setShrinkView() {
        if(mGraphWidth > 0 && mGraphHeight > 0) {
            Bitmap bitmap = Bitmap.createBitmap(mGraphWidth, mGraphHeight, Bitmap.Config.ARGB_8888);
            Canvas bitmapHolder = new Canvas(bitmap);
            mGraphView.draw(bitmapHolder);
            Bitmap bitmap2 = Bitmap.createBitmap(bitmap, (int) mGraphXStart, (int) mGraphYTop, (int) mGraphXLength, (int) mGraphYLength);
            Bitmap mBitmapForDecreaseGraph = Bitmap.createScaledBitmap(bitmap2, mShrinkView.getWidth(), mShrinkView.getHeight(), true);

            mShrinkView.setImageBitmap(mBitmapForDecreaseGraph);
            bitmap.recycle();
            bitmap2.recycle();
        }
    }

    /**
     * 縮小グラフのスクロール位置表示
     */
    private void setShrinkScrollView(int x) {
        int width = mShrinkScrollView.getWidth();
        int height = mShrinkScrollView.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, x * ((float) width / mGraphWidth), height, p);
        canvas.drawRect(x * ((float) width / mGraphWidth) + mMaskFrame.getWidth() * ((float) width / mGraphWidth), 0, width, height, p);
        mShrinkScrollView.setImageBitmap(bitmap);
    }

    /**
     * 設定値のテキスト表示
     */
    private void setValueText(int band, int value) {
        CharSequence text;
        if(band > 0 && band <= BAND_DATA_COUNT) {
            if (value > 0) {
                text = STR_EQ_HZ[band - 1] + ": +" + String.valueOf(value);
            } else {
                text = STR_EQ_HZ[band - 1] + ": " + String.valueOf(value);
            }
            TextViewUtil.setTextIfChanged(mTextView, text);
        }
    }

    /**
     * グラフ目盛り線・ラベルの描画
     */
    private void drawGraphLine() {
        Resources res = getResources();
        mBitmapForGraph = Bitmap.createBitmap(mGraphWidth, mGraphHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmapForGraph);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStrokeWidth(res.getDimension(R.dimen.eq_pro_graph_line_stroke_width));
        //縦軸目盛り線を描画
        p.setAlpha(255 * 20 / 100);
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            float x = mGraphXStart + mGraphXLength / (BAND_DATA_COUNT - 1) * i;
            canvas.drawLine(x, mGraphYTop, x, mGraphYTop + mGraphYLength, p);
        }
        mGraphLineVertical.setImageBitmap(mBitmapForGraph);

        Bitmap bitmap2 = Bitmap.createBitmap(mMaskFrame.getWidth(), mGraphHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap2);
        //横軸目盛り線を描画
        p.setAlpha(255 * 40 / 100);
        float extra = res.getDimension(R.dimen.eq_pro_graph_horizontal_line_extra);
        for (int i = 0; i < 5; i++) {
            canvas2.drawLine(mGraphXStart - extra, mGraphYTop + mGraphYLength * i / 4, mMaskFrame.getWidth() - mGraphXStart + extra, mGraphYTop + mGraphYLength * i / 4, p);
        }
        Paint pText = new Paint();
        pText.setColor(Color.WHITE);
        pText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        pText.setTextSize(res.getDimension(R.dimen.eq_pro_graph_label_font_size));
        //縦軸目盛りラベルを描画
        pText.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fontMetrics = pText.getFontMetrics();
        // 中心にしたいY座標からAscentとDescentの半分を引く
        float baseY = (fontMetrics.ascent + fontMetrics.descent) / 2;
        float labelX = res.getDimension(R.dimen.eq_pro_graph_vertical_label_position);
        canvas2.drawText("+12", labelX, mGraphYTop - baseY, pText);
        canvas2.drawText("0", labelX, mGraphYTop + mGraphYLength / 2 - baseY, pText);
        canvas2.drawText("-12", labelX, mGraphYTop + mGraphYLength - baseY, pText);
        mGraphLine.setImageBitmap(bitmap2);
        //横軸目盛りラベルを描画
        Bitmap bitmap3 = Bitmap.createBitmap(mGraphWidth, mGraphHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas3 = new Canvas(bitmap3);
        pText.setTextAlign(Paint.Align.CENTER);
        String[] str = {"20", "40", "80", "160", "315", "630", "1.25k", "2.5k", "5k", "10k", "20k"};
        float labelY = res.getDimension(R.dimen.eq_pro_graph_horizontal_label_position);
        for (int i = 0; i < str.length; i++) {
            float x = mGraphXStart + mGraphXLength / (str.length - 1) * i;
            canvas3.drawText(str[i], x, mGraphYTop + mGraphYLength + labelY, pText);
        }
        //ラベル縦幅でトリミング
        float labelHeight = res.getDimension(R.dimen.eq_pro_graph_label_height);
        Bitmap bitmap4 = Bitmap.createBitmap(bitmap3, 0, (int) (mGraphYTop + mGraphYLength + mGraphYTop - labelHeight), mGraphWidth, (int) labelHeight);

        mGraphHorizontalValue.setImageBitmap(bitmap4);
    }

    private class DragViewListener implements View.OnTouchListener {
        private View mDragView;// ドラッグ対象のView
        // ドラッグ中に移動量を取得するための変数
        private int mOldX;
        private int mOldY;
        private int mBandIndex;//Viewの該当Band(1～31)
        private int mValue;//現在の設定値
        private boolean mFirstMovedFlg;

        public DragViewListener(View dragView) {
            this.mDragView = dragView;
        }

        public void setBandIndex(int index) {
            this.mBandIndex = index;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしている位置取得（スクリーン座標）
            //int x = (int) event.getRawX();
            int y = (int) event.getRawY();
/*            // タッチしている位置取得（相対座標）
            int x1 = (int)event.getX();
            int y1 = (int)event.getY();*/
            // 今回イベントでのView移動先の位置
            //int left = (int)mDragView.getX() + (x - mOldX);
            int top;
            if(mOldY == 0) {
                top = (int) mDragView.getY();
            }else{
                top = (int) mDragView.getY() + (y - mOldY);
            }
            //float positionX = left + mDragView.getWidth() / 2;
            float positionY = top + mDragView.getHeight() / 2;
            //座標から設定値を求める
            mValue = EQ_SETTING_MAX - Math.round((EQ_SETTING_RANGE - 1) * ((positionY - mGraphYTop) / mGraphYLength));
            mCursor.setX((float) mDataX[mBandIndex] - (float) mCursor.getWidth() / 2 - mScrollX);
            mCursor.setY(positionY - (float) mCursor.getHeight() / 2);
            mEditLine.setX((float) mDataX[mBandIndex] - (float) mEditLine.getWidth() / 2 - mScrollX);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //ScrollViewを無効化する
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    mCursor.setVisibility(View.VISIBLE);
                    mEditLine.setVisibility(View.VISIBLE);
                    mFirstMovedFlg = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (positionY >= mGraphYTop && positionY <= mGraphYTop + mGraphYLength) {
                        if (!mFirstMovedFlg) {
                            mCursor.setVisibility(View.VISIBLE);
                            mEditLine.setVisibility(View.VISIBLE);
                            mFirstMovedFlg = true;
                        }
                        // Viewを移動する
                        mDragView.setY(top);
                        setValueText(mBandIndex, mValue);
                        mDataY[mBandIndex] = positionY;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mCursor.setVisibility(View.INVISIBLE);
                    mEditLine.setVisibility(View.INVISIBLE);
                    //設定値から座標を求める
                    positionY = (mGraphYLength / (float) (EQ_SETTING_RANGE - 1)) * (EQ_SETTING_MAX - mValue) + mGraphYTop;
                    mDragView.setY((int) (positionY - (float) mDragView.getHeight() / 2));
                    mDataY[mBandIndex] = positionY;
                    mBands[mBandIndex - 1] = mValue;
                    getPresenter().onChangeBandValueAction(mBands);
                    setShrinkView();
                    break;
                default:
                    //
                    break;
            }

            // 今回のタッチ位置を保持
            //mOldX = x;
            mOldY = y;
            // イベント処理完了
            return true;
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
