package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.HomePresenter;
import jp.pioneer.carsync.presentation.util.DabTextUtil;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.HdRadioTextUtil;
import jp.pioneer.carsync.presentation.util.RadioTextUtil;
import jp.pioneer.carsync.presentation.util.Rotate3dAnimation;
import jp.pioneer.carsync.presentation.util.SxmTextUtil;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.HomeView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.adapter.ShortcutKeyAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.AnalogClock;
import jp.pioneer.carsync.presentation.view.widget.BatteryView;
import jp.pioneer.carsync.presentation.view.widget.BearingGauge;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;
import jp.pioneer.carsync.presentation.view.widget.ScrollTextView;
import jp.pioneer.carsync.presentation.view.widget.SpeedMeter;

/**
 * HOMEの画面
 */

public class HomeFragment extends AbstractScreenFragment<HomePresenter, HomeView> implements HomeView {
    private static final int ANTENNA_LEVEL_COUNT = 9;
    private static final int SPEED_METER_MAX_SPEED_KM = 240; //メ-ターの最大速度(km/h)
    private static final int SPEED_METER_MAX_SPEED_MI = 150; //メ-ターの最大速度(mi/h)
    private static final double SPEED_METER_MAX_RANGE = 0.85; //メ-ターの最大範囲(240/280)
    private static final double SPEED_METER_KM_TO_MI = 0.625;
    private static final double SPEED_METER_M_TO_FT = 3.2808;
    /** メッセージ表示のディレイ時間. */
    protected static final int MESSAGE_DELAY_TIME = 1500;
    @Inject HomePresenter mPresenter;
    @BindView(R.id.car_icon) RelativeLayout mCarIcon;
    @BindView(R.id.car_icon_image) ImageView mCarIconImage;
    @BindView(R.id.car_icon_back) ImageView mCarIconBack;
    @BindView(R.id.car_icon_back_error) ImageView mCarIconBackError;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.battery) BatteryView mBatteryView;
    @BindView(R.id.line_indicator) CustomLinePageIndicator mLineIndicator;
    @BindView(R.id.speed_meter_view) RelativeLayout mSpeedMeterView;
    @BindView(R.id.speed_meter_layout) ConstraintLayout mSpeedMeterLayout;
    @BindView(R.id.speed_meter) SpeedMeter mSpeedMeter;
    @BindView(R.id.speed_meter_base) ImageView mSpeedMeterBase;
    @BindView(R.id.avr_value_text) TextView mAvrValueText;
    @BindView(R.id.speed_text) TextView mSpeedText;
    @BindView(R.id.alt_text) TextView mAltText;
    @BindView(R.id.speed_unit_text) TextView mSpeedUnitText;
    @BindView(R.id.bearing) BearingGauge mBearingGauge;
    @BindView(R.id.adas_detecting_layout) ConstraintLayout mAdasDetectingLayout;
    @BindView(R.id.pedestrian) ImageView mAdasPedestrian;
    @BindView(R.id.car) ImageView mAdasCar;
    @BindView(R.id.lane) ImageView mAdasLane;
    @BindView(R.id.shortcut_group) RelativeLayout mShortCutGroup;
    @BindView(R.id.src_message) ConstraintLayout mSrcMessage;
    @BindView(R.id.fx_eq_message_text) TextView mFxEqMessageText;
    @BindView(R.id.fx_eq_message_text_white) TextView mFxEqMessageTextWhite;
    @BindView(R.id.message_line) ImageView mMassageLine;
    @BindView(R.id.message_line_white) ImageView mMassageLineWhite;
    private View mView;
    private ImageView mJacket;
    private TextView mDeviceName;
    private ScrollTextView mTitleMarquee;
    private TextView mTitle;
    private ProgressBar mProgress;
    private RelativeLayout mClockLayout;
    private TextClock mDigitalClock;
    private View mDigitalClockView;
    private AnalogClock mAnalogClock;
    private TextView mAdasInfoText;
    private TextView mDate;
    private Unbinder mUnbinder;
    private ShortcutKeyAdapter mAdapter;
    private String mTitleText = "";
    private int mOrientation;
    private TextView mBand;
    private TextView mPrePch;
    private TextView mFrequency;
    private TextView mFrequencyNoDecimal;
    private TextView mFrequencyDecimal;
    private TextView mFrequencyUnit;
    private TextView mChannel;
    private ScrollTextView mServiceName;
    private TextView mPch;
    private ImageView mAntenna;
    private static final Handler mHandler = new Handler();
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Runnable mRunnable;
    private DistanceUnit mDistanceUnit;
    private float mBatteryPct = 0;
    private boolean mIsCharge = false;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private TextView mAmPm;
    private Runnable mDelayMessageFunc = new Runnable() {
        @Override
        public void run() {
            if (mSrcMessage != null)
                mSrcMessage.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * コンストラクタ
     */
    public HomeFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return HomeFragment
     */
    public static HomeFragment newInstance(Bundle args) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mUnbinder = ButterKnife.bind(this, view);
        mView = view;
        Configuration config = getContext().getResources().getConfiguration();
        mOrientation = config.orientation;
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
        getPresenter().setArgument(getArguments());
        mDistanceUnit = getPresenter().getDistanceUnit();
        if (mDistanceUnit == DistanceUnit.FEET_MILE) {
            mSpeedUnitText.setText(getString(R.string.unt_002));
            mSpeedMeterBase.setImageResource(R.drawable.p0981_speedmeter_basemile);
        } else {
            mSpeedUnitText.setText(getString(R.string.unt_001));
            mSpeedMeterBase.setImageResource(R.drawable.p0980_speedmeter_base);
        }
        setSpeedMeterView();
        mBatteryView.setBatteryPct(mBatteryPct);
        mBatteryView.setCharge(mIsCharge);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onDestroyView() {
        mBatteryPct = mBatteryView.getBatteryPct();
        mIsCharge = mBatteryView.isCharge();
        //TextClock that was originally registered here. Are you missing a call to unregisterReceiver()?エラーの回避
        mClockLayout.removeAllViews();
        mTimer.cancel();
        mTimerTask.cancel();
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(mDelayMessageFunc);
        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mSpeedMeterView.setOnClickListener(null);
        mClockLayout.setOnClickListener(null);
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected HomePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.HOME;
    }

    @Override
    public void setDistanceUnit(DistanceUnit distanceUnit) {
        mDistanceUnit = distanceUnit;
    }

    /**
     * Playerエリアの設定
     *
     * @param type 現在のSource
     */
    @Override
    public void setPlayerView(MediaSourceType type) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        RelativeLayout layout = (RelativeLayout) mView.findViewById(R.id.player_view);
        // レイアウトのビューをすべて削除する
        layout.removeAllViews();
        switch (type) {
            case APP_MUSIC:
                inflater.inflate(R.layout.element_home_player, layout);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mDeviceName = (TextView) layout.findViewById(R.id.device_name);
                mDeviceName.setVisibility(View.INVISIBLE);
                break;
            case BT_AUDIO:
                inflater.inflate(R.layout.element_home_player, layout);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mDeviceName = (TextView) layout.findViewById(R.id.device_name);
                mDeviceName.setVisibility(View.VISIBLE);
                break;
            case RADIO:
            case TI:
            case HD_RADIO:
                inflater.inflate(R.layout.element_home_player_radio, layout);
                mBand = (TextView) layout.findViewById(R.id.band_text);
                mPrePch = (TextView) layout.findViewById(R.id.pre_pch_text);
                mFrequency = (TextView) layout.findViewById(R.id.frequency_text);
                mFrequencyNoDecimal = (TextView) layout.findViewById(R.id.frequency_no_decimal_text);
                mFrequencyDecimal = (TextView) layout.findViewById(R.id.frequency_decimal_text);
                mFrequencyUnit = (TextView) layout.findViewById(R.id.frequency_unit_text);
                mPch = (TextView) layout.findViewById(R.id.pch_text);
                mAntenna = (ImageView) layout.findViewById(R.id.antenna_icon);
                mChannel = (TextView) layout.findViewById(R.id.channel_number_text);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mServiceName = (ScrollTextView) layout.findViewById(R.id.service_name);
                mProgress.setVisibility(View.GONE);
                mPrePch.setVisibility(View.INVISIBLE);
                mServiceName.setVisibility(View.INVISIBLE);
                break;
            case SIRIUS_XM:
                inflater.inflate(R.layout.element_home_player_radio, layout);
                mBand = (TextView) layout.findViewById(R.id.band_text);
                mPrePch = (TextView) layout.findViewById(R.id.pre_pch_text);
                mFrequency = (TextView) layout.findViewById(R.id.frequency_text);
                mFrequencyNoDecimal = (TextView) layout.findViewById(R.id.frequency_no_decimal_text);
                mFrequencyDecimal = (TextView) layout.findViewById(R.id.frequency_decimal_text);
                mFrequencyUnit = (TextView) layout.findViewById(R.id.frequency_unit_text);
                mPch = (TextView) layout.findViewById(R.id.pch_text);
                mAntenna = (ImageView) layout.findViewById(R.id.antenna_icon);
                mChannel = (TextView) layout.findViewById(R.id.channel_number_text);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mServiceName = (ScrollTextView) layout.findViewById(R.id.service_name);
                mPrePch.setVisibility(View.INVISIBLE);
                mServiceName.setVisibility(View.INVISIBLE);
                break;
            case DAB:
                inflater.inflate(R.layout.element_home_player_radio, layout);
                mBand = (TextView) layout.findViewById(R.id.band_text);
                mPrePch = (TextView) layout.findViewById(R.id.pre_pch_text);
                mFrequency = (TextView) layout.findViewById(R.id.frequency_text);
                mFrequencyNoDecimal = (TextView) layout.findViewById(R.id.frequency_no_decimal_text);
                mFrequencyDecimal = (TextView) layout.findViewById(R.id.frequency_decimal_text);
                mFrequencyUnit = (TextView) layout.findViewById(R.id.frequency_unit_text);
                mPch = (TextView) layout.findViewById(R.id.pch_text);
                mAntenna = (ImageView) layout.findViewById(R.id.antenna_icon);
                mChannel = (TextView) layout.findViewById(R.id.channel_number_text);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mServiceName = (ScrollTextView) layout.findViewById(R.id.service_name);
                mPrePch.setVisibility(View.INVISIBLE);
                mPch.setVisibility(View.INVISIBLE);
                mFrequency.setVisibility(View.GONE);
                mFrequencyDecimal.setVisibility(View.GONE);
                mFrequencyNoDecimal.setVisibility(View.GONE);
                mFrequencyUnit.setVisibility(View.GONE);
                mChannel.setVisibility(View.GONE);
                mServiceName.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                break;
            default:
                inflater.inflate(R.layout.element_home_player, layout);
                mProgress = (ProgressBar) layout.findViewById(R.id.progressbar);
                mDeviceName = (TextView) layout.findViewById(R.id.device_name);
                mDeviceName.setVisibility(View.INVISIBLE);
                break;
        }
        mJacket = (ImageView) layout.findViewById(R.id.jacket_view);
        mTitleMarquee = (ScrollTextView) layout.findViewById(R.id.music_title_text);
        mGlobalLayoutListener = () -> {
            mTitleMarquee.startScroll();
            if(mServiceName!=null) {
                mServiceName.startScroll();
            }
            mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        mView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    /**
     * スピードメーターの設定
     */
    private void setSpeedMeterView() {
        mTimer = new Timer();

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                CarRunningStatus status = getPresenter().getCarRunningStatus();
                String altUnit;
                int speedValue, avrValue;
                int altValue;
                if (mSpeedMeter != null) {
                    if (mDistanceUnit == DistanceUnit.FEET_MILE) {
                        speedValue = (int) (status.speedForSpeedMeter * SPEED_METER_KM_TO_MI);
                        mSpeedMeter.seekLevel = (int) (speedValue * 360 / SPEED_METER_MAX_SPEED_MI * SPEED_METER_MAX_RANGE);
                        avrValue = (int) (status.averageSpeed * SPEED_METER_KM_TO_MI);
                        altValue = (int) (status.altitude * SPEED_METER_M_TO_FT);
                        altUnit = getString(R.string.unt_004);
                    } else {
                        speedValue = (int) status.speedForSpeedMeter;
                        mSpeedMeter.seekLevel = (int) (speedValue * 360 / SPEED_METER_MAX_SPEED_KM * SPEED_METER_MAX_RANGE);
                        avrValue = (int) status.averageSpeed;
                        altValue = (int) status.altitude;
                        altUnit = getString(R.string.unt_003);
                    }
                    String speedText = (speedValue < 0) ? "" : String.valueOf(speedValue);
                    String avrText = (avrValue < 0) ? "" : String.valueOf(avrValue);
                    String altText = (Double.compare(status.altitude, Double.MIN_VALUE)==0 )? getString(R.string.hom_028) : getString(R.string.hom_028) + altValue + altUnit;
                    mRunnable = () -> {
                        if (mSpeedMeter != null) {
                            //電話帳表示中はSpeedMeterを表示しない（GalaxyJ3Proでちらつくため）
                            MainActivity activity = (MainActivity) getActivity();
                            if (activity.isShowContactContainer() || activity.isShowSearchContainer()) {
                                mSpeedMeter.setVisibility(View.INVISIBLE);
                                return;
                            }
                            mSpeedMeter.setVisibility(View.VISIBLE);
                            mSpeedMeter.invalidate();
                            mSpeedText.setText(speedText);
                            mAvrValueText.setText(avrText);
                            mAltText.setText(altText);
                            if (status.bearing >= 0) {
                                if (mBearingGauge.getVisibility() != View.VISIBLE) {
                                    mBearingGauge.setVisibility(View.VISIBLE);
                                }
                                mBearingGauge.setBearingLevel(status.bearing);
                            } else {
                                if (mBearingGauge.getVisibility() != View.INVISIBLE) {
                                    mBearingGauge.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                        if(mAdasInfoText!=null){
                            mAdasInfoText.setText(getPresenter().getAdasInfoText());
                        }
                    };
                    mHandler.post(mRunnable);
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 100);
        // エリアのクリックイベント
        mSpeedMeterView.setOnClickListener(v -> {
            if (mSpeedMeterLayout.getVisibility() == View.VISIBLE) {
                getPresenter().onChangeSpeedMeterAction(true);
                mSpeedMeterLayout.setVisibility(View.INVISIBLE);
                mAdasDetectingLayout.setVisibility(View.VISIBLE);
            } else {
                getPresenter().onChangeSpeedMeterAction(false);
                mSpeedMeterLayout.setVisibility(View.VISIBLE);
                mAdasDetectingLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void setSpeedMeterViewType(boolean enabled, boolean adasView) {
        mSpeedMeterView.setEnabled(enabled);
        if (adasView) {
            mSpeedMeterLayout.setVisibility(View.INVISIBLE);
            mAdasDetectingLayout.setVisibility(View.VISIBLE);
        } else {
            mSpeedMeterLayout.setVisibility(View.VISIBLE);
            mAdasDetectingLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Clockエリアの設定
     *
     * @param type 現在のtype
     */
    @Override
    public void setClockView(int type) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mClockLayout = (RelativeLayout) mView.findViewById(R.id.clock_view);
        // レイアウトのビューをすべて削除する
        mClockLayout.removeAllViews();
        if(type ==2){
            inflater.inflate(R.layout.element_home_adas_info, mClockLayout);
            mAdasInfoText = (TextView) mClockLayout.findViewById(R.id.adas_info);
        }else {
        inflater.inflate(R.layout.element_home_clock, mClockLayout);
        mDigitalClock = (TextClock) mClockLayout.findViewById(R.id.digital_clock);
        mDigitalClockView = (View) mClockLayout.findViewById(R.id.digital_clock_view);
        mAmPm = (TextView) mClockLayout.findViewById(R.id.am_pm);

        // 12時間表示の場合、日本とそれ以外で表示形式を変える
        if (getPresenter().getTimeFormatSetting() == TimeFormatSetting.TIME_FORMAT_24) {
            mDigitalClock.setFormat12Hour("kk\nmm");
            mDigitalClock.setFormat24Hour("kk\nmm");
            mAmPm.setVisibility(View.GONE);
        } else {
            if (type == 0) {
                mAmPm.setVisibility(View.VISIBLE);
            }

            if (AppUtil.isZero2ElevenIn12Hour(getContext())) {
                mDigitalClock.setFormat12Hour("KK\nmm");
                mDigitalClock.setFormat24Hour("KK\nmm");
            } else {
                mDigitalClock.setFormat12Hour("hh\nmm");
                mDigitalClock.setFormat24Hour("hh\nmm");
            }
        }

        mAnalogClock = (AnalogClock) mClockLayout.findViewById(R.id.analog_clock);
        mDate = (TextView) mClockLayout.findViewById(R.id.date);
        if (type == 0) {
            mDigitalClockView.setVisibility(View.VISIBLE);
            mAnalogClock.setVisibility(View.INVISIBLE);
        } else if (type == 1) {
            mDigitalClockView.setVisibility(View.INVISIBLE);
            mAnalogClock.setVisibility(View.VISIBLE);
        }
        //日付表示
        String date_text;
        char[] order = android.text.format.DateFormat.getDateFormatOrder(getContext());
        StringBuilder sb = new StringBuilder();
        sb.append("E");
        for (char item : order) {
            switch (item) {
                case 'd':
                    sb.append(" d");
                    break;
                case 'M':
                    sb.append(" MMM");
                    break;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(sb.toString(), Locale.ENGLISH);
        String dateText = sdf.format(Calendar.getInstance().getTime());
        date_text = String.format("%s", dateText);
        mDate.setText(date_text);

        // 時計エリアのクリックイベント
        mClockLayout.setOnClickListener(v -> {
            if (mDigitalClockView.getVisibility() == View.VISIBLE) {
                getPresenter().onChangeClockAction(1);
                startAnimation(mDigitalClockView, mAnalogClock); // Digital -> Analog
            } else {
                getPresenter().onChangeClockAction(0);
                startAnimation(mAnalogClock, mDigitalClockView); // Analog -> Digital
            }
        });
        }
    }

    private void startAnimation(final View out, final View in) {
        final Rotate3dAnimation feedIn = new Rotate3dAnimation(-90.0f, 0f, in.getWidth() * 0.5f, in.getHeight() * 0.5f, 0.0f, false);
        feedIn.setDuration(500);
        Rotate3dAnimation feedOut = new Rotate3dAnimation(0f, 90.0f, out.getWidth() * 0.5f, out.getHeight() * 0.5f, 0f, false);
        feedOut.setDuration(500);
        feedOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                out.setVisibility(View.INVISIBLE);
                in.setVisibility(View.VISIBLE);
                in.startAnimation(feedIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        out.startAnimation(feedOut);
    }

    /**
     * ShortCutKeyの設定
     *
     * @param keys ShortCutKeys
     */
    @Override
    public void setShortcutKeyItems(ArrayList<ShortcutKeyItem> keys) {
        int cur = mViewPager.getCurrentItem();
        mAdapter.setShortcutKeysItems(keys);
        mLineIndicator.setCurrentItem(Math.min(cur, mAdapter.getCount()));
        mLineIndicator.setVisibility(mAdapter.getCount() <= 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void setAlexaNotification(boolean notification) {
        if (mAdapter != null) {
            mAdapter.setNotification(notification);
        }
    }

    @Override
    public void setShortCutButtonEnabled(boolean enabled) {
        if (enabled) {
            mShortCutGroup.setVisibility(View.VISIBLE);
        } else {
            mShortCutGroup.setVisibility(View.GONE);
        }
    }

    /*
    @OnClick(R.id.setting_button)
    public void onClickSettingButton() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SettingsContainerFragment.KEY_RETURN_SCREEN_WHEN_CLOSE, getScreenId());
        getPresenter().onSettingsAction(bundle);
    }
    */

    /**
     * HOMEボタン押下イベント
     */
    @OnClick(R.id.setting_button_on_home_screen)
    public void onClickHomeButton() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SettingsContainerFragment.KEY_RETURN_SCREEN_WHEN_CLOSE, getScreenId());
        getPresenter().onSettingsAction(bundle);
    }


    /**
     * 楽曲タイトルの設定
     *
     * @param title 曲のタイトル
     */
    @Override
    public void setMusicTitle(String title) {
        if (title != null) {
            TextViewUtil.setMarqueeTextIfChanged(mTitleMarquee, title);
        }
    }

    /**
     * アルバムアートの設定
     *
     * @param uri アルバムアートURI
     */
    @Override
    public void setMusicAlbumArt(Uri uri) {
        if (uri == null) {
            mJacket.setImageDrawable(null);
        } else {
            Glide.with(getContext())
                    .load(uri)
                    .error(R.drawable.p0070_noimage)
                    .into(mJacket);
        }
    }

    /**
     * イメージアートの設定
     *
     * @param resource イメージリソース
     */
    @Override
    public void setCenterImage(int resource) {
        if (resource == 0) {
            mJacket.setImageDrawable(null);
        } else {
            mJacket.setImageResource(resource);
        }
    }

    /**
     * 接続デバイス名の設定
     *
     * @param audioDeviceName 接続デバイス名
     */
    @Override
    public void setAudioDeviceName(String audioDeviceName) {
        TextViewUtil.setTextIfChanged(mDeviceName, audioDeviceName);
    }

    /**
     * プログレスバーの最大値の設定
     *
     * @param max プログレスバーの最大値
     */
    @Override
    public void setMaxProgress(int max) {
        mProgress.setMax(max);
        if (max == 0) {
            if (mProgress.getVisibility() != View.INVISIBLE) {
                mProgress.setVisibility(View.INVISIBLE);
            }
        } else {
            if (mProgress.getVisibility() != View.VISIBLE) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * プログレスバーの現在値の設定
     *
     * @param curr プログレスバーの現在値
     */
    @Override
    public void setCurrentProgress(int curr) {
        mProgress.setProgress(curr);
    }

    /**
     * ラジオ再生情報の設定
     *
     * @param status 再生状態
     * @param info   ラジオ情報
     */
    @Override
    public void setRadioInfo(CarDeviceStatus status, RadioInfo info) {
        TextViewUtil.setMarqueeTextIfChanged(mBand, RadioTextUtil.getBandName(getContext(), info));
        String frequency = FrequencyUtil.toString(getContext(), info.currentFrequency, info.frequencyUnit);
        if (info.tunerStatus == TunerStatus.BSM
                || info.tunerStatus == TunerStatus.PTY_SEARCH
                || info.tunerStatus == TunerStatus.SEEK) {
            frequency = null;
        }
        if (frequency == null) {
            mFrequency.setText(null);
            mFrequencyDecimal.setText(null);
            mFrequencyNoDecimal.setText(null);
            mFrequencyUnit.setText(null);
        } else {
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
        mFrequencyUnit.setVisibility(View.VISIBLE);
        mChannel.setVisibility(View.GONE);
        float level = (float) info.antennaLevel / info.maxAntennaLevel;
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        switch (levelInt) {
            case 0:
                mAntenna.setImageResource(R.drawable.p0490_antenna);
                break;
            case 1:
                mAntenna.setImageResource(R.drawable.p0491_antenna);
                break;
            case 2:
                mAntenna.setImageResource(R.drawable.p0492_antenna);
                break;
            case 3:
                mAntenna.setImageResource(R.drawable.p0493_antenna);
                break;
            case 4:
                mAntenna.setImageResource(R.drawable.p0494_antenna);
                break;
            case 5:
                mAntenna.setImageResource(R.drawable.p0495_antenna);
                break;
            case 6:
                mAntenna.setImageResource(R.drawable.p0496_antenna);
                break;
            case 7:
                mAntenna.setImageResource(R.drawable.p0497_antenna);
                break;
            case 8:
                mAntenna.setImageResource(R.drawable.p0498_antenna);
                break;
            default:
                break;
        }
        mBand.setVisibility(View.VISIBLE);
    }
    /**
     * HDラジオ再生情報の設定
     *
     * @param status 再生状態
     * @param info   ラジオ情報
     */
    @Override
    public void setHdRadioInfo(CarDeviceStatus status, HdRadioInfo info) {
        TextViewUtil.setMarqueeTextIfChanged(mBand, HdRadioTextUtil.getBandName(getContext(), info));
        String frequency = FrequencyUtil.toString(getContext(), info.currentFrequency, info.frequencyUnit);
        if (info.tunerStatus == TunerStatus.BSM
                || info.tunerStatus == TunerStatus.PTY_SEARCH
                || info.tunerStatus == TunerStatus.SEEK) {
            frequency = null;
        }
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
        mFrequencyUnit.setVisibility(View.VISIBLE);
        mChannel.setVisibility(View.GONE);
        float level = (float) info.antennaLevel / info.maxAntennaLevel;
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        switch (levelInt) {
            case 0:
                mAntenna.setImageResource(R.drawable.p0490_antenna);
                break;
            case 1:
                mAntenna.setImageResource(R.drawable.p0491_antenna);
                break;
            case 2:
                mAntenna.setImageResource(R.drawable.p0492_antenna);
                break;
            case 3:
                mAntenna.setImageResource(R.drawable.p0493_antenna);
                break;
            case 4:
                mAntenna.setImageResource(R.drawable.p0494_antenna);
                break;
            case 5:
                mAntenna.setImageResource(R.drawable.p0495_antenna);
                break;
            case 6:
                mAntenna.setImageResource(R.drawable.p0496_antenna);
                break;
            case 7:
                mAntenna.setImageResource(R.drawable.p0497_antenna);
                break;
            case 8:
                mAntenna.setImageResource(R.drawable.p0498_antenna);
                break;
            default:
                break;
        }
        mBand.setVisibility(View.VISIBLE);
    }
    /**
     * DAB再生情報の設定
     *
     * @param status 再生状態
     * @param info   DAB情報
     */
    @Override
    public void setDabInfo(CarDeviceStatus status, DabInfo info) {
        TextViewUtil.setMarqueeTextIfChanged(mBand, DabTextUtil.getBandName(getContext(), info));
        TextViewUtil.setMarqueeTextIfChanged(mServiceName, DabTextUtil.getServiceComponentLabelForPlayer(getContext(),info));
        float level = (float) info.antennaLevel / info.maxAntennaLevel;
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        switch (levelInt) {
            case 0:
                mAntenna.setImageResource(R.drawable.p0490_antenna);
                break;
            case 1:
                mAntenna.setImageResource(R.drawable.p0491_antenna);
                break;
            case 2:
                mAntenna.setImageResource(R.drawable.p0492_antenna);
                break;
            case 3:
                mAntenna.setImageResource(R.drawable.p0493_antenna);
                break;
            case 4:
                mAntenna.setImageResource(R.drawable.p0494_antenna);
                break;
            case 5:
                mAntenna.setImageResource(R.drawable.p0495_antenna);
                break;
            case 6:
                mAntenna.setImageResource(R.drawable.p0496_antenna);
                break;
            case 7:
                mAntenna.setImageResource(R.drawable.p0497_antenna);
                break;
            case 8:
                mAntenna.setImageResource(R.drawable.p0498_antenna);
                break;
            default:
                break;
        }
        mBand.setVisibility(View.VISIBLE);
        if(info.timeShiftMode){
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setMax(info.totalBufferTime);
            mProgress.setProgress(info.currentPosition);
            mProgress.setSecondaryProgress(info.currentBufferTime);
            mAntenna.setVisibility(View.GONE);
        }else{
            mProgress.setVisibility(View.GONE);
            mAntenna.setVisibility(View.VISIBLE);
        }
    }

    /**
     * TI再生情報の設定
     */
    @Override
    public void setTiInfo() {
        mPrePch.setVisibility(View.GONE);
        mPch.setVisibility(View.GONE);
        mBand.setVisibility(View.GONE);
    }

    /**
     * Sxm再生情報の設定
     *
     * @param status 再生状態
     * @param info   Sxm情報
     */
    @Override
    public void setSxmInfo(CarDeviceStatus status, SxmMediaInfo info) {
        if(info.inReplayMode && !info.inTuneMix) {
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setMax(info.totalBufferTime);
            mProgress.setProgress(info.currentPosition);
            mProgress.setSecondaryProgress(info.currentBufferTime);
        } else {
            mProgress.setVisibility(View.GONE);
        }

        TextViewUtil.setMarqueeTextIfChanged(mBand, SxmTextUtil.getBandName(info));
        mFrequency.setVisibility(View.GONE);
        mFrequencyDecimal.setVisibility(View.GONE);
        mFrequencyUnit.setVisibility(View.GONE);
        mChannel.setVisibility(View.VISIBLE);
        TextViewUtil.setMarqueeTextIfChanged(mChannel, SxmTextUtil.getCurrentChannel(info));
        float level = (float) info.antennaLevel / info.maxAntennaLevel;
        int levelInt = (int) (level * (ANTENNA_LEVEL_COUNT - 1));
        switch (levelInt) {
            case 0:
                mAntenna.setImageResource(R.drawable.p0490_antenna);
                break;
            case 1:
                mAntenna.setImageResource(R.drawable.p0491_antenna);
                break;
            case 2:
                mAntenna.setImageResource(R.drawable.p0492_antenna);
                break;
            case 3:
                mAntenna.setImageResource(R.drawable.p0493_antenna);
                break;
            case 4:
                mAntenna.setImageResource(R.drawable.p0494_antenna);
                break;
            case 5:
                mAntenna.setImageResource(R.drawable.p0495_antenna);
                break;
            case 6:
                mAntenna.setImageResource(R.drawable.p0496_antenna);
                break;
            case 7:
                mAntenna.setImageResource(R.drawable.p0497_antenna);
                break;
            case 8:
                mAntenna.setImageResource(R.drawable.p0498_antenna);
                break;
            default:
                break;
        }
    }

    /**
     * プリセットチャンネルの設定
     *
     * @param pch プリセットチャンネル
     */
    @Override
    public void setPch(int pch) {
        if (pch == -1) {
            mPrePch.setVisibility(View.INVISIBLE);
            mPch.setText(null);
        } else {
            mPrePch.setVisibility(View.VISIBLE);
            TextViewUtil.setMarqueeTextIfChanged(mPch, String.valueOf(pch));
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        if (mAnalogClock != null) {
            mAnalogClock.setColor(color);
        }
        mLineIndicator.setColor(color);
        mAdapter.setColor(color);
    }

    @Override
    public void setAdasEnabled(boolean isEnabled) {
        if (isEnabled) {
            mCarIcon.setVisibility(View.VISIBLE);
        } else {
            mCarIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setAdasIcon(int status) {
        switch (status) {
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
    public void setAdasDetection(boolean pedestrian, boolean car, boolean leftLane,boolean rightLane) {
        if (mAdasPedestrian != null) {
            if (pedestrian) {
                mAdasPedestrian.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.illumi_color_orange));
            } else {
                mAdasPedestrian.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey600));
            }
        }
        if (mAdasCar != null) {
            if (car) {
                mAdasCar.setImageResource(R.drawable.p1811_adasstatus_fcw_on);
            } else {
                mAdasCar.setImageResource(R.drawable.p1812_adasstatus_fcw_off);
            }
        }
        if (mAdasLane != null) {
            if (leftLane&&rightLane) {
                mAdasLane.setImageResource(R.drawable.p1813_adasstatus_ldw_on);
            } else if(leftLane) {
                mAdasLane.setImageResource(R.drawable.p1815_adasstatus_ldw_on_left);
            } else if(rightLane) {
                mAdasLane.setImageResource(R.drawable.p1816_adasstatus_ldw_on_right);
            } else {
                mAdasLane.setImageResource(R.drawable.p1814_adasstatus_ldw_off);
            }
        }
    }

    /**
     * PlayerView押下イベント
     */
    @OnClick(R.id.player_view)
    public void onClickPlayerView() {
        getPresenter().onPlayerAction();
    }

    /**
     * Adas Icon押下イベント
     */
    @OnClick(R.id.car_icon)
    public void onClickAdasIcon() {
        getPresenter().onAdasErrorAction();
    }

    @Override
    public void displayVoiceMessage(String str) {
        mHandler.removeCallbacks(mDelayMessageFunc);
        mFxEqMessageTextWhite.setText(str);
        mFxEqMessageText.setVisibility(View.INVISIBLE);
        mFxEqMessageTextWhite.setVisibility(View.VISIBLE);
        mSrcMessage.setVisibility(View.VISIBLE);
        mMassageLine.setVisibility(View.INVISIBLE);
        mMassageLineWhite.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mDelayMessageFunc, MESSAGE_DELAY_TIME);
    }
}
