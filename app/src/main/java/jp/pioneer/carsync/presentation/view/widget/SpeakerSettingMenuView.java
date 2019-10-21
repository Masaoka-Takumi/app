package jp.pioneer.carsync.presentation.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter;

/**
 * Advanced Menuのポップアップメニュー用のView
 * Created by tsuyosh on 2016/02/23.
 */
public class SpeakerSettingMenuView extends FrameLayout {
    public static final int SPEAKER_TYPE_FL = 0;
    public static final int SPEAKER_TYPE_FR = 1;
    public static final int SPEAKER_TYPE_RL = 2;
    public static final int SPEAKER_TYPE_RR = 3;
    public static final int SPEAKER_TYPE_SW = 4;
    public static final int SPEAKER_TYPE_HL = 5;
    public static final int SPEAKER_TYPE_HR = 6;
    public static final int SPEAKER_TYPE_ML = 7;
    public static final int SPEAKER_TYPE_MR = 8;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SPEAKER_TYPE_FL, SPEAKER_TYPE_FR, SPEAKER_TYPE_RL, SPEAKER_TYPE_RR, SPEAKER_TYPE_SW,
            SPEAKER_TYPE_HL, SPEAKER_TYPE_HR, SPEAKER_TYPE_ML, SPEAKER_TYPE_MR
    })
    public @interface SpeakerType {
    }

    @BindView(R.id.titleText)
    TextView mTitleText;

    @BindView(R.id.downButton)
    CustomButton mDownButton;

    @BindView(R.id.speakerTypeLine)
    View mSpeakerTypeLine;

    @BindView(R.id.speakerTypeIcon)
    ImageView mSpeakerIcon;

    @BindView(R.id.speakerTypeIconSw)
    ImageView mSpeakerIconSw;

    @BindView(R.id.list)
    public MyListView mListView;

    private Animation mSlideUpAnimation;
    private Animation mSlideDownAnimation;
    private OnMenuDownListener mMenuDownListener;
    private int mSpeakerType;

    private boolean busy = false;
    private boolean mIsClosed = false;

    public boolean isBusy() {            // AdvancedSettingIf
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpeakerSettingMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    public SpeakerSettingMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public SpeakerSettingMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeakerSettingMenuView(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View root = inflate(context, R.layout.widget_speaker_setting_menu, this);
        ButterKnife.bind(this, root);

        mSlideUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        mSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mSlideDownAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        mSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mListView != null) {
                    ListAdapter adapter = mListView.getAdapter();
                    if (adapter instanceof SpeakerSettingMenuAdapter) {
                        ((SpeakerSettingMenuAdapter) adapter).setShowing(false);
                    }
                    mListView.setAdapter(null);
                }

                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        TypedArray ta = context.obtainStyledAttributes(
                attrs, R.styleable.SpeakerSettingMenuView, defStyleAttr, 0);
        mSpeakerType = ta.getInt(R.styleable.SpeakerSettingMenuView_speakerType, SPEAKER_TYPE_FL);
        int color = ta.getColor(R.styleable.SpeakerSettingMenuView_speakerTypeLineColor, Color.WHITE);
        setSpeakerTypeLineColor(color);
        ta.recycle();

        applySpeakerType();
    }

    public void setOnMenuDownListener(OnMenuDownListener listener) {
        mMenuDownListener = listener;
    }

    public interface OnMenuDownListener {
        void onMenuDown();
    }

    public void slideUp() {
        startAnimation(mSlideUpAnimation);
        mIsClosed=false;
        mDownButton.setEnabled(true);
        mListView.setEnabled(true);
    }

    public void slideDown() {
        if(!mIsClosed) {
            startAnimation(mSlideDownAnimation);
            //×ボタン連続押下での反応を防ぐ
            mListView.setEnabled(false);
            mDownButton.setEnabled(false);
        }
        mIsClosed = true;
    }

    @OnClick(R.id.downButton)
    void onDownButtonClicked() {
        slideDown();
        mMenuDownListener.onMenuDown();
    }

    public void setAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);

        if (adapter instanceof SpeakerSettingMenuAdapter) {
            ((SpeakerSettingMenuAdapter) adapter).setShowing(true);
        }
    }

    public void setSpeakerType(@SpeakerType int type) {
        if (mSpeakerType == type) return;
        mSpeakerType = type;
        applySpeakerType();
    }

    public void setSpeakerTypeLineColor(@ColorInt int color) {
        mSpeakerTypeLine.setBackgroundColor(color);
    }

    private void applySpeakerType() {
        int icon;
        int title;
        int angle;
        switch (mSpeakerType) {
            case SPEAKER_TYPE_FL:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_058;
                angle = 0;
                break;
            case SPEAKER_TYPE_FR:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_059;
                angle = 180;
                break;
            case SPEAKER_TYPE_RL:
                icon = R.drawable.p0230_spicon;
                title = R.string.set_178;
                angle = 0;
                break;
            case SPEAKER_TYPE_RR:
                icon = R.drawable.p0230_spicon;
                title = R.string.set_179;
                angle = 180;
                break;
            case SPEAKER_TYPE_HL:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_241;
                angle = 0;
                break;
            case SPEAKER_TYPE_HR:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_242;
                angle = 180;
                break;
            case SPEAKER_TYPE_ML:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_243;
                angle = 0;
                break;
            case SPEAKER_TYPE_MR:
                icon = R.drawable.p0230_spicon;
                title = R.string.val_244;
                angle = 180;
                break;
            case SPEAKER_TYPE_SW:
                icon = R.drawable.p0230_spicon;
                title = R.string.set_206;
                angle = -90;
                break;
            default:
                return;
        }
        mTitleText.setText(title);

        mSpeakerIcon.setVisibility(mSpeakerType == SPEAKER_TYPE_SW ? View.GONE : View.VISIBLE);
        mSpeakerIconSw.setVisibility(mSpeakerType == SPEAKER_TYPE_SW ? View.VISIBLE : View.GONE);
        if (mSpeakerType != SPEAKER_TYPE_SW) {
            mSpeakerIcon.setImageResource(icon);
            mSpeakerIcon.setRotation(angle);
        }
    }


    protected boolean mDisallowIntercept;

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mDisallowIntercept = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        if (mDisallowIntercept) {
            return false; // Do not intercept touch event, let the child handle it
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Here we actually handle the touch event (e.g. if the action is ACTION_MOVE,
        // scroll this container).
        // This method will only be called if the touch event was intercepted in
        // onInterceptTouchEvent
        return super.onTouchEvent(ev);
    }


    public void requestDisallowInterceptTouchEv(boolean disallowIntercept) {    // AdvancedSettingIf
        this.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (mListView != null) {
            mListView.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isBusy()) {
            super.onLayout(changed, left, top, right, bottom);
        }
    }


}
