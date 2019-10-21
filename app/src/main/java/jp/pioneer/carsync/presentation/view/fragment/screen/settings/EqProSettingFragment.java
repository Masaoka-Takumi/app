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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.christophesmet.android.views.maskableframelayout.MaskableFrameLayout;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.EqProSettingPresenter;
import jp.pioneer.carsync.presentation.view.EqProSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.EqProSettingDrawView;
import jp.pioneer.carsync.presentation.view.widget.EqProSettingGraphView;
import timber.log.Timber;

/**
 * EQ Pro Setting画面
 */

public class EqProSettingFragment extends AbstractScreenFragment<EqProSettingPresenter, EqProSettingView> implements EqProSettingView {
    @Inject EqProSettingPresenter mPresenter;
    @BindView(R.id.graph_layout) RelativeLayout mGraphLayout;
    @BindView(R.id.frm_mask) MaskableFrameLayout mMaskFrame;
    @BindView(R.id.shrink_image) ImageView mShrinkView;
    @BindView(R.id.draw_view) EqProSettingDrawView mDrawView;
    @BindView(R.id.graph_view) EqProSettingGraphView mGraphView;
    @BindView(R.id.graph_line) ImageView mGraphLine;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private Unbinder mUnbinder;
    private static final int EQ_SETTING_RANGE = 25; //EQ設定値の個数
    private static final int EQ_SETTING_MAX = 12; //EQ設定値の最大値
    private static final int EQ_SETTING_MIN = -12; //EQ設定値の最小値
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private static final int SPLINE_DATA_COUNT = 31; //スプライン曲線のポイント数

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private int mGraphWidth; //グラフエリアの横幅
    private int mGraphHeight; //グラフエリアの縦幅
    private float mGraphXLength; //グラフのX座標幅
    private float mGraphYLength; //グラフのY座標幅
    private float mGraphYTop; //グラフのY座標開始位置
    private float mGraphXStart; //グラフのX座標開始位置
    private boolean mIsFirstDrawn = false;
    private Bitmap mBitmapForGraph;
    private Bitmap mBitmapForDecreaseGraph;
    private Bitmap mBitmapForMask;

    public EqProSettingFragment() {

    }

