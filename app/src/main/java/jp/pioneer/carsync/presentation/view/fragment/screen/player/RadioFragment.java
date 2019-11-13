package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
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
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.RadioPresenter;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.RadioView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicatorPreset;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.ShortCutKeyViewPager;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextView;
import timber.log.Timber;

/**
 * ラジオ再生画面
 */

public class RadioFragment extends AbstractRadioFragment<RadioPresenter, RadioView> implements RadioView, SingleChoiceDialogFragment.Callback {
    private static final String TAG_DIALOG_PTY = "pty";

    @Inject RadioPresenter mPresenter;
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
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.status_view_back) View mStatusViewBack;
    @BindView(R.id.status_view) LinearLayout mStatusView;
    @BindView(R.id.bsm_icon) ImageView mBsmIcon;
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.dialog_close_button) ImageView mDialogCloseButton;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    private boolean mFirstFlg = false;
    private int mDisplayCount = 0;
    private String mTitleName = "";
    private String mArtist = "";
    private String mPty = "";
    private ArrayList<String> mStringArrayList = new ArrayList<String>();
    private ViewGroup mViewGroup;
    private View mView;
    private ShortCutKeyViewPager mViewPager;
    private CustomLinePageIndicatorPreset mLineIndicator;
    private BandType mBandType;
    private RelativeLayout mShortCutGroup;
    private Unbinder mUnbinder;
    private int mColor;
    private boolean mIsRdsInterruption = false;
    private final Handler mHandler = new Handler();
    private TunerStatus mCurrentStatus;
    private boolean isPosted;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            if(mCurrentStatus == TunerStatus.SEEK){
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
    public RadioFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioFragment
     */
    public static RadioFragment newInstance(Bundle args) {
        RadioFragment fragment = new RadioFragment();
        if(args == Bundle.EMPTY) {
         args = new Bundle();
        }
        args.putInt("pager",-1);
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
     * 周波数押下イベント.
     */
    @OnClick({R.id.frequency_text, R.id.frequency_decimal_text, R.id.frequency_no_decimal_text})
    public void onClickFrequency() {
        getPresenter().onFrequencyAction();
    }

    /**
     * バンド押下イベント.
     */
    @OnClick(R.id.band_text)
    public void onClickBand() {
        getPresenter().onToggleBandAction();
    }

    /**
     * PTYサーチ押下イベント.
     */
    @OnClick(R.id.pty_search_icon)
    public void onClickPtySearch() {
        getPresenter().showPtySelect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_radio, container, false);
        Bundle args = getArguments();
        if(args!=null&&args.getInt("pager")==-1){
            getPresenter().setPagerPosition(-1);
            args.clear();
            setArguments(args);
        }
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
        return ScreenId.RADIO;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected RadioPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onClickJacketView() {
    }

    @Override
    public void onClickFavoriteButton() {
        getPresenter().onFavoriteAction();
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
    public void setRdsInterruption(RdsInterruptionType type, String info) {
        if(type==RdsInterruptionType.NORMAL){
            mIsRdsInterruption = false;
            mSourceBtn.setEnabled(true);
            mSourceBtn.setAlpha(ENABLE_ALPHA);
            mListBtn.setEnabled(true);
            mListBtn.setAlpha(ENABLE_ALPHA);
            mBand.setVisibility(View.VISIBLE);
            mFavorite.setEnabled(true);
            mFavorite.setAlpha(ENABLE_ALPHA);
            mFrequency.setEnabled(true);
            mFrequencyDecimal.setEnabled(true);
            mFrequencyNoDecimal.setEnabled(true);
            setEqFxButtonEnabled(true,true);
        }else{
            mIsRdsInterruption = true;
            setPtySearchEnabled(false);
            mSourceBtn.setEnabled(false);
            mSourceBtn.setAlpha(DISABLE_ALPHA);
            mListBtn.setEnabled(false);
            mListBtn.setAlpha(DISABLE_ALPHA);
            mBand.setVisibility(View.INVISIBLE);
            mFavorite.setEnabled(false);
            mFavorite.setAlpha(DISABLE_ALPHA);
            mFrequency.setEnabled(false);
            mFrequencyDecimal.setEnabled(false);
            mFrequencyNoDecimal.setEnabled(false);
            setEqFxButtonEnabled(false,false);
            TextViewUtil.setTextIfChanged(mMusicInformation, info);
            mMusicInformation.setSingleText(info);
            String text = "";
            switch (type) {
                case TA:
                    text = getString(R.string.ply_035);
                    break;
                case NEWS:
                    text = getString(R.string.ply_019);
                    break;
                case ALARM:
                    text = getString(R.string.ply_005);
                    break;
                default:
                    break;
            }
            TextViewUtil.setMarqueeTextIfChanged(mPsName, text);
        }
    }

    @Override
    public void setPtySearchEnabled(boolean isEnabled) {
        if(mIsRdsInterruption)isEnabled=false;
        mPtySearch.setEnabled(isEnabled);
        if(isEnabled){
            mPtySearch.setAlpha(ENABLE_ALPHA);
        }else{
            mPtySearch.setAlpha(DISABLE_ALPHA);
        }
    }
    
    @Override
    public void setMusicInfo(String title,String pty,String artist) {
        if(mIsRdsInterruption)return;
        if (artist == null) {
            artist = "";
        }
        if (pty == null) {
            pty = "";
        }
        if (mTitleName.equals(title)&&mArtist.equals(artist)&&mPty.equals(pty)) {
            return;
        }else{
            mTitleName = title;
            mArtist = artist;
            mPty = pty;
        }
        mStringArrayList = new ArrayList<>();
        if(!mPty.equals("")) {
            mStringArrayList.add(mPty);
        }
        if(!mArtist.equals("")) {
            mStringArrayList.add(mArtist);
        }
        mMusicInformation.setStringArrayList(mStringArrayList);
    }

    @Override
    public void setFrequency(String frequency) {
        if (frequency == null){
            mFrequency.setText(null);
            mFrequencyDecimal.setText(null);
            mFrequencyNoDecimal.setText(null);
            mFrequencyUnit.setText(null);
        }else {
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

    @Override
    public void setPsInformation(String info) {
        if(!mIsRdsInterruption) {
            TextViewUtil.setMarqueeTextIfChanged(mPsName, info);
        }
    }

    @Override
    public void setPch(int pch) {
        if(pch == -1 || mIsRdsInterruption){
            mPrePch.setVisibility(View.INVISIBLE);
            mPch.setText(null);
        }else{
            mPrePch.setVisibility(View.VISIBLE);
            TextViewUtil.setMarqueeTextIfChanged(mPch, String.valueOf(pch));
        }
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
    public void setBand(RadioBandType band) {
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
    public void setStatus(TunerStatus status) {
        if(mIsRdsInterruption)return;
        if(mCurrentStatus != status) {
            if (status == TunerStatus.SEEK || status == TunerStatus.PI_SEARCH) {
                showStatus(GestureType.SEEK);
                mBand.setEnabled(false);
                mPtySearch.setEnabled(false);
                mFavorite.setEnabled(false);
            } else {
                if (!isPosted&&(mCurrentStatus == TunerStatus.SEEK || mCurrentStatus == TunerStatus.PI_SEARCH)) {
                    mHandler.removeCallbacks(mDelayGestureFunc);
                    AlphaAnimation alphaFadeout = new AlphaAnimation(1.0f, 0.0f);
                    alphaFadeout.setDuration(500);
                    alphaFadeout.setFillAfter(true);
                    mGesture.startAnimation(alphaFadeout);
                }
                mBand.setEnabled(true);
                mPtySearch.setEnabled(true);
                mFavorite.setEnabled(true);
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
        if(!mIsRdsInterruption) {
            mListBtn.setEnabled(isEnabled);
            if (isEnabled) {
                mListBtn.setAlpha(ENABLE_ALPHA);
            } else {
                mListBtn.setAlpha(DISABLE_ALPHA);
            }
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

    private void displayStatusText(@StringRes int textId){
        mGestureText.setText(textId);
        mGestureIconBase.setVisibility(View.INVISIBLE);
        mGestureIcon.setVisibility(View.INVISIBLE);
        mGestureText.setVisibility(View.VISIBLE);
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

    // MARK - dialog

    @Override
    public void selectItem(int position) {
        getPresenter().onPtySearchAction(position);
    }

    @Override
    public void onClose(SingleChoiceDialogFragment fragment) {

    }

    public void showStatusView(boolean isShow, String type){
        if(isShow) {
            mStatusViewBack.setVisibility(View.VISIBLE);
            mStatusView.setVisibility(View.VISIBLE);
            mStatusViewBack.setOnTouchListener(new View.OnTouchListener() {
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
            int text;
            int animationResource;
            switch (type) {
                case RadioPresenter.TAG_BSM:
                    text = R.string.ply_058;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.VISIBLE);
                    break;
                case RadioPresenter.TAG_PTY_SEARCH:
                    text = R.string.ply_041;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.GONE);
                    break;
                default:
                    text = R.string.ply_058;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.VISIBLE);
                    break;
            }
            mTitleText.setText(text);
            mBsmIcon.setImageResource(animationResource);
            AnimationDrawable frameAnimation = (AnimationDrawable) mBsmIcon.getDrawable();
            // アニメーションの開始
            frameAnimation.start();
        }else{
            mStatusViewBack.setVisibility(View.GONE);
            mStatusView.setVisibility(View.GONE);
            mStatusViewBack.setOnTouchListener(null);
        }
    }
    /**
     * BSMの閉じるボタン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.dialog_close_button)
    public void onClickCloseButton(View view) {
        getPresenter().onCloseAction();
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
            mView = inflater.inflate(R.layout.fragment_player_radio_bar, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
            mViewPager = (ShortCutKeyViewPager) mView.findViewById(R.id.viewPager);
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
                if(!mIsRdsInterruption) {
                    getPresenter().onNextPresetAction();
                }
            }

            @Override
            public void onSwipeRight() {
                if(!mIsRdsInterruption) {
                    getPresenter().onPreviousPresetAction();
                }
            }
        });
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        mFirstFlg = false;
        mGlobalLayoutListener = () -> {
            if (!mFirstFlg) {
                if (mPsName != null) {
                    mPsName.startScroll();
                }
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
        mLineIndicator.setShortCutKeyOn(bandTypes.size()<mAdapter.getCount());
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
                    mViewPager.setCurrentItem(0,false);
                    getPresenter().setPagerPosition(0);
                }
            }
        });
    }
    /**
     * ViewPagerに現在のBandを設定
     */
    @Override
    public void setViewPagerCurrentPage(BandType bandType){
        mBandType = bandType;
        if(mAdapter==null)return;
        ArrayList<BandType> bandTypes = mAdapter.getPresetBandList();
        if(bandTypes!=null&&bandTypes.contains(bandType)){
            int pageIndex;
            if(bandTypes.size()==mAdapter.getCount()){
                //ショートカットキーなし
                pageIndex = bandTypes.indexOf(bandType);
                if(bandTypes.get(mViewPager.getCurrentItem())!=bandType) {
                    getPresenter().setPagerPosition(pageIndex);
                    mViewPager.setCurrentItem(pageIndex,false);
                }
            }else if(bandTypes.size()<mAdapter.getCount()){
                //ショートカットキーあり
                pageIndex = bandTypes.indexOf(bandType) + 1;
                //現在位置がショートカットなら何もしない
                if(getPresenter().getPagerPosition()==0)return;
                if(mViewPager.getCurrentItem()==0||bandTypes.get(mViewPager.getCurrentItem()-1)!=bandType) {
                    getPresenter().setPagerPosition(pageIndex);
                    mViewPager.setCurrentItem(pageIndex,false);
                }
            }
            Timber.d("setViewPagerCurrentPage:mBandType=" + mBandType + ",mPagerPosition=" + getPresenter().getPagerPosition() );
        }
    }
    /**
     * 選択中のPCH設定
     */
    @Override
    public void setSelectedPreset(int pch){
        if(mAdapter!=null) {
            mAdapter.setSelectedPresetNumber(pch);
        }
    }
    // MARK -ここまでRadio、HdRadio、Dab共通
}
