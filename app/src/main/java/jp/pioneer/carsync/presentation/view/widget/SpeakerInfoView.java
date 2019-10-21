package jp.pioneer.carsync.presentation.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.util.NumberFormatUtil;

/**
 * Advanced Setting画面のスピーカー設定情報を表示するViewです
 * Created by tsuyosh on 2016/02/19.
 */
public class SpeakerInfoView extends FrameLayout {
    public static final int SPEAKER_TYPE_FL = 0;
    public static final int SPEAKER_TYPE_FR = 1;
    public static final int SPEAKER_TYPE_RL = 2;
    public static final int SPEAKER_TYPE_RR = 3;
    public static final int SPEAKER_TYPE_SW = 4;
    public static final int SPEAKER_TYPE_HL = 5;
    public static final int SPEAKER_TYPE_HR = 6;
    public static final int SPEAKER_TYPE_ML = 7;
    public static final int SPEAKER_TYPE_MR = 8;

    public static final int TIME_ALIGNMENT_UNIT_CM = 0;
    public static final int TIME_ALIGNMENT_UNIT_INCH = 1;

    public static final int SUBWOOFER_PHASE_NORMAL = 0;
    public static final int SUBWOOFER_PHASE_REVERSE = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SUBWOOFER_PHASE_NORMAL, SUBWOOFER_PHASE_REVERSE})
    @interface SubwooferPhase{}

    private int mLayoutId = -1;

    View mRootView;

    @BindView(R.id.speakerIcon)
    RelativeLayout mSpeakerIcon;

    @BindView(R.id.speakerTypeIcon)
    TextView mSpeakerTypeIcon;

    @BindView(R.id.speakerLevelText)
    TextView mSpeakerLevelText;

    @BindView(R.id.speakerTypeLine)
    View mSpeakerTypeLine;


    @BindView(R.id.hpfInfoContainer)
    ViewGroup mHpfInfoContainer;

    @BindView(R.id.hpfFrequencyText)
    TextView mHpfFrequencyText;

    @BindView(R.id.hpfFrequencyUnitText)
    TextView mHpfFrequencyUnitText;


    @BindView(R.id.lpfInfoContainer)
    ViewGroup mLpfInfoContainer;

    @BindView(R.id.lpfFrequencyText)
    TextView mLpfFrequencyText;

    @BindView(R.id.lpfFrequencyUnitText)
    TextView mLpfFrequencyUnitText;


    @BindView(R.id.taInfoContainer)
    ViewGroup mTaInfoContainer;

    @BindView(R.id.taDistanceText)
    TextView mTaDistanceText;

    @BindView(R.id.taDistanceUnit)
    TextView mTaDistanceUnit;

    private boolean mSpeakerEnabled;
    private int mSpeakerType;
    private int mSpeakerLevel;
    private ColorStateList mSpeakerIconBackgroundTint;
    private float mHighPassFilterFrequency;
    private float mLowPassFilterFrequency;
    private float mTimeAlignment;
    private int mTimeAlignmentUnit;

    private int mSubwooferPhase;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpeakerInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr);
    }

    public SpeakerInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    public SpeakerInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeakerInfoView(Context context) {
        this(context, null);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpeakerInfoView, defStyleAttr, 0);
        mSpeakerEnabled = ta.getBoolean(R.styleable.SpeakerInfoView_speakerEnabled, false);
        mSpeakerType = ta.getInt(R.styleable.SpeakerInfoView_speakerType, SPEAKER_TYPE_FL);
        mSpeakerIconBackgroundTint = ta.getColorStateList(R.styleable.SpeakerInfoView_speakerIconBackgroundTint);
        mSpeakerLevel = ta.getInt(R.styleable.SpeakerInfoView_speakerLevel, 0);
        mHighPassFilterFrequency = ta.getFloat(R.styleable.SpeakerInfoView_highPassFilterFrequency, 0);
        mLowPassFilterFrequency = ta.getFloat(R.styleable.SpeakerInfoView_lowPassFilterFrequency, 0);
        mTimeAlignment = ta.getFloat(R.styleable.SpeakerInfoView_timeAlignment, 0);
        mTimeAlignmentUnit = ta.getInt(R.styleable.SpeakerInfoView_timeAlignmentUnit, TIME_ALIGNMENT_UNIT_CM);
        ta.recycle();

        initLayout(context);
    }

    private void initLayout(Context context) {
        loadLayout(context);
        applySpeakerEnabled();
        applySpeakerType();
        applySpeakerIconBackgroundTint();
        applySpeakerLevel();
        applyHighPassFilterFrequency();
        applyLowPassFilterFrequency();
        applyTimeAlignment();
        applySubwooferPhase();
    }

    private void loadLayout(Context context) {
        int layoutId;
        switch(mSpeakerType) {
            case SPEAKER_TYPE_FL:
            case SPEAKER_TYPE_HL:
                layoutId = R.layout.widget_speaker_info_fl;
                break;
            case SPEAKER_TYPE_FR:
            case SPEAKER_TYPE_HR:
                layoutId = R.layout.widget_speaker_info_fr;
                break;
            case SPEAKER_TYPE_RL:
            case SPEAKER_TYPE_ML:
                layoutId = R.layout.widget_speaker_info_rl;
                break;
            case SPEAKER_TYPE_RR:
            case SPEAKER_TYPE_MR:
                layoutId = R.layout.widget_speaker_info_rr;
                break;
            case SPEAKER_TYPE_SW:
                layoutId = R.layout.widget_speaker_info_sw;
                break;
            default:
                return;
        }
        if (mLayoutId == layoutId) {
            // 同じレイアウトを利用するので切り替えは不要
            return;
        }
        mLayoutId = layoutId;

        if (mRootView != null) {
            ButterKnife.bind(this);
            removeView(mRootView);
            mRootView = null;
        }
        mRootView = inflate(context, mLayoutId, this);
        ButterKnife.bind(this, mRootView);
    }

    public void setSpeakerEnabled(boolean enabled) {
        if (mSpeakerEnabled == enabled) return;
        mSpeakerEnabled = enabled;
        applySpeakerEnabled();
    }

    public void setSpeakerIconBackgroundTint(ColorStateList tintList) {
        mSpeakerIconBackgroundTint = tintList;
        applySpeakerIconBackgroundTint();
    }

    public void setUiColor(int color){
        ImageView speakerIconSelect = (ImageView) mSpeakerIcon.getChildAt(2);
        if(mSpeakerType==SPEAKER_TYPE_SW){
            speakerIconSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0201_swbtn_select_1nrm, color));
        }else{
            speakerIconSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0206_spbtn_select_1nrm, color));
        }
    }

    public void setSpeakerType(int type) {
        if (mSpeakerType == type) return;
        mSpeakerType = type;
        // レイアウトを作り直し
        initLayout(getContext());
    }

    public void setSpeakerTypeLineColor(@ColorInt int color) {
        mSpeakerTypeLine.setBackgroundColor(color);
    }

    public void setSpeakerLevel(int level) {
        if (mSpeakerLevel == level) return;
        mSpeakerLevel = level;
        applySpeakerLevel();
    }

    public void setHighPassFilterFrequency(float frequency) {
        if (mHighPassFilterFrequency == frequency) return;
        mHighPassFilterFrequency = frequency;
        applyHighPassFilterFrequency();
    }

    public void setLowPassFilterFrequency(float frequency) {
        if (mLowPassFilterFrequency == frequency) return;
        mLowPassFilterFrequency = frequency;
        applyLowPassFilterFrequency();
    }

    public void setTimeAlignment(float value, int unit) {
        if (mTimeAlignment == value && mTimeAlignmentUnit == unit) return;
        mTimeAlignment = value;
        mTimeAlignmentUnit = unit;
        applyTimeAlignment();
    }

    public void setSubwooferPhase(@SubwooferPhase int subwooferPhase) {
        if (mSubwooferPhase == subwooferPhase) return;
        mSubwooferPhase = subwooferPhase;
        applySubwooferPhase();
    }

    private void applySpeakerEnabled() {
        if (mSpeakerIcon != null) {
            if(mSpeakerEnabled) {
                mSpeakerIcon.getChildAt(0).setVisibility(View.GONE);
                mSpeakerIcon.getChildAt(1).setVisibility(View.VISIBLE);
                mSpeakerIcon.getChildAt(2).setVisibility(View.VISIBLE);
            }else{
                mSpeakerIcon.getChildAt(0).setVisibility(View.VISIBLE);
                mSpeakerIcon.getChildAt(1).setVisibility(View.GONE);
                mSpeakerIcon.getChildAt(2).setVisibility(View.GONE);
            }
        }
    }

    private void applySpeakerType() {
        String speakerType;
        switch (mSpeakerType) {
            case SPEAKER_TYPE_FL:
            case SPEAKER_TYPE_HL:
                speakerType = (mSpeakerType == SPEAKER_TYPE_FL)
                        ? getResources().getString(R.string.speaker_front_left) : getResources().getString(R.string.speaker_high_left);
                break;
            case SPEAKER_TYPE_FR:
            case SPEAKER_TYPE_HR:
                speakerType = (mSpeakerType == SPEAKER_TYPE_FR)
                        ? getResources().getString(R.string.speaker_front_right) : getResources().getString(R.string.speaker_high_right);
                break;
            case SPEAKER_TYPE_RL:
            case SPEAKER_TYPE_ML:
                speakerType = (mSpeakerType == SPEAKER_TYPE_RL)
                        ? getResources().getString(R.string.speaker_rear_left) : getResources().getString(R.string.speaker_mid_leftt);
                break;
            case SPEAKER_TYPE_RR:
            case SPEAKER_TYPE_MR:
                speakerType = (mSpeakerType == SPEAKER_TYPE_RR)
                        ? getResources().getString(R.string.speaker_rear_right) : getResources().getString(R.string.speaker_mid_right);
                break;
            case SPEAKER_TYPE_SW:
                speakerType = getResources().getString(R.string.speaker_sub_woofer);
                break;
            default:
                return;
        }
        updateSpeakerIcon();
        mSpeakerTypeIcon.setText(speakerType);
    }

    private void applySpeakerIconBackgroundTint() {
        if (mSpeakerIcon != null) {
            ViewCompat.setBackgroundTintList(mSpeakerIcon, mSpeakerIconBackgroundTint);
        }
    }

    private void updateSpeakerIcon() {
        if (mSpeakerIcon == null) return;

//        int speakerIconResId;
        switch (mSpeakerType) {
            case SPEAKER_TYPE_FL:
            case SPEAKER_TYPE_HL:
            case SPEAKER_TYPE_RL:
            case SPEAKER_TYPE_ML:
                //speakerIconResId = R.drawable.setting_speaker_left;
                break;
            case SPEAKER_TYPE_FR:
            case SPEAKER_TYPE_HR:
            case SPEAKER_TYPE_RR:
            case SPEAKER_TYPE_MR:
                //speakerIconResId = R.drawable.setting_speaker_right;
                break;
            case SPEAKER_TYPE_SW:
                switch (mSubwooferPhase) {
                    case SUBWOOFER_PHASE_NORMAL:
                        //speakerIconResId = R.drawable.setting_speaker_sw_normal;
                        mSpeakerIcon.setRotation(0);
                        break;
                    case SUBWOOFER_PHASE_REVERSE:
                        //speakerIconResId = R.drawable.setting_speaker_sw_reverse;
                        mSpeakerIcon.setRotation(180);
                        break;
                    default:
                        return;
                }
                break;
            default:
                return;
        }
        //mSpeakerIcon.setBackgroundResource(speakerIconResId);
    }

    private void applySpeakerLevel() {
        String text = (mSpeakerLevel > 0) ? "+" + mSpeakerLevel : String.valueOf(mSpeakerLevel);
        mSpeakerLevelText.setText(text);
    }

    private void applyHighPassFilterFrequency() {
        applyPassFilterFrequency(mHighPassFilterFrequency, mHpfInfoContainer, mHpfFrequencyText, mHpfFrequencyUnitText);
    }

    private void applyLowPassFilterFrequency() {
        applyPassFilterFrequency(mLowPassFilterFrequency, mLpfInfoContainer, mLpfFrequencyText, mLpfFrequencyUnitText);
    }

    private void applyPassFilterFrequency(float freq, ViewGroup container, TextView freqTextView, TextView unitTextView) {
        if (freq >= 0) {
            container.setVisibility(View.VISIBLE);
            String text, unit;
            text = NumberFormatUtil.formatFrequency(freq, false);
            if (freq >= 1000) {
                unit = getResources().getString(R.string.frequency_unit_khz);
            } else {
                unit = getResources().getString(R.string.frequency_unit_hz);
            }
            freqTextView.setText(text);
            unitTextView.setText(unit);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void applyTimeAlignment() {
        if (mTimeAlignment >= 0) {
            String text = String.format(Locale.ENGLISH, "%.1f", mTimeAlignment);
            text = text.replace(".0", "");
            //mTaDistanceText.setText(text);
            String unit = mTimeAlignmentUnit == TIME_ALIGNMENT_UNIT_INCH ? getResources().getString(R.string.ta_distance_unit_in) : getResources().getString(R.string.ta_distance_unit_cm);
            //mTaDistanceUnit.setText(unit);
            //Viewを分けると文字数変化後のViewサイズ変更が上手く行かない
            String text2 = text + unit;
            mTaDistanceText.setText(text2);
            mTaInfoContainer.setVisibility(View.VISIBLE);
        } else {
            mTaInfoContainer.setVisibility(View.GONE);
        }
    }

    private void applySubwooferPhase() {
        if (mSpeakerType != SPEAKER_TYPE_SW) return; // SW以外には関係ない
        updateSpeakerIcon();
    }
}