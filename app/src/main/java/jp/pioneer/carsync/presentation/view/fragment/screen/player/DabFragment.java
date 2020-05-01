package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
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
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.DabPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.DabView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.BlurProgressBar;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicatorPreset;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.ShortCutKeyViewPager;
import timber.log.Timber;

public class DabFragment extends AbstractRadioFragment<DabPresenter, DabView> implements DabView {
    @Inject DabPresenter mPresenter;
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
    @BindView(R.id.favorite_view) ImageView mFavorite;
    @BindView(R.id.play_pause_icon) ImageView mPlayPauseIcon;
    @BindView(R.id.time_shift_icon) ImageView mTimeShiftIcon;
    @BindView(R.id.service_name) ScrollTextView mServiceName;
    @BindView(R.id.service_number) TextView mServiceNumber;
    @BindView(R.id.dynamic_label_text) ScrollTextView mDynamicLabelText;
    @BindView(R.id.pty_name_text) ScrollTextView mPtyName;
    @BindView(R.id.fm_link) TextView mFmLink;
    @BindView(R.id.antenna_icon) ImageView mAntenna;
    @BindView(R.id.source_button) RelativeLayout mSourceBtn;
    @BindView(R.id.source_button_icon) ImageView mSourceBtnIcon;
    @BindView(R.id.list_button) RelativeLayout mListBtn;
    @BindView(R.id.visualizer_button) ConstraintLayout mVisualizerBtn;
    @BindView(R.id.fx_button) ConstraintLayout mFxBtn;
    @BindView(R.id.fx_text) TextView mFxText;
    @BindView(R.id.vis_text) TextView mVisText;
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.progressbar) BlurProgressBar mProgress;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    private Unbinder mUnbinder;
    private boolean mFirstFlg = false;
    private ViewGroup mViewGroup;
    private View mView;
    private ShortCutKeyViewPager mViewPager;
    private CustomLinePageIndicatorPreset mLineIndicator;
    private BandType mBandType;
    private RelativeLayout mShortCutGroup;
    private ProgressBar mProgressAlexa;
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private final Handler mHandler = new Handler();
    private int mColor;
    private TunerStatus mCurrentStatus;
    private boolean isPosted;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private int mRealPosition=-1;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
			if(mCurrentStatus == TunerStatus.LIST_UPDATE){
                showStatus(GestureType.LIST_UPDATE);
            } else if(mCurrentStatus == TunerStatus.SEEK){
                showStatus(GestureType.SEEK);
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
    public DabFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioFragment
     */
    public static DabFragment newInstance(Bundle args) {
        DabFragment fragment = new DabFragment();
        if(args == Bundle.EMPTY) {
            args = new Bundle();
        }
        args.putInt("pager",-1);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_player_2, container, false);
        mViewGroup = (ViewGroup) view.findViewById(R.id.container_layout);
        Bundle args = getArguments();
        if(args!=null&&args.getInt("pager")==-1){
            getPresenter().setPagerPosition(-1);
            args.clear();
            setArguments(args);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mDelayGestureFunc);
        mHandler.removeCallbacks(mDelayMessageFunc);
        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DAB;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DabPresenter getPresenter() {
        return mPresenter;
    }
    /**
     * リストボタン押下イベント
     */
    @OnClick(R.id.list_button)
    public void onClickListButton() {
        getPresenter().onSelectListAction();
    }

    /**
     * サービス名押下イベント.
     */
    @OnClick(R.id.service_name)
    public void onClickServiceName() {
        getPresenter().onServiceNameAction();
    }

    /**
     * バンド押下イベント.
     */
    @OnClick(R.id.band_text)
    public void onClickBand() {
        getPresenter().onToggleBandAction();
    }

    /**
     * PlayPause押下イベント.
     */
    @OnClick(R.id.play_pause_icon)
    public void onClickPlayPauseIcon() {
        getPresenter().onPlayPauseAction();
    }

    /**
     * タイムシフト押下イベント.
     */
    @OnClick(R.id.time_shift_icon)
    public void onClickTimeShiftIcon() {
        getPresenter().onTimeShiftAction();
    }

    @Override
    public void onClickJacketView() {
    }

    @Override
    public void onClickFavoriteButton() {
        getPresenter().onFavoriteAction();
    }

    @Override
    public void onClickFxButton() {
        getPresenter().onSelectFxAction();
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
    public void setFmLink(String fmLink) {
        TextViewUtil.setMarqueeTextIfChanged(mFmLink, fmLink);
    }

    @Override
    public void setServiceName(String serviceName) {
        TextViewUtil.setMarqueeTextIfChanged(mServiceName, serviceName);
    }

    @Override
    public void setPlayPauseVisible(boolean isVisible) {
        if(isVisible) {
            mPlayPauseIcon.setVisibility(View.VISIBLE);
        }else{
            mPlayPauseIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setPlayPause(boolean isPlay,boolean timeShiftModeAvailable) {
        if(timeShiftModeAvailable){
            mPlayPauseIcon.setImageResource(R.drawable.p1563_pause);
        } else {
            if (isPlay) {
                mPlayPauseIcon.setImageResource(R.drawable.p1563_pause);
            } else {
                mPlayPauseIcon.setImageResource(R.drawable.p1562_play);
            }
        }
    }

    @Override
    public void setTimeShiftVisible(boolean isVisible) {
        if(isVisible) {
            mTimeShiftIcon.setVisibility(View.VISIBLE);
        }else{
            mTimeShiftIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setTimeShift(boolean timeShiftMode,boolean timeShiftModeAvailable) {
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        if (timeShiftMode) {
            mTimeShiftIcon.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1561_timeshift, outValue.resourceId));
        } else {
            if(timeShiftModeAvailable) {
                mTimeShiftIcon.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1561_timeshift, R.color.time_shift_off));
            }else{
                mTimeShiftIcon.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1560_sxmlive, outValue.resourceId));
            }
        }
        mServiceNumber.setVisibility(timeShiftMode ? View.INVISIBLE : View.VISIBLE);
        mProgress.setVisibility(!mShortcutKeyVisible&&timeShiftMode ? View.VISIBLE : View.INVISIBLE);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setVisibility(mShortcutKeyVisible && timeShiftMode ? View.VISIBLE : View.INVISIBLE);
        }
        mAntenna.setVisibility(timeShiftMode ? View.INVISIBLE : View.VISIBLE);
        setListEnabled(!timeShiftMode);
    }

    @Override
    public void setDynamicLabelText(String info) {
        TextViewUtil.setMarqueeTextIfChanged(mDynamicLabelText, info);
    }

    @Override
    public void setPtyName(String pty) {
        //TextViewUtil.setMarqueeTextIfChanged(mPtyName, pty);
    }

    @Override
    public void setServiceNumber(String serviceNumber) {
        TextViewUtil.setMarqueeTextIfChanged(mServiceNumber, serviceNumber);
    }

    @Override
    public void setFavoriteVisible(boolean isVisible) {
        if(isVisible) {
            mFavorite.setVisibility(View.VISIBLE);
        }else{
            mFavorite.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setFavorite(boolean isFavorite) {
        if (isFavorite) {
            mFavorite.setImageResource(R.drawable.button_favorite_on);
        } else {
            mFavorite.setImageResource(R.drawable.button_favorite_off);
        }
    }

    @Override
    public void setBand(DabBandType band) {
        if (band == null) {
            mBand.setText("");
        } else {
            mBand.setText(getString(band.getLabel()));
        }
    }

    @Override
    public void setAntennaLevel(float level) {
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        int antennaId = ANTENNA_LIST.get(levelInt, -1);
        if(antennaId >= 0){
            mAntenna.setImageResource(antennaId);
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
    public void setStatus(PlaybackMode mode,TunerStatus status) {
		if(mCurrentStatus != status) {
            if(status == TunerStatus.LIST_UPDATE){
                showStatus(GestureType.LIST_UPDATE);
            } else if(status == TunerStatus.SEEK) {
                showStatus(GestureType.SEEK);
            }else {
                if (!isPosted&(mCurrentStatus == TunerStatus.SEEK || mCurrentStatus == TunerStatus.LIST_UPDATE)) {
                    mHandler.removeCallbacks(mDelayGestureFunc);
                    AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
                    alphaFadeout.setDuration(500);
                    alphaFadeout.setFillAfter(true);
                    mGesture.startAnimation(alphaFadeout);
                }
            }
            //DABソースの状態がLIST UPDATE、または、ERRORの場合、Preset areaの右→左スワイプを抑制
            if(status == TunerStatus.LIST_UPDATE||status == TunerStatus.ERROR){
                if(!(mLineIndicator.isShortCutKeyOn()&&mViewPager.getCurrentItem()==0)){
                    mViewPager.setAllowedSwipeLeftOnly(true);
                }
            }else{
                mViewPager.setAllowedSwipeLeftOnly(false);
            }
            mCurrentStatus = status;
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

    @Override
    public void setListEnabled(boolean isEnabled) {
        mListBtn.setEnabled(isEnabled);
        if(isEnabled){
            mListBtn.setAlpha(ENABLE_ALPHA);
        }else{
            mListBtn.setAlpha(DISABLE_ALPHA);
        }
    }

    @Override
    public void setColor(int color) {
        mColor = color;
    }

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

    @Override
    public boolean isShowRadioTabContainer() {
        if(getParentFragment() != null) {
            return ((PlayerContainerFragment) getParentFragment()).isShowRadioTabContainer();
        }
        return false;
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
        isPosted = true;
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
        isPosted = true;
        mHandler.postDelayed(mDelayGestureFunc, GESTURE_DELAY_TIME);
    }

    private void displayStatusText(@StringRes int textId) {
        mGestureText.setText(textId);
        mGestureIconBase.setVisibility(View.INVISIBLE);
        mGestureIcon.setVisibility(View.INVISIBLE);
        mGestureText.setVisibility(View.VISIBLE);
        mGesture.setVisibility(View.VISIBLE);
        mGesture.clearAnimation();
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
            mView = inflater.inflate(R.layout.fragment_player_dab_bar, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
            mViewPager = (ShortCutKeyViewPager) mView.findViewById(R.id.viewPager);
            mLineIndicator = (CustomLinePageIndicatorPreset) mView.findViewById(R.id.line_indicator);
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
                @Override
                protected void onClickPreset(BandType bandType, int presetNum) {
                    getPresenter().onSelectPreset(presetNum);
                }
                @Override
                protected void onLongClickPreset(BandType bandType, int presetNum) {
                    getPresenter().onRegisterPresetChannel(presetNum);
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
            mView = inflater.inflate(R.layout.fragment_player_dab, mViewGroup);
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
                if(mCurrentStatus!=TunerStatus.LIST_UPDATE&&mCurrentStatus!=TunerStatus.SEEK) {
                    getPresenter().onNextPresetAction();
                }
            }

            @Override
            public void onSwipeRight() {
                if(mCurrentStatus!=TunerStatus.LIST_UPDATE&&mCurrentStatus!=TunerStatus.SEEK) {
                    getPresenter().onPreviousPresetAction();
                }
            }
        });
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        //v1.6 PTY Info表示削除
        mPtyName.setVisibility(View.INVISIBLE);
        mFirstFlg = false;
        mGlobalLayoutListener = () -> {
            if (!mFirstFlg) {
                mDynamicLabelText.startScroll();
                //mPtyName.startScroll();
                mServiceName.startScroll();
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
    // MARK -以下Radio、HdRadio、Dab共通
    /**
     * BandTypeListの設定
     *
     * @param bandTypes BandTypes
     */
    @Override
    public void setBandList(ArrayList<BandType> bandTypes) {
        mAdapter.setPresetBandList(bandTypes);
        int cur = mViewPager.getCurrentItem();
        mLineIndicator.setShortCutKeyOn(bandTypes.size()<mAdapter.getRealCount());
        mLineIndicator.setCurrentItem(Math.min(cur, mAdapter.getCount()));
        mLineIndicator.setVisibility(mAdapter.getCount() <= 1 ? View.INVISIBLE : View.VISIBLE);
        mViewPager.setOnGestureListener(new ShortCutKeyViewPager.OnGestureListener() {
            @Override
            public void onSwipeLeft() {
                if (bandTypes.size() == mAdapter.getCount()) {
                    //ショートカットなしの場合
                    getPresenter().onToggleBandAction();
                } else {
                    //ショートカットありの場合
                    // 現在位置がショートカットで左→右にスワイプしたら現在Band位置に戻す
                    if(mViewPager.getCurrentItem()==0){
                        getPresenter().setPagerPosition(-1);
                        setViewPagerCurrentPage(mBandType);
                    }else{
                        getPresenter().onToggleBandAction();
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                if (bandTypes.size() == mAdapter.getCount()) {
                    //ショートカットなしの場合
                    //左→右スワイプは抑制されている
                } else {
                    //ショートカットありで左→右にスワイプした場合はショートカットを表示
                    mViewPager.setCurrentItem(0, true);
                    getPresenter().setPagerPosition(0);
                }
            }
        });
        mViewPager.setPagingEnabled(true);
        mViewPager.setAllowedSwipeRightOnly(bandTypes.size() == mAdapter.getRealCount());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ArrayList<BandType> bandTypes = mAdapter.getPresetBandList();
                BandType bandType;
                Timber.d("onPageSelected:position=" + position);

                if (bandTypes.size() == mAdapter.getRealCount()) {
                    if (position == mAdapter.getRealCount()) {
                        //最後のページにスクロールしたら最初のBandに遷移
                        mRealPosition = 0;
                    }
                    //ショートカットなしの場合
                    bandType = bandTypes.get(mAdapter.getRealPosition(position));

                    //左スワイプは抑制されている
                    //Band位置が変わっていたらBAND切替通知を車載機に送る
                    if (mBandType != null && mBandType != bandType && position != 0) {
                        Timber.d("onToggleBandAction");
                        getPresenter().setPagerPosition(mAdapter.getRealPosition(position));
                        getPresenter().onToggleBandAction();
                    }
                } else {
                    if (position == mAdapter.getRealCount()) {
                        //最後のページにスクロールしたら最初のBandに遷移
                        mRealPosition = 1;
                    }
                    //ショートカットありで現在位置がショートカットの時は何もしない
                    if (position == 0) {
                        //移動完了後にも位置の保存が必要
                        getPresenter().setPagerPosition(0);
                        //ショートカット位置では右→左スワイプを抑制しない
                        mViewPager.setAllowedSwipeLeftOnly(false);
                        return;
                    }else{
                        //DABソースの状態がLIST UPDATE、または、ERRORの場合、Preset areaの右→左スワイプを抑制
                        if(mCurrentStatus==TunerStatus.LIST_UPDATE||mCurrentStatus==TunerStatus.ERROR) {
                            mViewPager.setAllowedSwipeLeftOnly(true);
                        }else{
                            mViewPager.setAllowedSwipeLeftOnly(false);
                        }
                    }
                    //ショートカットありで左にスワイプした場合はショートカットを表示
                    if (position < getPresenter().getPagerPosition()) {
                        Timber.d("onPageSelected:position=" + position + ",getPagerPosition=" + getPresenter().getPagerPosition());
                        getPresenter().setPagerPosition(0);
                        //smoothScroll有効でないとViewPager更新がされない
                        mViewPager.setCurrentItem(0, true);
                        return;
                    }
                    //ショートカット位置から右にスワイプした場合元のBand位置に戻す
                    if (getPresenter().getPagerPosition() == 0 && position > 0) {
                        getPresenter().setPagerPosition(bandTypes.indexOf(mBandType) + 1);
                        //smoothScroll有効でないとViewPager更新がされない
                        mViewPager.setCurrentItem(bandTypes.indexOf(mBandType) + 1, true);
                        return;
                    }
                    //現位置のバンドを取得
                    bandType = bandTypes.get(mAdapter.getRealPosition(position) - 1);
                    //Band位置が変わっていたらBAND切替通知を車載機に送る//最後ページから最初のbandへの遷移後は何もしない
                    if (mBandType != null && mBandType != bandType && position != 1) {
                        Timber.d("onToggleBandAction");
                        //Band変更時のsetViewPagerCurrentPageでPage移動されないようにする
                        getPresenter().setPagerPosition(mAdapter.getRealPosition(position));
                        getPresenter().onToggleBandAction();
                    }
                }
                Timber.d("onPageSelected:bandType=" + bandType + ",mBandType=" + mBandType + ",position=" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Timber.d("onPageScrollStateChanged:state=" + state + ",mRealPosition=" + mRealPosition);
                if (state == ViewPager.SCROLL_STATE_IDLE && mRealPosition >= 0) {
                    getPresenter().setPagerPosition(mRealPosition);
                    mViewPager.setCurrentItem(mRealPosition, false);
                    //遷移完了後にViewPagerの位置更新をする
                    mAdapter.setCurrentPagerPosition(mRealPosition);
                    mRealPosition = -1;
                    return;
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mViewPager.getCurrentItem();
                    Timber.d("onPageScrollStateChanged:position=" + position);
                    //移動完了後にViewPagerの位置更新をする
                    mAdapter.setCurrentPagerPosition(position);
                }
            }
        });
    }

    /**
     * ViewPagerに現在のBandを設定
     */
    @Override
    public void setViewPagerCurrentPage(BandType bandType){
        Timber.d("setViewPagerCurrentPage:bandType="+bandType);
        if(mBandType!=bandType) {
            mBandType = bandType;
            if (mAdapter == null) return;
            ArrayList<BandType> bandTypes = mAdapter.getPresetBandList();
            if (bandTypes != null && bandTypes.contains(bandType)) {
                int pageIndex;
                if (bandTypes.size() == mAdapter.getRealCount()) {
                    //ショートカットキーなし
                    pageIndex = bandTypes.indexOf(bandType);
                    if (mViewPager.getCurrentItem() == 0 || bandTypes.get(getPresenter().getPagerPosition()) != bandType) {
                        Timber.d("setViewPagerCurrentPage:move");
                        getPresenter().setPagerPosition(pageIndex);
                        mViewPager.setCurrentItem(pageIndex, false);
                        mAdapter.setCurrentPagerPosition(pageIndex);
                    }
                } else if (bandTypes.size() < mAdapter.getRealCount()) {
                    //ショートカットキーあり
                    pageIndex = bandTypes.indexOf(bandType) + 1;
                    //現在位置がショートカットなら何もしない
                    if (getPresenter().getPagerPosition() == 0) return;
                    if (mViewPager.getCurrentItem() == 0 || bandTypes.get(getPresenter().getPagerPosition() - 1) != bandType) {
                        Timber.d("setViewPagerCurrentPage:move2");
                        //遷移前に位置を保存し、onPageSelectedで左スワイプ判定されないようにする
                        getPresenter().setPagerPosition(pageIndex);
                        mViewPager.setCurrentItem(pageIndex, false);
                        mAdapter.setCurrentPagerPosition(pageIndex);
                    }
                }
                Timber.d("setViewPagerCurrentPage:mBandType=" + mBandType + ",mPagerPosition=" + getPresenter().getPagerPosition());
            }
        }
    }
    /**
     * 選択中のPCH設定
     */
    @Override
    public void setSelectedPreset(int pch){
        mAdapter.setSelectedPresetNumber(pch);
    }
    // MARK -ここまでRadio、HdRadio、Dab共通
}
