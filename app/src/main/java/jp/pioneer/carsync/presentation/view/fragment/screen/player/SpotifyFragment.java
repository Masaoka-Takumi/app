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
import jp.pioneer.carsync.domain.model.CarDeviceRepeatMode;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.SpotifyPresenter;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.SpotifyView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextView;
import timber.log.Timber;

/**
 * Spotify再生の画面
 */

public class SpotifyFragment extends AbstractMusicPlayerFragment<SpotifyPresenter, SpotifyView> implements SpotifyView {
    private static final int DELAY_TIME = 500;
    @Inject SpotifyPresenter mPresenter;
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
    @BindView(R.id.music_title_text) ScrollTextView mTitle;
    @BindView(R.id.music_information_text) SwitchTextView mMusicInformation;
    @BindView(R.id.repeat_button) ImageView mRepeat;
    @BindView(R.id.shuffle_button) ImageView mShuffle;
    @BindView(R.id.thumb_up_group) ConstraintLayout mThumbUpGroup;
    @BindView(R.id.thumb_down_group) ConstraintLayout mThumbDownGroup;
    @BindView(R.id.thumb_up_button) ImageView mThumbUp;
    @BindView(R.id.thumb_down_button) ImageView mThumbDown;
    @BindView(R.id.progressbar) ProgressBar mProgress;
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
    private boolean mFirstFlg = false;
    private int mDisplayCount = 0;
    private String mTitleName = "";
    private String mArtist = "";
    private String mAlbum = "";
    private String mPlayingTrackSource = "";
    private ArrayList<String> mStringArrayList = new ArrayList<String>();
    private int mContentMaxSeconds = 0; // 再生中コンテンツの総時間(秒)
    private int mCurrentPositionSeconds = 0; // 再生済み時間(秒)
    private ViewGroup mViewGroup;
    private View mView;
    private ViewPager mViewPager;
    private CustomLinePageIndicator mLineIndicator;
    private RelativeLayout mShortCutGroup;
    private ProgressBar mProgressAlexa;
    private Unbinder mUnbinder;
    private int mColor;
    private boolean mIsRadioPlaying;
    private final Handler mHandler = new Handler();
    private PlaybackMode mCurrentPlayBackMode;
    private boolean isPosted;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private int mOrientation;
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            if(mCurrentPlayBackMode == PlaybackMode.PAUSE){
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
    public SpotifyFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SpotifyFragment
     */
    public static SpotifyFragment newInstance(Bundle args) {
        SpotifyFragment fragment = new SpotifyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_music_streaming, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mViewGroup =container;
        mView = view;
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        return view;
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
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SpotifyPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SPOTIFY;
    }

    @Override
    public void onClickJacketView() {
        getPresenter().onPlayPauseAction();
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
    public void onClickSetting(View view) {
        getPresenter().onSettingShowAction();
    }

    @Override
    public void onClickRepeat(View view) {
        getPresenter().onRepeatAction();
    }

    @Override
    public void onClickShuffle(View view) {
        getPresenter().onShuffleAction();
    }

    @Override
    public void setMusicTitle(String title) {
        TextViewUtil.setMarqueeTextIfChanged(mTitle, title);
    }

    @Override
    public void setMusicInfo(String title,String artist,String albumName,String playingTrackSource) {
        if (artist == null) {
            artist = "";
        }
        if (albumName == null) {
            albumName = "";
        }
        if (playingTrackSource == null) {
            playingTrackSource = "";
        }
        if (mTitleName.equals(title)&&mArtist.equals(artist)&&mAlbum.equals(albumName)&&mPlayingTrackSource.equals(playingTrackSource)) {
            return;
        }else{
            mTitleName = title;
            mArtist = artist;
            mAlbum = albumName;
            mPlayingTrackSource = playingTrackSource;
        }
        mStringArrayList = new ArrayList<>();
        if(!mAlbum.equals("")) {
            mStringArrayList.add(mAlbum);
        }
        if(!mArtist.equals("")) {
            mStringArrayList.add(mArtist);
        }
        if(!mPlayingTrackSource.equals("")) {
            mStringArrayList.add(mPlayingTrackSource);
        }
        mMusicInformation.setStringArrayList(mStringArrayList);
    }

    @Override
    public void setMaxProgress(int max) {
        mProgress.setMax(max);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setMax(max);
        }
        mContentMaxSeconds = max;
        if (mCurrentTimeView != null&&mRemainingTimeView != null) {
            updateTimeLabel(mContentMaxSeconds, mCurrentPositionSeconds, mCurrentTimeView, mRemainingTimeView);
        }
    }

    @Override
    public void setCurrentProgress(int curr) {
        mProgress.setProgress(curr);
        if(mProgressAlexa!=null) {
            mProgressAlexa.setProgress(curr);
        }
        mCurrentPositionSeconds = curr;
        if (mCurrentTimeView != null&&mRemainingTimeView != null) {
            updateTimeLabel(mContentMaxSeconds, mCurrentPositionSeconds, mCurrentTimeView, mRemainingTimeView);
        }
    }

    @Override
    public void setThumbStatus(ThumbStatus thumbStatus) {
        if (mIsRadioPlaying) {
            switch (thumbStatus) {
                case NONE:
                    mThumbUp.setImageResource(R.drawable.p0323_iconbtn_1nrm);
                    mThumbDown.setImageResource(R.drawable.p0324_iconbtn_1nrm);
                    break;
                case UP:
                    mThumbUp.setImageResource(R.drawable.p0323_iconbtn_2slc);
                    mThumbDown.setImageResource(R.drawable.p0324_iconbtn_1nrm);
                    break;
                case DOWN:
                    mThumbUp.setImageResource(R.drawable.p0323_iconbtn_1nrm);
                    mThumbDown.setImageResource(R.drawable.p0324_iconbtn_2slc);
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        }
    }

    @Override
    public void setRepeatImage(CarDeviceRepeatMode mode) {
        switch (mode) {
            case OFF:
                mRepeat.setImageResource(R.drawable.p0043_trickplaybtn_1nrm);
                mRepeat.setAlpha(DISABLE_ALPHA);
                break;
            case ONE:
                mRepeat.setImageResource(R.drawable.p0044_trickplaybtn_1nrm);
                mRepeat.setAlpha(ENABLE_ALPHA);
                break;
            case ALL:
                mRepeat.setImageResource(R.drawable.p0043_trickplaybtn_1nrm);
                mRepeat.setAlpha(ENABLE_ALPHA);
                break;
            case FOLDER:
                mRepeat.setImageResource(R.drawable.p0045_trickplaybtn_1nrm);
                mRepeat.setAlpha(ENABLE_ALPHA);
                break;
            default:
                Timber.w("This case is impossible.");
                break;
        }
    }

    @Override
    public void setShuffleImage(ShuffleMode mode) {
        switch (mode) {
            case OFF:
                mShuffle.setAlpha(DISABLE_ALPHA);
                break;
            case ON:
                mShuffle.setAlpha(ENABLE_ALPHA);
                break;
            default:
                Timber.w("This case is impossible.");
                break;
        }
    }

    @Override
    public void setModeView(boolean radioPlaying) {
        mIsRadioPlaying = radioPlaying;
        mThumbDownGroup.setOnClickListener(v -> getPresenter().onThumbDownAction());
        mThumbUpGroup.setOnClickListener(v -> getPresenter().onThumbUpAction());
        if (radioPlaying) {
            mShuffle.setVisibility(View.INVISIBLE);
            mRepeat.setVisibility(View.INVISIBLE);
            mThumbDownGroup.setVisibility(View.VISIBLE);
            mThumbUpGroup.setVisibility(View.VISIBLE);
            mThumbDown.setEnabled(true);
            mThumbUp.setEnabled(true);
        } else {
            mThumbDownGroup.setVisibility(View.GONE);
            mThumbUpGroup.setVisibility(View.GONE);
            mShuffle.setVisibility(View.VISIBLE);
            mRepeat.setVisibility(View.VISIBLE);
        }
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
                            mGesture.startAnimation(alphaFadeout);
                        }
                    }
                }
                mCurrentPlayBackMode = mode;
            }
        }
    }

    @Override
    public void showGesture(GestureType type) {
        if(type.isDisplayImg()){
            displayGesture(type.ids.get(0), type.ids.get(1));
        }
    }

    private void showStatus(GestureType type) {
        if(type.isDisplayImg()){
            displayStatus(type.ids.get(0), type.ids.get(1));
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
        isPosted = true;
        mHandler.postDelayed(mDelayGestureFunc, DELAY_TIME);
    }

    private void displayStatus(@DrawableRes int iconBaseId, @DrawableRes int iconId){
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
            mView = inflater.inflate(R.layout.fragment_player_music_streaming_alexa, mViewGroup);
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
            if(mOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                ViewGroup.LayoutParams lp = mTitle.getLayoutParams();
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.setMargins((int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_2), mlp.topMargin, (int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_2), mlp.bottomMargin);
                mTitle.setLayoutParams(mlp);
            }
        }else{
            mView = inflater.inflate(R.layout.fragment_player_music_streaming, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
        }
        setLayout();
    }

    private void setLayout() {
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
                getPresenter().onSkipNextAction();
            }

            @Override
            public void onSwipeRight() {
                getPresenter().onSkipPreviousAction();
            }
        });

        //TextViewUtil.setSelected(mTitle, mArtist, mAlbum, mInfo);
        mListBtn.setAlpha(DISABLE_ALPHA);
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        mFirstFlg = false;
        mGlobalLayoutListener = () -> {
            if (!mFirstFlg) {
                if (mTitle != null) {
                    mTitle.startScroll();
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
}
