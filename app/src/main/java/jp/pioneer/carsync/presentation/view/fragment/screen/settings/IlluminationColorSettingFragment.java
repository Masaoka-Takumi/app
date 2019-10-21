package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.annimon.stream.Optional;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.IlluminationColorSettingPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.IlluminationColorSettingView;
import jp.pioneer.carsync.presentation.view.adapter.IllumiColorAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * イルミネーションカラー設定画面
 * <p>
 * プリセットカラー選択とカスタムカラー選択を本画面にて設定する。
 */

public class IlluminationColorSettingFragment extends AbstractScreenFragment<IlluminationColorSettingPresenter, IlluminationColorSettingView>
        implements IlluminationColorSettingView {

    @Inject IlluminationColorSettingPresenter mPresenter;
    @BindView(R.id.color_list) RecyclerView mColor;
    @BindView(R.id.illumi_frame) ImageView mCustomFrame;
    @BindView(R.id.illumi_frame_light) ImageView mCustomFrameLight;
    @BindView(R.id.illumi_select) ImageView mCustomSelect;
    @BindView(R.id.illumi_select_light) ImageView mCustomSelectLight;
    @BindView(R.id.target_point) RelativeLayout mTarget;
    @BindView(R.id.point_light) ImageView mCustomPointLight;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private IllumiColorAdapter mItemAdapter;
    private Bitmap mColorPalette;
    private Unbinder mUnbinder;
    private int mOrientation;
    /**
     * コンストラクタ
     */
    public IlluminationColorSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return IlluminationColorSettingFragment
     */
    public static IlluminationColorSettingFragment newInstance(Bundle args) {
        IlluminationColorSettingFragment fragment = new IlluminationColorSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_illumi_color, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getContext().getResources().getConfiguration();
        mOrientation = config.orientation;

        getPresenter().setArgument(getArguments());

        // RecyclerViewの横表示を実現するために、LinearLayoutManagerを使い設定を行う。
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mColor.setLayoutManager(manager);
        mColor.setOverScrollMode(View.OVER_SCROLL_NEVER);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ILLUMINATION_COLOR_SETTING;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected IlluminationColorSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setColor(List<Integer> colors) {
        mItemAdapter = new IllumiColorAdapter(getContext(), colors, (v, position) -> getPresenter().onSelectColorItemAction(position));
        mColor.setAdapter(mItemAdapter);
    }

    @Override
    public void setPosition(int position) {
        mItemAdapter.setPosition(position);
        mItemAdapter.notifyDataSetChanged();
        if (position == -1) {
            mCustomSelect.setVisibility(View.VISIBLE);
            mCustomSelectLight.setVisibility(View.VISIBLE);
        } else {
            mCustomSelect.setVisibility(View.GONE);
            mCustomSelectLight.setVisibility(View.GONE);
        }
    }

    @Override
    public void setCustomColor(int red, int green, int blue) {
        mCustomFrame.setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0033_illcolorselectbtn_1nrm, red, green, blue));
        mCustomFrameLight.setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0034_illcolorselectbtn_1nrm, red, green, blue));
        mCustomSelectLight.setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0036_illcolorbtn_1nrm, red, green, blue));
        mCustomPointLight.setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0038_illcolorbtn_1nrm, red, green, blue));
    }

    /**
     * パレットタッチイベント
     *
     * @param v     パレット
     * @param event MotionEvent
     * @return 処理結果
     */
    @OnTouch(R.id.color_palette)
    public boolean onTouchColorPalette(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Optional.ofNullable(mTarget.getAnimation()).ifPresent(Animation::cancel);
            mTarget.setVisibility(View.VISIBLE);
            // リソースの取得
            if(mOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                mColorPalette = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.p0180_colorpalet);
            }else{
                mColorPalette = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.p0181_colorpalet);
            }
        }

        float orgWidth = mColorPalette.getWidth();
        float orgHeight = mColorPalette.getHeight();
        float orgX = checkLimitPosition((event.getX() / v.getWidth()) * orgWidth, orgWidth);
        float orgY = checkLimitPosition((event.getY() / v.getHeight()) * orgHeight, orgHeight);
        int px = (int) checkLimitPosition(event.getX(), v.getWidth()) - (mTarget.getWidth() / 2);
        int py = (int) checkLimitPosition(event.getY(), v.getHeight()) - (mTarget.getHeight() / 2);
        int color = mColorPalette.getPixel((int) orgX, (int) orgY);

        // ポインターの移動
        mTarget.setTranslationX(px);
        mTarget.setTranslationY(py);

        // 色の変更
        getPresenter().onCustomColorAction(Color.red(color), Color.green(color), Color.blue(color));

        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            // リソースの解放
            mColorPalette.recycle();
            mColorPalette = null;

            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mTarget.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mTarget.startAnimation(anim);
        }
        return true;
    }

    /**
     * カスタムカラー押下イベント
     */
    @OnClick(R.id.custom_item)
    public void onClickCustomItem() {
        getPresenter().onSelectCustomItemAction();
    }

    private float checkLimitPosition(float curr, float max) {
        if (curr <= 0.0f) {
            return 1.0f;
        } else if (curr >= max) {
            return max - 2.0f;
        }
        return curr;
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