    public static EqProSettingFragment newInstance(Bundle args) {
        EqProSettingFragment fragment = new EqProSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_eq_pro, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setCustomType(getArguments());
        mIsFirstDrawn = false;
        mGlobalLayoutListener = () -> {
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mMaskFrame.getWidth()) + ", " +
                    "Height = " + String.valueOf(mMaskFrame.getHeight()));
            if (!mIsFirstDrawn) {
                if(mMaskFrame.getWidth() > 0 && mMaskFrame.getHeight() > 0) {
                    Resources res = getResources();
                    mGraphWidth = mMaskFrame.getWidth();
                    mGraphHeight = mMaskFrame.getHeight();
                    mGraphXStart = res.getDimension(R.dimen.eq_pro_graph_margin_start);
                    mGraphYTop = res.getDimension(R.dimen.eq_pro_graph_margin_top);

                    mGraphXLength = mGraphWidth - mGraphXStart * 2;
                    mGraphYLength = mGraphHeight - mGraphYTop * 2;
                    //mask画像の設定
                    Bitmap maskEqImage = BitmapFactory.decodeResource(res, R.drawable.p0115_maskeq);
                    //mask画像をグラフエリアのサイズにトリミング
                    int maskTop = (int) (maskEqImage.getHeight() * res.getDimension(R.dimen.eq_pro_header_height_px) / res.getDimension(R.dimen.eq_quick_mask_image_height_px));//ヘッダー部分の画面縦サイズに対する比率
                    mBitmapForMask = Bitmap.createBitmap(maskEqImage, 0, maskTop, maskEqImage.getWidth(), maskEqImage.getHeight() - maskTop);
                    Drawable maskResized = new BitmapDrawable(res, mBitmapForMask);
                    mMaskFrame.setMask(maskResized);
                    maskEqImage.recycle();
                    mGraphView.setSize(mGraphWidth, mGraphHeight);
                    mDrawView.setSize(mGraphWidth, mGraphHeight);
                    mDrawView.setDataX(mGraphView.getDataX());
                    drawGraphLine();
                    setShrinkView();
                    mIsFirstDrawn = true;
                }
            }
            // removeOnGlobalLayoutListener()の削除
            mMaskFrame.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        mMaskFrame.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        //Drawing終了リスナ
        mDrawView.setCustomEventListener(() -> mGraphView.setDataY(mDrawView.getDataY()));
        //グラフ更新リスナ
        mGraphView.setCustomEventListener(() -> {
            getPresenter().onChangeBandValueAction(mGraphView.getBands());
            setShrinkView();
        });
        mDrawView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
    protected EqProSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EQ_PRO_SETTING;
    }

    @OnClick(R.id.zoom_button)
    public void onClickZoom() {
        getPresenter().onZoomAction();
    }

    @OnClick(R.id.reset_button)
    public void onClickReset() {
        float[] bands = new float[BAND_DATA_COUNT];
        Arrays.fill(bands, 0);
        mGraphView.setBandData(bands);
    }

    /**
     * Band値のSplineデータへの反映
     *
     * @param bands 31Bandの配列
     */
    @Override
    public void setBandData(@Size(31) float[] bands) {
        mGraphView.setBandData(bands);
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mDrawView.setUIColor(color);
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
        //横軸目盛り線を描画
        p.setAlpha(255 * 40 / 100);
        float extra = res.getDimension(R.dimen.eq_pro_graph_horizontal_line_extra);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(mGraphXStart - extra, mGraphYTop + mGraphYLength * i / 4, mGraphXStart + mGraphXLength + extra, mGraphYTop + mGraphYLength * i / 4, p);
        }
        //縦軸目盛り線を描画
        p.setAlpha(255 * 20 / 100);
        for (int i = 0; i < BAND_DATA_COUNT; i++) {
            float x = mGraphXStart + mGraphXLength / (BAND_DATA_COUNT - 1) * i;
            canvas.drawLine(x, mGraphYTop, x, mGraphYTop + mGraphYLength, p);
        }
        Paint pText = new Paint();
        pText.setColor(Color.WHITE);
        pText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        //縦軸目盛りラベルを描画
        pText.setTextSize(res.getDimension(R.dimen.eq_pro_graph_label_font_size));
        pText.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fontMetrics = pText.getFontMetrics();
        // 中心にしたいY座標からAscentとDescentの半分を引く
        float baseY = (fontMetrics.ascent + fontMetrics.descent) / 2;
        float labelX = res.getDimension(R.dimen.eq_pro_graph_vertical_label_position);
        canvas.drawText("+12", labelX, mGraphYTop - baseY, pText);
        canvas.drawText("0", labelX, mGraphYTop + mGraphYLength / 2 - baseY, pText);
        canvas.drawText("-12", labelX, mGraphYTop + mGraphYLength - baseY, pText);
        //横軸目盛りラベルを描画
        pText.setTextAlign(Paint.Align.CENTER);
        String[] str = {"20", "80", "125", "330", "800", "1.25K", "5K", "8K", "20K"};
        float labelY = res.getDimension(R.dimen.eq_pro_graph_horizontal_label_position);
        for (int i = 0; i < str.length; i++) {
            float x = mGraphXStart + mGraphXLength / (str.length - 1) * i;
            canvas.drawText(str[i], x, mGraphYTop + mGraphYLength + labelY, pText);
        }
        mGraphLine.setImageBitmap(mBitmapForGraph);
    }

    /**
     * 縮小グラフの表示
     */
    private void setShrinkView() {
        if(mGraphWidth > 0 && mGraphHeight > 0) {
            Bitmap bitmap = Bitmap.createBitmap(mGraphWidth, mGraphHeight, Bitmap.Config.ARGB_8888);
            Canvas bitmapHolder = new Canvas(bitmap);
            mGraphView.draw(bitmapHolder);
            //グラフ範囲にトリミング
            Bitmap bitmap2 = Bitmap.createBitmap(bitmap, (int) mGraphXStart, (int) mGraphYTop, (int) mGraphXLength, (int) mGraphYLength);
            mBitmapForDecreaseGraph = Bitmap.createScaledBitmap(bitmap2, mShrinkView.getWidth(), mShrinkView.getHeight(), true);

            mShrinkView.setImageBitmap(mBitmapForDecreaseGraph);
            bitmap.recycle();
            bitmap2.recycle();
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
