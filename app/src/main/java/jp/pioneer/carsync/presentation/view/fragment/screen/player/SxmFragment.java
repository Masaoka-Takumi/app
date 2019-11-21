package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
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
import butterknife.OnLongClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.SxmPresenter;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.SxmView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.BlurProgressBar;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextViewAutofit;

/**
 * SiriusXM再生画面
 * <p>
 * ReplayモードとChannelモードがあり、当画面にて切換を行う。
 */

public class SxmFragment extends AbstractRadioFragment<SxmPresenter, SxmView> implements SxmView {
    @Inject SxmPresenter mPresenter;
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
    @BindView(R.id.pch_text) TextView mPch;
    @BindView(R.id.favorite_view) ImageView mFavorite;
    @BindView(R.id.channel_number_text) TextView mChannelNumber;
    @BindView(R.id.channel_name_text) TextView mChannelName;
    @BindView(R.id.music_information_text) SwitchTextViewAutofit mMusicInformation;
    @BindView(R.id.left_center_group) ConstraintLayout mLeftCenterGroup;
    @BindView(R.id.right_center_group) ConstraintLayout mRightCenterGroup;
    @BindView(R.id.replay_button) FrameLayout mReplay;
    @BindView(R.id.tune_mix_button) FrameLayout mTuneMix;
    @BindView(R.id.tune_mix_button_image) ImageView mTuneMixImage;
    @BindView(R.id.live_button) FrameLayout mLive;
    @BindView(R.id.channel_button) FrameLayout mChannel;
    @BindView(R.id.antenna_icon) ImageView mAntenna;
    @BindView(R.id.sxm_icon) ImageView mSxmIcon;
    @BindView(R.id.source_button) RelativeLayout mSourceBtn;
    @BindView(R.id.source_button_icon) ImageView mSourceBtnIcon;
    @BindView(R.id.list_button) RelativeLayout mListBtn;
    @BindView(R.id.visualizer_button) ConstraintLayout mVisualizerBtn;
    @BindView(R.id.fx_button) ConstraintLayout mFxBtn;
    @BindView(R.id.progressbar) BlurProgressBar mProgress;
    @BindView(R.id.fx_text) TextView mFxText;
    @BindView(R.id.vis_text) TextView mVisText;
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    private boolean mFirstFlg = false;
    private int mDisplayCount = 0;
    private String mTitle = "";
    private String mArtist = "";
    private String mCategory = "";
    private ArrayList<String> mStringArrayList = new ArrayList<String>();
    private AlertDialog mSubscription;
    private ViewGroup mViewGroup;
    private View mView;
    private ViewPager mViewPager;
    private CustomLinePageIndicator mLineIndicator;
    private RelativeLayout mShortCutGroup;
    private ProgressBar mProgressAlexa;
    private Unbinder mUnbinder;
    private int mColor;
    private final Handler mHandler = new Handler();
    private PlaybackMode mCurrentPlayBackMode;
    private boolean isPosted;
    private boolean mInTuneMix;
    private boolean mIsReplayMode;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private boolean mIsJacketLongClick;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            if (mCurrentPlayBackMode == PlaybackMode.PAUSE) {
                showStatus(GestureType.PAUSE);
            } else {
                AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
                alphaFadeout.setDuration(500);
                alphaFadeout.setFillAfter(true);
                mGesture.startAnimation(alphaFadeout);
            }
            isPosted = false;
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
    public SxmFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SxmFragment
     */
    public static SxmFragment newInstance(Bundle args) {
        SxmFragment fragment = new SxmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * リストボタン押下イベント
     */
    @OnClick(R.id.list_button)
    public void onClickListButton() {
        getPresenter().onSelectListAction();
    }

    /**
     * リピートボタン押下イベント
     * <p>
     * ChannelModeの時のみ押下可能
     */
    @OnClick(R.id.replay_button)
    public void onClickReplayButton() {
        getPresenter().onReplayAction();
    }

    /**
     * ジャケット長押しイベント
     */
    @OnLongClick(R.id.jacket_view)
    public boolean onLongClickJacket() {
        mIsJacketLongClick = true;
        return false;
    }

    @OnTouch(R.id.jacket_view)
    public boolean OnTouchJacket(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsJacketLongClick = false;
        }else if(event.getAction() == MotionEvent.ACTION_UP){
			//離し時点で長押し判定
            if(mIsJacketLongClick){
                getPresenter().onLongClickJacketAction();
                return true;
            }
        }
        return false;
    }

