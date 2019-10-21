package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.AuxPresenter;
import jp.pioneer.carsync.presentation.view.AuxView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;

/**
 * AUX再生の画面
 */
public class AuxFragment extends AbstractScreenFragment<AuxPresenter, AuxView> implements AuxView {
    @Inject AuxPresenter mPresenter;
    private static final int GESTURE_DELAY_TIME = 500;
    private static final int MESSAGE_DELAY_TIME = 1500;
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
    @BindView(R.id.source_button) RelativeLayout mSourceBtn;
    @BindView(R.id.source_button_icon) ImageView mSourceBtnIcon;
    @BindView(R.id.list_button) RelativeLayout mListBtn;
    @BindView(R.id.visualizer_button) ConstraintLayout mVisualizerBtn;
    @BindView(R.id.fx_button) ConstraintLayout mFxBtn;
    @BindView(R.id.fx_text) TextView mFxText;
    @BindView(R.id.vis_text) TextView mVisText;
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    @Nullable @BindView(R.id.currentTimeView) TextView mCurrentTimeView;
    @Nullable @BindView(R.id.remainingTimeView) TextView mRemainingTimeView;
    private ViewGroup mViewGroup;
    private View mView;
    private ViewPager mViewPager;
    private CustomLinePageIndicator mLineIndicator;
    private RelativeLayout mShortCutGroup;
    private ProgressBar mProgressAlexa;
    private Unbinder mUnbinder;
    private final Handler mHandler = new Handler();
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private int mOrientation;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            mGesture.setVisibility(View.INVISIBLE);
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
    public AuxFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AuxFragment
     */
    public static AuxFragment newInstance(Bundle args) {
        AuxFragment fragment = new AuxFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_no_info, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mViewGroup =container;
        mView = view;
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        setLayout();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mDelayGestureFunc);
        mHandler.removeCallbacks(mDelayMessageFunc);
        mViewGroup.removeAllViews();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AuxPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.AUX;
    }

    @Override
    public void showGesture(GestureType type) {
        if(type.isDisplayImg()){
            displayGesture(type.ids.get(0), type.ids.get(1));
        }
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
    @OnClick(R.id.jacket_view)
    public void onClickJacketView() {
        /*
         * 動作はないが、親Viewのジェスチャを動かすために、
         * 当メソッドが必要
         */
    }

    /**
     * 左ボタン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.source_button)
    public void onClickLeft(View view) {
        getPresenter().onSelectSourceAction();
    }

    /**
     * HOMEボタン押下イベント
     */
    @OnClick(R.id.home_button)
    public void onClickHomeButton() {
        getPresenter().onHomeAction();
    }

    /**
     * 設定ボタン押下イベント
     */
    @OnClick(R.id.player_setting_button)
    public void onClickSettingButton() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SettingsContainerFragment.KEY_RETURN_SCREEN_WHEN_CLOSE, getScreenId());
        getPresenter().onSettingsAction(bundle);
    }
    @OnClick(R.id.visualizer_button)
    public void onClickVisualizerButton(){
        getPresenter().onSelectVisualAction();
    }

    @OnClick(R.id.fx_button)
    public void onClickFxButton(){
        getPresenter().onSelectFxAction();
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        // ??
    }

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
            mVisualizerBtn.setAlpha(1.0f);
        }else{
            mVisualizerBtn.setAlpha(0.4f);
        }
        if(fxEnabled){
            mFxBtn.setAlpha(1.0f);
        }else{
            mFxBtn.setAlpha(0.4f);
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
            mView = inflater.inflate(R.layout.fragment_player_no_info_alexa, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
            mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
            mLineIndicator = (CustomLinePageIndicator) mView.findViewById(R.id.line_indicator);
            mShortCutGroup = (RelativeLayout) mView.findViewById(R.id.shortcut_group);
            mProgressAlexa = (ProgressBar) mView.findViewById(R.id.progressbar_alexa);
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
            if(mOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                mProgressAlexa = (ProgressBar) mView.findViewById(R.id.progressbar_alexa);
                mProgressAlexa.setVisibility(View.INVISIBLE);
            }
        }else{
            mView = inflater.inflate(R.layout.fragment_player_no_info, mViewGroup);
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
                // no action
            }

            @Override
            public void onSwipeRight() {
                // no action
            }
        });

        mJacket.setImageResource(R.drawable.p0276_sourceimg);
        mListBtn.setAlpha(0.4f);
        if(mCurrentTimeView!=null) {
            mCurrentTimeView.setVisibility(View.GONE);
        }
        if(mRemainingTimeView!=null) {
            mRemainingTimeView.setVisibility(View.GONE);
        }
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
    }

    @Override
    public void setAlexaNotification(boolean notification) {
        if(mAdapter!=null) {
            mAdapter.setNotification(notification);
        }
    }
}
