package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.AndroidMusicPresenter;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.AndroidMusicView;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomGestureLayout;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.SwitchTextView;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mbg.alexa.util.AssetCacheUtil;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

/**
 * Androidローカルコンテンツ再生の画面
 */
@RuntimePermissions
public class AndroidMusicFragment extends AbstractMusicPlayerFragment<AndroidMusicPresenter, AndroidMusicView>
        implements AndroidMusicView ,StatusPopupDialogFragment.Callback{
    @Inject AndroidMusicPresenter mPresenter;
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
    @Nullable
    @BindView(R.id.now_playing_button)
    ImageView mNowPlayBtn;
    @Nullable
    @BindView(R.id.music_title_text)
    ScrollTextView mTitle;
    @BindView(R.id.music_information_text) SwitchTextView mMusicInformation;
    @Nullable
    @BindView(R.id.repeat_button)
    ImageView mRepeat;
    @Nullable
    @BindView(R.id.shuffle_button)
    ImageView mShuffle;
    @BindView(R.id.progressbar) ProgressBar mProgress;
    @BindView(R.id.source_button) RelativeLayout mSourceBtn;
    @BindView(R.id.source_button_icon) ImageView mSourceBtnIcon;
    @BindView(R.id.list_button) RelativeLayout mListBtn;
    @BindView(R.id.visualizer_button) ConstraintLayout mVisualizerBtn;
    @BindView(R.id.fx_button) ConstraintLayout mFxBtn;
    @BindView(R.id.blurView) BlurView mBlurView;
    @BindView(R.id.seek_icon_back) ImageView mSeekIconBack;
    @BindView(R.id.seek_icon) ImageView mSeekIcon;
    @BindView(R.id.magnification_text) TextView mSeekText;
    @BindView(R.id.fx_text) TextView mFxText;
    @BindView(R.id.vis_text) TextView mVisText;
    @BindView(R.id.fx_eq_message) ConstraintLayout mFxEqMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.alexa_av_logo) ImageView mLogoImage;
    @BindView(R.id.web_view) WebView mWebView;
    @BindView(R.id.player_am_pm) TextView mAmPm;
    @BindView(R.id.clock) TextClock mClock;
    @Nullable @BindView(R.id.currentTimeView) TextView mCurrentTimeView;
    @Nullable @BindView(R.id.remainingTimeView) TextView mRemainingTimeView;
    private boolean mFirstFlg = false;
    private int mDisplayCount = 0;
    private String mTitleName = "";
    private String mArtist = "";
    private String mAlbum = "";
    private String mGenre = "";
    private ArrayList<String> mStringArrayList = new ArrayList<String>();
    private ArrayList<String> mAmazonMusicStringArrayList = new ArrayList<String>();
    private int mContentMaxSeconds = 0; // 再生中コンテンツの総時間(秒)
    private int mCurrentPositionSeconds = 0; // 再生済み時間(秒)
    private ViewGroup mViewGroup;
    private View mView;
    private ViewPager mViewPager;
    private CustomLinePageIndicator mLineIndicator;
    private RelativeLayout mShortCutGroup;
    private ProgressBar mProgressAlexa;
    private TextView mTextInfo1;

    private Unbinder mUnbinder;
    private static final int SEEK_TIME = 1000;
    private static final int SEEK_SPEED1 = 10;
    private static final int SEEK_SPEED2 = 40;
    private int mSeekSpeed = 0, mLastSpeed = 0;
    private Timer mSeekTimer = null;
    private int mRelSeekTime = 500;
    private float mSection = 0;
    private float mStartX = 0.0f;
    private Timer mTimer = null;
    private final Handler mHandler = new Handler();
    private PlaybackMode mCurrentPlayBackMode;
    private boolean isPosted;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ShortcutKeyAdapter mAdapter;
    private boolean mShortcutKeyVisible = false;
    private int mOrientation;
    private AudioMode mAudioMode = AudioMode.MEDIA;
    private boolean mPlayPauseEnable = false;
    private boolean mNextEnable = false;
    private boolean mPrevEnable = false;
    private boolean mPlayPauseStatusUpdateEnable = true;

    public static boolean isSetMetaData = false;
    /** デバッグフラグ. */
    private final boolean DEBUG = true;
    /** デバッグ用タグ. */
    private static final String TAG = AndroidMusicFragment.class.getSimpleName();
    private Runnable mDelayGestureFunc = new Runnable() {
        @Override
        public void run() {
            if (mCurrentPlayBackMode == PlaybackMode.PAUSE && mPlayPauseStatusUpdateEnable) {
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
    public AndroidMusicFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AndroidMusicFragment
     */
    public static AndroidMusicFragment newInstance(Bundle args) {
        AndroidMusicFragment fragment = new AndroidMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * NowPlayingListボタン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.now_playing_button)
    public void onClickNowPlaying(View view) {
        getPresenter().onNowPlayingAction();
    }

    /**
     * リストボタン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.list_button)
    public void onClickListButton(View view) {
        getPresenter().onSelectListAction();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_player_2, container, false);
        mViewGroup = (ViewGroup) view.findViewById(R.id.container_layout);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        mSection = point.x / 5;
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        return view;
    }

    /**
     * シーク更新タイマー起動
     */
    private void setSeekTimer() {
        if (mSeekSpeed != 0 && mSeekSpeed == mLastSpeed) {
            return;
        }
        if (mSeekTimer != null) {
            mSeekTimer.cancel();
            mSeekTimer = null;
        }
        mLastSpeed = mSeekSpeed;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> {
                    if (mSeekSpeed != 0) {
                        int st = 0;
                        switch (mSeekSpeed) {
                            case 39:
                                st = 9750;
                                break;
                            case 9:
                                st = 4500;
                                break;
                            case -9:
                                st = -5500;
                                break;
                            case -39:
                                st = -10250;
                                break;
                        }
                        getPresenter().onSeekAction(st);
                    }
                });
            }
        };
        int seek = Math.abs(mSeekSpeed) > 10 ? 250 : 500;
        mSeekTimer = new Timer(true);
        mSeekTimer.schedule(task, seek, seek);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AndroidMusicFragmentPermissionsDispatcher.setResumeWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AndroidMusicFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mDelayGestureFunc);
        mHandler.removeCallbacks(mDelayMessageFunc);
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mSeekTimer != null) {
            mSeekTimer.cancel();
        }
        mGestureLayout.removeOnSeekGestureListener();

        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mUnbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisplayCount = mMusicInformation.getDisplayCount();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AndroidMusicPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ANDROID_MUSIC;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setResume() {
        getPresenter().updateView(true);
    }

    @Override
    public void onClickJacketView() {
        if (mAudioMode == AudioMode.ALEXA && !mPlayPauseEnable) return;
        getPresenter().onPlayPauseAction();
    }

    @Override
    public void setNowPlayingEnabled(boolean isEnabled) {
        if(mNowPlayBtn!=null) {
            mNowPlayBtn.setEnabled(isEnabled);
            if (isEnabled) {
                mNowPlayBtn.setAlpha(1.0f);
            } else {
                mNowPlayBtn.setAlpha(0.4f);
            }
        }
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
        if (mAudioMode == AudioMode.MEDIA) {
            getPresenter().onSelectSourceAction();
        } else {
            getPresenter().onExitAlexaMode();
        }
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
    public void setMusicTitle(String title) {
        if(mTitle!=null) {
            TextViewUtil.setMarqueeTextIfChanged(mTitle, title);
        }
    }

    @Override
    public void setMusicInfo(String title,String artist,String albumName,String genre) {
        if (artist == null) {
            artist = "";
        }
        if (albumName == null) {
            albumName = "";
        }
        if (genre == null) {
            genre = "";
        }
        //onChangeMusicInformation();
        if (mTitleName.equals(title)&&mArtist.equals(artist)&&mAlbum.equals(albumName)&&mGenre.equals(genre)) {
            return;
        }else{
            mTitleName = title;
            mArtist = artist;
            mAlbum = albumName;
            mGenre = genre;
        }
        mStringArrayList = new ArrayList<>();
        if(!mAlbum.equals("")) {
            mStringArrayList.add(mAlbum);
        }
        if(!mArtist.equals("")) {
            mStringArrayList.add(mArtist);
        }
        if(!mGenre.equals("")) {
            mStringArrayList.add(mGenre);
        }
        mMusicInformation.setStringArrayList(mStringArrayList);
    }

    @Override
    public void setMusicAlbumArt(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .error(R.drawable.p0070_noimage)
                .into(mJacket);
    }

    @Override
    public void setMaxProgress(int max) {
        mProgress.setMax(max);
        if (mProgressAlexa != null) {
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
        if (mProgressAlexa != null) {
            mProgressAlexa.setProgress(curr);
        }
        mCurrentPositionSeconds = curr;
        if (mCurrentTimeView != null&&mRemainingTimeView != null) {
            updateTimeLabel(mContentMaxSeconds, mCurrentPositionSeconds, mCurrentTimeView, mRemainingTimeView);
        }
    }

    @Override
    public void setRepeatImage(SmartPhoneRepeatMode mode) {
        if(mRepeat==null)return;
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
            default:
                Timber.w("This case is impossible.");
                break;
        }
    }

    @Override
    public void setShuffleImage(ShuffleMode mode) {
        if(mShuffle==null)return;
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
    public void setPlaybackMode(PlaybackMode mode) {
        if (mode != PlaybackMode.FAST_FORWARD && mode != PlaybackMode.REWIND) {
            if (mCurrentPlayBackMode != mode) {
                if (mode == PlaybackMode.PAUSE) {
                    if (mPlayPauseStatusUpdateEnable) {
                        showStatus(GestureType.PAUSE);
                    }
                } else {
                    if (mCurrentPlayBackMode != null) {
                        if (!isPosted) {
                            android.util.Log.d(AndroidMusicFragment.class.getSimpleName(), "setPlaybackMode fade mode=" + mode.name());
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
        //一時停止状態でArtworkをタップして再生を開始した際、再生アイコン表示後に一時停止アイコンが再度表示されるため、5秒後に更新
        if (type == GestureType.PLAY&&mAudioMode==AudioMode.ALEXA) {
            mPlayPauseStatusUpdateEnable = false;
            Timer timer = new Timer(false);
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    mPlayPauseStatusUpdateEnable = true;
                    timer.cancel();
/*                    if(mCurrentPlayBackMode==PlaybackMode.PAUSE){
                        showStatus(GestureType.PAUSE);
                    }*/
                }
            };
            timer.schedule(task, 5000);
        }
        if (type.isDisplayImg()) {
            displayGesture(type.ids.get(0), type.ids.get(1));
        }
    }

    @Override
    public void setListEnabled(boolean isEnabled) {
        mListBtn.setEnabled(isEnabled);
        if (isEnabled) {
            mListBtn.setAlpha(1.0f);
        } else {
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
        if (isEnabled) {
            mCarIcon.setVisibility(View.VISIBLE);
        } else {
            mCarIcon.setVisibility(View.GONE);
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

    @Override
    public boolean isShowPlayerTabContainer() {
        if (getParentFragment() != null) {
            return ((PlayerContainerFragment) getParentFragment()).isShowPlayerTabContainer();
        }
        return false;
    }

    /**
     * Adas Icon押下イベント
     */
    @OnClick(R.id.car_icon)
    public void onClickAdasIcon() {
        getPresenter().onAdasErrorAction();
    }

    public void setNowPlayBtnState() {
        //NowPlayingListDialog閉幕時、Ripple Effectが残っていることがある
        if (mNowPlayBtn != null) mNowPlayBtn.invalidate();
    }

    /**
     * ShortCutKeyの設定
     *
     * @param keys ShortCutKeys
     */
    @Override
    public void setShortcutKeyItems(ArrayList<ShortcutKeyItem> keys) {
        if (mShortcutKeyVisible) {
            int cur = mViewPager.getCurrentItem();
            mAdapter.setShortcutKeysItems(keys);
            mLineIndicator.setCurrentItem(Math.min(cur, mAdapter.getCount()));
            mLineIndicator.setVisibility(mAdapter.getCount() <= 1 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void setAlexaNotification(boolean notification) {
        if (mAdapter != null) {
            mAdapter.setNotification(notification);
        }
    }

    @Override
    public void setShortCutButtonEnabled(boolean enabled) {
        mShortcutKeyVisible = enabled;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mViewGroup.removeAllViews();
        if (enabled) {
            mView = inflater.inflate(R.layout.fragment_player_music_alexa, mViewGroup);
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
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ViewGroup.LayoutParams lp = mTitle.getLayoutParams();
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mlp.setMargins((int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_1), mlp.topMargin, (int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_1), mlp.bottomMargin);
                mTitle.setLayoutParams(mlp);
            }
        } else {
            mView = inflater.inflate(R.layout.fragment_player_music, mViewGroup);
            mUnbinder = ButterKnife.bind(this, mView);
        }
        mLogoImage.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        setGestureLayout();

    }

    @Override
    public void setAudioMode(AudioMode audioMode) {
        mAudioMode = audioMode;
        mCurrentPlayBackMode = null;
    }

    @Override
    public void setAmazonMusicLayout() {
        mAudioMode = AudioMode.ALEXA;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mViewGroup.removeAllViews();
        mView = inflater.inflate(R.layout.fragment_player_amazon_music, mViewGroup);
        mUnbinder = ButterKnife.bind(this, mView);
        mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
        mLineIndicator = (CustomLinePageIndicator) mView.findViewById(R.id.line_indicator);
        mShortCutGroup = (RelativeLayout) mView.findViewById(R.id.shortcut_group);
        mProgressAlexa = (ProgressBar) mView.findViewById(R.id.progressbar_alexa);
        mTextInfo1 = (TextView) mView.findViewById(R.id.text_info_1);
        mMusicInformation = (SwitchTextView) mView.findViewById(R.id.music_information_text);

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
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams lp = mTextInfo1.getLayoutParams();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMargins((int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_1), mlp.topMargin, (int) getResources().getDimension(R.dimen.player_music_alexa_title_margin_1), mlp.bottomMargin);
            mTextInfo1.setLayoutParams(mlp);
        }
        mSourceBtnIcon.setImageResource(R.drawable.p1620_amzn_music_exit);
        mLogoImage.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        setListEnabled(false);
        setGestureLayout();

    }

    @Override
    public void setAmazonMusicInfo(final RenderPlayerInfoItem renderPlayerInfoItem) {
        AlexaAudioManager alexaAudioManager = AlexaAudioManager.getInstance();
        if (renderPlayerInfoItem == null) {
            mPlayPauseEnable = true;
            mNextEnable = true;
            mPrevEnable = true;
            return;
        };
        AlexaIfDirectiveItem currentItem = alexaAudioManager.getCurrentPlayItem();
        if (currentItem instanceof PlayItem) {
            String currentAudioItemId = ((PlayItem) currentItem).audioItem.audioItemId;
            String renderAudioItemId = renderPlayerInfoItem.audioItemId;
            if (!currentAudioItemId.equals(renderAudioItemId)) {
                // audioItemIdが不一致
                mPlayPauseEnable = true;
                mNextEnable = true;
                mPrevEnable = true;
                return;
            }
        }
        isSetMetaData = true;
        if (renderPlayerInfoItem.content != null) {
            AlexaIfDirectiveItem.Content content = renderPlayerInfoItem.content;
            AlexaIfDirectiveItem.Source source = null;
            {
                AlexaIfDirectiveItem.ImageStructure art = content.getArt();
                if (art != null) {
                    List<AlexaIfDirectiveItem.Source> sources = art.getSources();
                    if (sources != null && sources.size() > 0) {
                        //small→...→x-largeと仮定して、Listの最後の画像(Large)を取得
                        int logoSize = sources.size() - 1;
                        source = sources.get(logoSize);
                    }
                }
            }
            AlexaIfDirectiveItem.Source provider = null;
            {
                AlexaIfDirectiveItem.Provider aProvider = content.getProvider();
                if (aProvider != null) {
                    AlexaIfDirectiveItem.ImageStructure art = aProvider.getLogo();
                    if (art != null) {
                        List<AlexaIfDirectiveItem.Source> sources = art.getSources();
                        if (sources != null && sources.size() > 0) {
                            provider = sources.get(0);
                        }
                    }
                }
            }
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getUrl();
            }
            String logoUrl = null;
            if (provider != null) {
                logoUrl = provider.getUrl();
            }
            if (logoUrl != null) {
                if (logoUrl.endsWith(".svg")) {
                    if (DEBUG) android.util.Log.d(TAG, " --> webView, logoUrl = " + logoUrl);
                    mWebView.setWebViewClient(new WebViewClient());
                    mWebView.loadUrl(logoUrl);
                    //webView.setBackgroundColor(Color.argb(255,0, 0, 0));
                    mWebView.setBackgroundColor(0);
                    mWebView.setVisibility(View.VISIBLE);
                    mLogoImage.setVisibility(View.INVISIBLE);
                } else if (!logoUrl.endsWith(".svg")) {
                    //画像取得スレッド起動
                    if (DEBUG) android.util.Log.d(TAG, " --> logoImage, logoUrl = " + logoUrl);
                    ImageGetTask task = new ImageGetTask(mLogoImage);
                    task.execute(logoUrl);
                    mLogoImage.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.INVISIBLE);
                }
            } else {
                mLogoImage.setVisibility(View.INVISIBLE);
            }
            if (mJacket != null) {
                if (imageUrl != null) {
                    //画像取得スレッド起動
                    setMusicAlbumArt(Uri.parse(imageUrl));
                } else {
                    setMusicAlbumArt(null);
                }
            }
            //long mediaLength = content.getMediaLengthInMilliseconds();
            //setMaxProgress((int)mediaLength);
            //TextInfoの設定
            ArrayList<String> textInfoList = new ArrayList<>();
            if (content.getTitle() != null) {
                textInfoList.add(content.getTitle());
            }
            if (content.getTitleSubtext1() != null) {
                textInfoList.add(content.getTitleSubtext1());
            }
            if (content.getTitleSubtext2() != null) {
                textInfoList.add(content.getTitleSubtext2());
            }
            if (content.getHeader() != null) {
                textInfoList.add(content.getHeader());
            }
            if (content.getHeaderSubtext1() != null) {
                textInfoList.add(content.getHeaderSubtext1());
            }
            mAmazonMusicStringArrayList = new ArrayList<>();
            for(int i = 1; i < textInfoList.size(); i++){
                mAmazonMusicStringArrayList.add(textInfoList.get(i));
            }
            if(textInfoList.size()>0) {
                mTextInfo1.setText(textInfoList.get(0));
                mTextInfo1.setVisibility(View.VISIBLE);
            }
            mMusicInformation.setStringArrayList(mAmazonMusicStringArrayList);


            mPlayPauseEnable = false;
            mNextEnable = false;
            mPrevEnable = false;
            if (renderPlayerInfoItem.controls != null) {
                List<AlexaIfDirectiveItem.Control> controls = renderPlayerInfoItem.controls;
                for (AlexaIfDirectiveItem.Control control : controls) {
                    String name = control.getName();
                    boolean enabled = control.getEnabled();

                    if ("PLAY_PAUSE".equals(name)) {
                        mPlayPauseEnable = enabled;
                        //changePlayPauseButtonStatus(enabled);
                    } else if ("NEXT".equals(name)) {
                        mNextEnable = enabled;
                        //changeNextButtonStatus(enabled);
                    } else if ("PREVIOUS".equals(name)) {
                        mPrevEnable = enabled;
                        //changePrevButtonStatus(enabled);
                    }
                }
            }
        }
    }
    @Override
    public void setControlEnable(boolean isEnable){
        mPlayPauseEnable = isEnable;
        mNextEnable = isEnable;
        mPrevEnable = isEnable;
    }

    // Image取得用スレッドクラス
    class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView image;

        public ImageGetTask(ImageView _image) {
            image = _image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap image = null;
            InputStream imageIs = null;
            try {
                if (DEBUG) android.util.Log.d(TAG, "ImageGetTask#doInBackground(), params[0] = " + params[0]);
                byte[] asset = AssetCacheUtil.getAssetCache(params[0]);
                if (asset != null) {
                    image = BitmapFactory.decodeByteArray(asset, 0, asset.length);
                }
                if (DEBUG) android.util.Log.d(TAG, " -- ImageGetTask#doInBackground(), image = " + image);
            } finally {
                if (imageIs != null) {
                    try {
                        imageIs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (DEBUG) android.util.Log.d(TAG, " -- ImageGetTask#doInBackground() END");
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (DEBUG) android.util.Log.d(TAG, "ImageGetTask#onPostExecute()");
            // 取得した画像をImageViewに設定します。
            image.setImageBitmap(result);
            if (DEBUG) android.util.Log.d(TAG, " -- ImageGetTask#onPostExecute() END");
        }
    }

    private void setGestureLayout() {

        mGestureLayout.setOnSeekGestureListener(new CustomGestureLayout.OnGestureListener() {
            @Override
            public void onStartSeek(MotionEvent ev) {
                if (mAudioMode == AudioMode.ALEXA) return;
                getPresenter().onStartSeek();

                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Timber.d("SEEK start");
                        if (mShortcutKeyVisible) mProgress.setVisibility(View.VISIBLE);
                        mBlurView.setBlurEnabled(true);
                        mBlurView.setVisibility(View.VISIBLE);
                        mSeekIconBack.setVisibility(View.VISIBLE);
                        mSeekIcon.setVisibility(View.VISIBLE);
                        mSeekText.setVisibility(View.VISIBLE);
                        mSeekIconBack.setImageResource(R.drawable.p1010_gesture_play);
                        mSeekIcon.setImageResource(R.drawable.p1011_gesture_play_h);
                        mStartX = ev.getX();
                        mSeekSpeed = 0;
                        mSeekText.setText("x 1.0");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mBlurView.startAnimation(anim);
            }

            @Override
            public void onSeek(MotionEvent ev) {
                if (mAudioMode == AudioMode.ALEXA) return;
                Timber.d("SEEK : X (" + ev.getX() + ") / Y (" + ev.getY() + ")");
                float moveX = ev.getX() - mStartX;
                mSeekIconBack.setVisibility(View.VISIBLE);
                mSeekIcon.setVisibility(View.VISIBLE);
                mSeekSpeed = 0;
                if (moveX < -1 * (mSection + mSection / 2)) {
                    mSeekSpeed = -SEEK_SPEED2 + 1;
                } else if (-1 * (mSection + mSection / 2) <= moveX && moveX < -1 * (mSection / 2)) {
                    mSeekSpeed = -SEEK_SPEED1 + 1;
                } else if ((mSection / 2) < moveX && moveX <= (mSection + mSection / 2)) {
                    mSeekSpeed = SEEK_SPEED1 - 1;
                } else if ((mSection + mSection / 2) < moveX) {
                    mSeekSpeed = SEEK_SPEED2 - 1;
                }
                if (mSeekSpeed == 0) {
                    mSeekIconBack.setImageResource(R.drawable.p1010_gesture_play);
                    mSeekIcon.setImageResource(R.drawable.p1011_gesture_play_h);
                } else if (mSeekSpeed > 0) {
                    mSeekIconBack.setImageResource(R.drawable.p1060_gesture_seekff);
                    mSeekIcon.setImageResource(R.drawable.p1061_gesture_seekff_h);
                } else {
                    mSeekIconBack.setImageResource(R.drawable.p1050_gesture_seekrew);
                    mSeekIcon.setImageResource(R.drawable.p1051_gesture_seekrew_h);
                }
                setSeekTimer();
                mSeekText.setText("x " + String.valueOf(mSeekSpeed + (mSeekSpeed >= 0 ? 1 : -1)) + ".0");

            }

            @Override
            public void onEndSeek() {
                if (mAudioMode == AudioMode.ALEXA) return;
                Timber.d("SEEK end");
                getPresenter().onFinishSeek();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        mLastSpeed = 0;
                        if (mSeekTimer != null) {
                            mSeekTimer.cancel();
                            mSeekTimer = null;
                        }
                        mTimer = null;
                    }
                };
                mTimer = new Timer(true);
                mTimer.schedule(task, mRelSeekTime);
                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mSeekIconBack.setVisibility(View.GONE);
                        mSeekIcon.setVisibility(View.GONE);
                        mSeekText.setVisibility(View.GONE);
                        if (mShortcutKeyVisible) mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (mBlurView != null) {
                            mBlurView.setBlurEnabled(false);
                            mBlurView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mBlurView.startAnimation(anim);
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
                if (mAudioMode == AudioMode.ALEXA && !mNextEnable) return;
                getPresenter().onSkipNextAction();
            }

            @Override
            public void onSwipeRight() {
                if (mAudioMode == AudioMode.ALEXA && !mPrevEnable) return;
                getPresenter().onSkipPreviousAction();
            }
        });

        // ブラーの設定
        final View decorView = getActivity().getWindow().getDecorView();
        final ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
        mBlurView.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurAlgorithm(new RenderScriptBlur(getContext()))
                .blurRadius(25);
        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        getPresenter().changeTimeFormatSetting(mClock, mAmPm);
        mFirstFlg = false;
        mGlobalLayoutListener = () -> {
            if(!mFirstFlg) {
                if (mTitle != null) {
                    mTitle.startScroll();
                }
                if (mMusicInformation != null) {
                    mMusicInformation.setDisplayCount(mDisplayCount);
                    if (mAudioMode == AudioMode.ALEXA){
                        mMusicInformation.restartDisplay(mAmazonMusicStringArrayList);
                    }else {
                        mMusicInformation.restartDisplay(mStringArrayList);
                    }
                }
                mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mFirstFlg = true;
            }
        };
        mView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
       getPresenter().exitList();
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {

    }
}