    /**
     * TuneMixボタン押下イベント
     * <p>
     * ChannelModeの時のみ押下可能
     */
    @OnClick(R.id.tune_mix_button)
    public void onClickTuneMixButton() {
        getPresenter().onTuneMixAction();
    }

    /**
     * Liveボタン押下イベント
     * <p>
     * ReplayModeの時のみ押下可能
     */
    @OnClick(R.id.live_button)
    public void onClickLiveButton() {
        getPresenter().onLiveAction();
    }

    /**
     * Channelボタン押下イベント
     * <p>
     * ReplayModeの時のみ押下可能
     */
    @OnClick(R.id.channel_button)
    public void onClickChannelButton() {
        getPresenter().onChannelAction();
    }

    /**
     * バンド押下イベント.
     */
    @OnClick(R.id.band_text)
    public void onClickBand() {
        getPresenter().onToggleBandAction();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_sirius_xm, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mViewGroup =container;
        mView = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisplayCount = mMusicInformation.getDisplayCount();
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
        return ScreenId.SIRIUS_XM;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SxmPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onClickJacketView() {
        getPresenter().onClickJacketAction();
    }

    @Override
    public void onClickFavoriteButton() {
        if(mIsReplayMode){
            getPresenter().onLiveAction();
        }else {
            getPresenter().onFavoriteAction();
        }
    }

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
    public void setChannelName(String channel) {
        TextViewUtil.setMarqueeTextIfChanged(mChannelName, channel);
    }

    @Override
    public void setMusicInfo(String title,String artist,String category) {
        if (title == null) {
            title = "";
        }
        if (artist == null) {
            artist = "";
        }
        if (category == null) {
            category = "";
        }
        if (mTitle.equals(title)&&mArtist.equals(artist)&&mCategory.equals(category)) {
            return;
        }else{
            mTitle = title;
            mArtist = artist;
            mCategory = category;
        }
        mStringArrayList = new ArrayList<>();
        if(!mArtist.equals("")) {
            mStringArrayList.add(mArtist);
        }
        if(!mTitle.equals("")) {
            mStringArrayList.add(mTitle);
        }
        if(!mCategory.equals("")) {
            mStringArrayList.add(mCategory);
        }
        mMusicInformation.setStringArrayList(mStringArrayList);
    }

    @Override
    public void setPch(int pch) {
        if(pch == -1){
            mPrePch.setVisibility(View.INVISIBLE);
            mPch.setText(null);
        }else{
            mPrePch.setVisibility(View.VISIBLE);
            TextViewUtil.setMarqueeTextIfChanged(mPch, String.valueOf(pch));
        }
    }

    @Override
    public void setChannelNumber(String channelNumber) {
        TextViewUtil.setMarqueeTextIfChanged(mChannelNumber, channelNumber);
    }

    @Override
    public void setFavorite(boolean isFavorite) {
        if(!mIsReplayMode) {
            if (isFavorite) {
                mFavorite.setImageResource(R.drawable.button_favorite_on);
            } else {
                mFavorite.setImageResource(R.drawable.button_favorite_off);
            }
        }
    }

    @Override
    public void setBand(String band) {
        TextViewUtil.setMarqueeTextIfChanged(mBand, band);
    }

    @Override
    public void setReplayMode(boolean isReplayMode) {
        mIsReplayMode = isReplayMode;
        // ReplayMode
        mChannel.setVisibility(isReplayMode ? View.VISIBLE : View.GONE);
        updateProgressBarVisibility();
        // ChannelMode
        mReplay.setVisibility(isReplayMode ? View.GONE : View.VISIBLE);
        if(isReplayMode){
            mFavorite.setImageResource(R.drawable.button_sxm_live);
        }
    }

    /**
     * プログレスバーの表示/非標示を切り替える
     */
    private void updateProgressBarVisibility() {
        mProgress.setVisibility((!mShortcutKeyVisible&&mIsReplayMode && !mInTuneMix) ? View.VISIBLE : View.INVISIBLE);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setVisibility((mShortcutKeyVisible && mIsReplayMode && !mInTuneMix) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void setAntennaLevel(float level) {
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        int antennaId = ANTENNA_LIST.get(levelInt, -1);
        if (antennaId >= 0) {
            mAntenna.setImageResource(antennaId);
        }
    }

    @Override
    public void setSxmIcon(boolean visible) {
        if (visible) {
            mSxmIcon.setVisibility(View.VISIBLE);
        } else {
            mSxmIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setMaxProgress(int max) {
        mProgress.setMax(max);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setMax(max);
        }
    }

    @Override
    public void setSecondaryProgress(int curr) {
        mProgress.setSecondaryProgress(curr);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setSecondaryProgress(curr);
        }
    }

    @Override
    public void setCurrentProgress(int curr) {
        mProgress.setProgress(curr);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setProgress(curr);
        }
    }

    @Override
    public void setTuneMix(boolean inTuneMix) {
        mInTuneMix = inTuneMix;
        if (inTuneMix) {
            mTuneMixImage.setImageResource(R.drawable.p0319_iconbtn_2slc);
        } else {
            mTuneMixImage.setImageResource(R.drawable.p0319_iconbtn_1nrm);
        }
        updateProgressBarVisibility();
    }

    @Override
    public void setReplayButtonEnabled(boolean enabled) {
        mReplay.setEnabled(enabled);

        if (enabled) {
            mLeftCenterGroup.setAlpha(ENABLE_ALPHA);
        } else {
            mLeftCenterGroup.setAlpha(DISABLE_ALPHA);
        }
    }

    @Override
    public void setTuneMixEnabled(boolean enabled) {
        mTuneMix.setEnabled(enabled);

        if (enabled) {
            mRightCenterGroup.setAlpha(ENABLE_ALPHA);
        } else {
            mRightCenterGroup.setAlpha(DISABLE_ALPHA);
        }
    }

    @Override
    public void setChButtonEnabled(boolean enabled) {
        mChannel.setEnabled(enabled);

        if (enabled) {
            mLeftCenterGroup.setAlpha(ENABLE_ALPHA);
        } else {
            mLeftCenterGroup.setAlpha(DISABLE_ALPHA);
        }
    }

    @Override
    public void setLiveButtonEnabled(boolean enabled) {
        if(mIsReplayMode){
            mFavorite.setEnabled(enabled);
            if (enabled) {
                mFavorite.setAlpha(ENABLE_ALPHA);
            } else {
                mFavorite.setAlpha(DISABLE_ALPHA);
            }
        }
    }

    @Override
    public void setFavoriteVisibility(boolean visible) {
        mFavorite.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTuneMixVisibility(boolean visible) {
        mRightCenterGroup.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setReplayButtonVisibility(boolean visible) {
        mLeftCenterGroup.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setPlaybackMode(PlaybackMode mode) {
        if (mode != PlaybackMode.FAST_FORWARD && mode != PlaybackMode.REWIND) {
            if (mCurrentPlayBackMode != mode) {
                if (mode == PlaybackMode.PAUSE) {
                    showStatus(GestureType.PAUSE);
                } else {
                    if (mCurrentPlayBackMode != null) {
                        if (!isPosted) {
                            mHandler.removeCallbacks(mDelayGestureFunc);
                            AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
                            alphaFadeout.setDuration(500);
                            alphaFadeout.setFillAfter(true);
                            if(mGesture.getAnimation()==null) {
                                mGesture.startAnimation(alphaFadeout);
                            }
                        }
                    }
                }
                mCurrentPlayBackMode = mode;
            }
        }
    }

    @Override
    public void showGesture(GestureType type) {
        if (type.isDisplayImg()) {
            displayGesture(type.ids.get(0), type.ids.get(1));
        } else {
            displayGestureText(type.ids.get(0));
        }
    }

    @Override
    public void setListEnabled(boolean isEnabled) {
        mListBtn.setEnabled(isEnabled);
        if(isEnabled){
            mListBtn.setAlpha(1.0f);
        }else{
            mListBtn.setAlpha(0.4f);
        }
    }

    private void showStatus(GestureType type) {
        if (type.isDisplayImg()) {
            displayStatus(type.ids.get(0), type.ids.get(1));
        }
    }

    private void displayGesture(@DrawableRes int iconBaseId, @DrawableRes int iconId) {
        mHandler.removeCallbacks(mDelayGestureFunc);
        mGestureIconBase.setImageResource(iconBaseId);
        mGestureIcon.setImageResource(iconId);
        mGestureIconBase.setVisibility(View.VISIBLE);
        mGestureIcon.setVisibility(View.VISIBLE);
        mGestureText.setVisibility(View.INVISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
        isPosted = true;
        mHandler.postDelayed(mDelayGestureFunc, GESTURE_DELAY_TIME);
    }

    private void displayGestureText(@StringRes int textId) {
        mHandler.removeCallbacks(mDelayGestureFunc);
        mGestureText.setText(textId);
        mGestureIconBase.setVisibility(View.INVISIBLE);
        mGestureIcon.setVisibility(View.INVISIBLE);
        mGestureText.setVisibility(View.VISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
        isPosted = true;
        mHandler.postDelayed(mDelayGestureFunc, GESTURE_DELAY_TIME);
    }

    private void displayStatus(@DrawableRes int iconBaseId, @DrawableRes int iconId) {
        mGestureIconBase.setImageResource(iconBaseId);
        mGestureIcon.setImageResource(iconId);
        mGestureIconBase.setVisibility(View.VISIBLE);
        mGestureIcon.setVisibility(View.VISIBLE);
        mGestureText.setVisibility(View.INVISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
    }

    // MARK - color

    @Override
    public void setColor(@ColorRes int color) {
        mColor = color;
    }

    // MARK - EQ FX

    @Override
    public void setEqButton(String str) {
        mVisText.setText(str);
    }

    @Override
    public void setFxButton(String str) {
        mFxText.setText(str);
    }

    @Override
    public void setEqFxButtonEnabled(boolean eqEnabled, boolean fxEnabled) {
        mVisualizerBtn.setEnabled(eqEnabled);
        mFxBtn.setEnabled(fxEnabled);
        if (eqEnabled) {
            mVisualizerBtn.setAlpha(ENABLE_ALPHA);
        } else {
            mVisualizerBtn.setAlpha(DISABLE_ALPHA);
        }
        if (fxEnabled) {
            mFxBtn.setAlpha(ENABLE_ALPHA);
        } else {
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
    public boolean isShowRadioTabContainer() {
        if(getParentFragment() != null) {
            return ((PlayerContainerFragment) getParentFragment()).isShowRadioTabContainer();
        }
        return false;
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
            mView = inflater.inflate(R.layout.fragment_player_sirius_xm_bar, mViewGroup);
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
            mProgressAlexa.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
        }else{
            mView = inflater.inflate(R.layout.fragment_player_sirius_xm, mViewGroup);
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
                if(mIsJacketLongClick){
                    getPresenter().onLongClickJacketAction();
                }
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
                getPresenter().onNextPresetAction();
            }

            @Override
            public void onSwipeRight() {
                getPresenter().onPreviousPresetAction();
            }
        });
        mLive.setVisibility(View.GONE);
        mTuneMix.setVisibility(View.VISIBLE);
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        mFirstFlg = false;
        mGlobalLayoutListener = () -> {
            if (!mFirstFlg) {
                if (mMusicInformation != null) {
                    mMusicInformation.setDisplayCount(mDisplayCount);
                    mMusicInformation.restartDisplay(mStringArrayList);
                }
                mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mFirstFlg = true;
            }
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
