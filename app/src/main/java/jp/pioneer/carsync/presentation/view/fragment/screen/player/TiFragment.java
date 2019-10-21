package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.TiPresenter;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.TiView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicatorPreset;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextView;

/**
 * TI再生画面
 */

public class TiFragment extends AbstractRadioFragment<TiPresenter, TiView> implements TiView {
    @Inject TiPresenter mPresenter;
    @BindView(R.id.car_icon) RelativeLayout mCarIcon;
    @BindView(R.id.car_icon_image) ImageView mCarIconImage;
    @BindView(R.id.car_icon_back) ImageView mCarIconBack;
    @BindView(R.id.car_icon_back_error) ImageView mCarIconBackError;
    @BindView(R.id.gesture_layout) CustomGestureLayout mGestureLayout;
    @BindView(R.id.jacket_view) ImageView mJacket;
    @BindView(R.id.gesture_view) FrameLayout mGesture;
    @BindView(R.id.gesture_icon) ImageView mGestureIcon;
    @BindView(R.id.gesture_icon_base) ImageView mGestureIconBase;
    @BindView(R.id.gesture_text) TextView mGestureText;
    @BindView(R.id.band_text) TextView mBand;
    @BindView(R.id.pre_pch_text) TextView mPrePch;
    @BindView(R.id.favorite_view) ImageView mFavorite;
    @BindView(R.id.pty_search_icon) ImageView mPtySearch;
    @BindView(R.id.frequency_text) TextView mFrequency;
    @BindView(R.id.frequency_decimal_text) TextView mFrequencyDecimal;
    @BindView(R.id.frequency_no_decimal_text) TextView mFrequencyNoDecimal;
    @BindView(R.id.frequency_unit_text) TextView mFrequencyUnit;
    @BindView(R.id.pch_text) TextView mPch;
    @BindView(R.id.ps_name_text) ScrollTextView mPsName;
    @BindView(R.id.music_information_text) SwitchTextView mMusicInformation;
    @BindView(R.id.antenna_icon) ImageView mAntenna;
    @BindView(R.id.source_button) RelativeLayout mSourceBtn;
    @BindView(R.id.source_button_icon) ImageView mSourceBtnIcon;
    @BindView(R.id.list_button) RelativeLayout mListBtn;
    @BindView(R.id.visualizer_button) ConstraintLayout mVisualizerBtn;
    @BindView(R.id.fx_button) ConstraintLayout mFxBtn;
    @BindView(R.id.fx_text) TextView mFxText;
    @BindView(R.id.vis_text) TextView mVisText;
    @BindView(R.id.info_text) LinearLayout mInfoText;
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    private ViewGroup mViewGroup;
    private View mView;
    private ViewPager mViewPager;
    private CustomLinePageIndicatorPreset mLineIndicator;
    private RelativeLayout mShortCutGroup;
    private Unbinder mUnbinder;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private final Handler mHandler = new Handler();
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
            alphaFadeout.setDuration(500);
            alphaFadeout.setFillAfter(true);
            mGesture.startAnimation(alphaFadeout);
        }
    };
    private Runnable mDelayMessageFunc = new Runnable() {
        @Override
        public void run() {
            mFxEqMessage.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * コンストラクタ
     */
    public TiFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioFragment
     */
    public static TiFragment newInstance(Bundle args) {
        TiFragment fragment = new TiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_radio, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mViewGroup =container;
        mView = view;

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mDelayGestureFunc);
        mHandler.removeCallbacks(mDelayMessageFunc);
        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mViewGroup.removeAllViews();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.TI;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected TiPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onClickJacketView() {
        mHandler.removeCallbacks(mDelayGestureFunc);
    }

    @Override
    public void onClickFavoriteButton() {}

    @Override
    public void onClickHomeButton() {
        getPresenter().onHomeAction();
    }

    @Override
    public void onClickSettingButton() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SettingsContainerFragment.KEY_RETURN_SCREEN_WHEN_CLOSE, getScreenId());
        getPresenter().onSettingsAction(bundle);
    }

    @Override
    public void onClickSourceButton() {
        getPresenter().onSelectSourceAction();
    }

    @Override
    public void onClickVisualizerButton() {
        getPresenter().onSelectVisualAction();
    }

    @Override
    public void onClickFxButton() {
        getPresenter().onSelectFxAction();
    }

    @Override
    public void setFrequency(String frequency) {
        //frequency="1620kHz";
        if (frequency != null) {
            if (frequency.indexOf(".") > 0) {
                TextViewUtil.setMarqueeTextIfChanged(mFrequency, frequency.substring(0, frequency.indexOf(".")));
                TextViewUtil.setMarqueeTextIfChanged(mFrequencyDecimal, frequency.substring(frequency.indexOf("."), frequency.length() - 3));
                TextViewUtil.setMarqueeTextIfChanged(mFrequencyUnit, frequency.substring(frequency.length() - 3));
                mFrequency.setVisibility(View.VISIBLE);
                mFrequencyDecimal.setVisibility(View.VISIBLE);
                mFrequencyNoDecimal.setVisibility(View.GONE);
            } else {
                TextViewUtil.setMarqueeTextIfChanged(mFrequencyNoDecimal, frequency.substring(0, frequency.length() - 3));
                TextViewUtil.setMarqueeTextIfChanged(mFrequencyUnit, frequency.substring(frequency.length() - 3));
                mFrequency.setVisibility(View.GONE);
                mFrequencyDecimal.setVisibility(View.GONE);
                mFrequencyNoDecimal.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * アンテナレベルの設定
     *
     * @param level アンテナレベル
     */
    @Override
    public void setAntennaLevel(float level) {
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        int antennaId = ANTENNA_LIST.get(levelInt, -1);
        if(antennaId >= 0){
            mAntenna.setImageResource(antennaId);
        }
    }

    @Override
    public void showGesture(GestureType type) {
        if(type.isDisplayText()){
            displayGestureText(type.ids.get(0));
        }

        if(type.isDisplayImg()){
            displayGesture(type.ids.get(0), type.ids.get(1));
        }
    }

    private void showStatus(GestureType type) {
        if(type.isDisplayText()){
            displayGestureText(type.ids.get(0));
        }
    }

    private void displayGestureText(@StringRes int textId){
        mHandler.removeCallbacks(mDelayGestureFunc);
        mGestureText.setText(textId);
        mGestureIconBase.setVisibility(View.INVISIBLE);
        mGestureIcon.setVisibility(View.INVISIBLE);
        mGestureText.setVisibility(View.VISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
        mHandler.postDelayed(mDelayGestureFunc, GESTURE_DELAY_TIME);
    }

    private void displayGesture(@DrawableRes int iconBaseId, @DrawableRes int iconId){
        mHandler.removeCallbacks(mDelayGestureFunc);
        mGestureIconBase.setImageResource(iconBaseId);
        mGestureIcon.setImageResource(iconId);
        mGestureIconBase.setVisibility(View.VISIBLE);
        mGestureIcon.setVisibility(View.VISIBLE);
        mGestureText.setVisibility(View.INVISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
        mHandler.postDelayed(mDelayGestureFunc, GESTURE_DELAY_TIME);
    }

    // MARK - EQ FX

    @Override
    public void setEqButton(String str){
        mVisText.setText(str);
    }

    @Override
    public void setFxButton(String str){
        mFxText.setText(str);
    }

    @Override
    public void setEqFxButtonEnabled(boolean eqEnabled, boolean fxEnabled){
        mVisualizerBtn.setEnabled(eqEnabled);
        mFxBtn.setEnabled(fxEnabled);
        if(eqEnabled){
            mVisualizerBtn.setAlpha(ENABLE_ALPHA);
        }else{
            mVisualizerBtn.setAlpha(DISABLE_ALPHA);
        }
        if(fxEnabled){
            mFxBtn.setAlpha(ENABLE_ALPHA);
        }else{
            mFxBtn.setAlpha(DISABLE_ALPHA);
        }
    }

    @Override
    public void displayEqFxMessage(String str) {
        mHandler.removeCallbacks(mDelayMessageFunc);
        mFxEqMessageText.setText(str);
        mFxEqMessage.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mDelayMessageFunc, MESSAGE_DELAY_TIME);
    }

    @Override
    public void setAdasEnabled(boolean isEnabled) {
        if(isEnabled){
            mCarIcon.setVisibility(View.VISIBLE);
        }else{
            mCarIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setAdasIcon(int status) {
        switch (status){
            case 0:
                mCarIconBack.setAlpha(0.0f);
                mCarIconBackError.setAlpha(0.0f);
                mCarIconImage.setImageResource(R.drawable.p0103_icon);
                break;
            case 1:
                mCarIconBack.setAlpha(1.0f);
                mCarIconBackError.setAlpha(0.0f);
                mCarIconImage.setImageResource(R.drawable.p0103_icon);
                break;
            case 2:
                mCarIconBack.setAlpha(0.0f);
                mCarIconBackError.setAlpha(1.0f);
                mCarIconImage.setImageResource(R.drawable.p0103_icon_error);
                break;
            default:
                mCarIconBack.setAlpha(0.0f);
                mCarIconBackError.setAlpha(0.0f);
                mCarIconImage.setImageResource(R.drawable.p0103_icon);
                break;
        }
    }

    /**
     * Adas Icon押下イベント
     */
    @OnClick(R.id.car_icon)
    public void onClickAdasIcon() {
        getPresenter().onAdasErrorAction();
    }
    /**
     * ShortCutKeyの設定
     *
     * @param keys ShortCutKeys
     */
    @Override
    public void setShortcutKeyItems(ArrayList<ShortcutKeyItem> keys) {
        if(mShortcutKeyVisible) {
            int cur = mViewPager.getCurrentItem();
            mAdapter.setShortcutKeysItems(keys);
            mLineIndicator.setCurrentItem(Math.min(cur, mAdapter.getCount()));
            mLineIndicator.setVisibility(mAdapter.getCount() <= 1 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void setShortCutButtonEnabled(boolean enabled) {
        mShortcutKeyVisible = enabled;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mViewGroup.removeAllViews();
        if(enabled){
            mView = inflater.inflate(R.layout.fragment_player_radio_bar, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
            mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
            mLineIndicator = (CustomLinePageIndicatorPreset) mView.findViewById(R.id.line_indicator);
            mShortCutGroup = (RelativeLayout) mView.findViewById(R.id.shortcut_group);
            mAdapter = new ShortcutKeyAdapter(getActivity()) {
                @Override
                protected void onClickKey(ShortcutKey shortCutKey) {
                    getPresenter().onKeyAction(shortCutKey);
                }
                @Override
                protected void onLongClickKey(ShortcutKey shortCutKey) {
                    getPresenter().onLongKeyAction(shortCutKey);
                }
            };
            mViewPager.setAdapter(mAdapter);
            mLineIndicator.setViewPager(mViewPager);
            // 単一ページの時はインジケータ非表示
            mLineIndicator.setVisibility(mAdapter.getCount() <= 1 ? View.INVISIBLE : View.VISIBLE);
            mShortCutGroup.setVisibility(View.VISIBLE);
        }else{
            mView = inflater.inflate(R.layout.fragment_player_radio, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
        }
        setLayout();
    }

    private void setLayout(){
        mGestureLayout.setOnSeekGestureListener(new CustomGestureLayout.OnGestureListener() {
            @Override
            public void onStartSeek(MotionEvent ev) {
                // no action
            }

            @Override
            public void onSeek(MotionEvent ev) {
                // no action
            }

            @Override
            public void onEndSeek() {
                // no action
            }

            @Override
            public void onSwipeUp() {
                getPresenter().onVolumeUpAction();
            }

            @Override
            public void onSwipeDown() {
                getPresenter().onVolumeDownAction();
            }

            @Override
            public void onSwipeLeft() {
                getPresenter().onNextChannelAction();
            }

            @Override
            public void onSwipeRight() {
                getPresenter().onPreviousChannelAction();
            }
        });
        mPrePch.setVisibility(View.GONE);
        mPch.setVisibility(View.GONE);
        mBand.setVisibility(View.GONE);
        mFavorite.setAlpha(DISABLE_ALPHA);
        mFavorite.setClickable(false);
        mPtySearch.setAlpha(DISABLE_ALPHA);
        mPtySearch.setClickable(false);
        mListBtn.setAlpha(DISABLE_ALPHA);
        mListBtn.setEnabled(false);
        mMusicInformation.setVisibility(View.GONE);
        mPsName.setText(R.string.ply_040);
        mInfoText.setVisibility(View.GONE);
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        mGlobalLayoutListener = () -> {
            mPsName.startScroll();
            mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        mView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    @Override
    public void setAlexaNotification(boolean notification) {
        if(mAdapter!=null) {
            mAdapter.setNotification(notification);
        }
    }
}
