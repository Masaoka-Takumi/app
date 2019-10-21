package jp.pioneer.carsync.presentation.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter;
import timber.log.Timber;

/**
 * Created by NSW00_906320 on 2017/07/28.
 */

public class JasperSpeakerSettingMenuItemView extends FrameLayout {
    @BindView(R.id.myOutline)
    MyLinearLayout myOutline;

    @BindView(android.R.id.title)
    TextView mTitleText;

    @BindView(R.id.decreaseButton)
    AutoRepeatButton mDecreaseButton;

    @BindView(R.id.currentValueText)
    TextView mCurrentValueText;

    @BindView(R.id.increaseButton)
    AutoRepeatButton mIncreaseButton;

    @BindView(R.id.decreaseButton2)
    AutoRepeatButton mDecreaseButton2;

    @BindView(R.id.currentValueText2)
    TextView mCurrentValueText2;

    @BindView(R.id.increaseButton2)
    AutoRepeatButton mIncreaseButton2;

    private boolean mDecreaseEnabled = true;
    private boolean mIncreaseEnabled = true;

    private boolean mDecreaseEnabled2 = false;
    private boolean mIncreaseEnabled2 = false;

    private SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener mListener;
    private SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener mListener2;

    private SpeakerSettingMenuAdapter.SpeakerSettingMenuType mSpeakerSettingMenuType = SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY;
    private SpeakerSettingMenuAdapter.SpeakerSettingMenuType mSpeakerSettingMenuType2 = SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY;

    protected boolean mDisallowIntercept;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JasperSpeakerSettingMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes,
                                            SpeakerSettingMenuAdapter.SpeakerSettingMenuType type, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type2) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mSpeakerSettingMenuType = type;
        mSpeakerSettingMenuType2 = type2;
        init(context, attrs, defStyleAttr);
    }

    public JasperSpeakerSettingMenuItemView(Context context, AttributeSet attrs, int defStyleAttr,
                                            SpeakerSettingMenuAdapter.SpeakerSettingMenuType type, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type2) {
        super(context, attrs, defStyleAttr);
        mSpeakerSettingMenuType = type;
        mSpeakerSettingMenuType2 = type2;
        init(context, attrs, defStyleAttr);
    }

    public JasperSpeakerSettingMenuItemView(Context context, AttributeSet attrs,
                                            SpeakerSettingMenuAdapter.SpeakerSettingMenuType type, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type2) {
        this(context, attrs, 0, type, type2);
    }

    public JasperSpeakerSettingMenuItemView(Context context,
                                            SpeakerSettingMenuAdapter.SpeakerSettingMenuType type, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type2) {
        this(context, null, type, type2);
    }

    public JasperSpeakerSettingMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, null, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY);
    }

    public JasperSpeakerSettingMenuItemView(@NonNull Context context) {
        this(context, null, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        boolean isLow17 = true; // Jasper Only

        int resource;
        if (isLow17) {
            if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_single;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_CUTOFF) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_SLOPE) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_CUTOFF_SLOPE) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_CUTOFF) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_SLOPE) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_CUTOFF_SLOPE) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.SUBWOOFER_PHASE) {
                resource = R.layout.jasper_widget_speaker_setting_menu_item_indent;
            } else {
                resource = R.layout.jasper_widget_speaker_setting_menu_item;
            }
        } else {
            resource = R.layout.jasper_widget_speaker_setting_menu_item;
        }

        View root = inflate(context, resource, this);
        ButterKnife.bind(this, root);

        mDecreaseButton.setMyNotifyListener(myNotifyListener);
        mIncreaseButton.setMyNotifyListener(myNotifyListener);
        mDecreaseButton2.setMyNotifyListener(myNotifyListener);
        mIncreaseButton2.setMyNotifyListener(myNotifyListener);

        setSpeakerSettingMenuType2(mSpeakerSettingMenuType2);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mDisallowIntercept = disallowIntercept;

        if (myOutline != null) {
            myOutline.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

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

    public void setTitle(CharSequence title) {
        mTitleText.setText(title);
    }

    public void setCurrentValueText(CharSequence valueText) {
//        if (com.pioneer.alternativeremote.protocol.BuildConfig.ENABLE_TIMBER) Timber.d("AutoRepeatButton setCurrentValueText %s", valueText);
        mCurrentValueText.setText(valueText);
    }

    public void setCurrentValueText2(CharSequence valueText) {
        mCurrentValueText2.setText(valueText);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTitleText.setEnabled(enabled);
        mCurrentValueText.setEnabled(enabled);
        applyDecreaseEnabled();
        applyIncreaseEnabled();
    }

    public void setEnabled2(boolean enabled) {
        mCurrentValueText2.setEnabled(enabled);
        applyDecreaseEnabled2();
        applyIncreaseEnabled2();
    }

    public void setDecreaseEnabled(boolean enabled) {
        if (mDecreaseEnabled == enabled) return;
        mDecreaseEnabled = enabled;
        applyDecreaseEnabled();
    }

    public void setIncreaseEnabled(boolean enabled) {
        if (mIncreaseEnabled == enabled) return;
        mIncreaseEnabled = enabled;
        applyIncreaseEnabled();
    }

    public void setDecreaseEnabled2(boolean enabled) {
        if (mDecreaseEnabled2 == enabled) return;
        mDecreaseEnabled2 = enabled;
        applyDecreaseEnabled2();
    }

    public void setIncreaseEnabled2(boolean enabled) {
        if (mIncreaseEnabled2 == enabled) return;
        mIncreaseEnabled2 = enabled;
        applyIncreaseEnabled2();
    }

    public void setButtonBackgroundTint(ColorStateList tint) {
        if (mDecreaseButton != null) {
            ViewCompat.setBackgroundTintList(mDecreaseButton, tint);
        }
        if (mIncreaseButton != null) {
            ViewCompat.setBackgroundTintList(mIncreaseButton, tint);
        }
        if (mDecreaseButton2 != null) {
            ViewCompat.setBackgroundTintList(mDecreaseButton2, tint);
        }
        if (mIncreaseButton2 != null) {
            ViewCompat.setBackgroundTintList(mIncreaseButton2, tint);
        }
    }

    public void setTitleTextColor(ColorStateList colors) {
        mTitleText.setTextColor(colors);
    }

    public void setCurrentValueTextColor(ColorStateList colors) {
        mCurrentValueText.setTextColor(colors);
        mCurrentValueText2.setTextColor(colors);
    }

    private void applyDecreaseEnabled() {
        mDecreaseButton.setEnabled(isEnabled() && mDecreaseEnabled);
    }

    private void applyIncreaseEnabled() {
        mIncreaseButton.setEnabled(isEnabled() && mIncreaseEnabled);
    }

    private void applyDecreaseEnabled2() {
        mDecreaseButton2.setEnabled(isEnabled() && mDecreaseEnabled2);
    }

    private void applyIncreaseEnabled2() {
        mIncreaseButton2.setEnabled(isEnabled() && mIncreaseEnabled2);
    }

    public void setOnSpeakerSettingChangingListener(SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener listener) {
        mListener = listener;
    }

    public void setOnSpeakerSettingChangingListener2(SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener listener) {
        mListener2 = listener;
    }

    private SpeakerSettingMenuAdapter.SpeakerSettingMenuType getSpeakerSettingMenuType2() {
        return mSpeakerSettingMenuType2;
    }

    private void setSpeakerSettingMenuType2(SpeakerSettingMenuAdapter.SpeakerSettingMenuType speakerSettingMenuType2) {
        this.mSpeakerSettingMenuType2 = speakerSettingMenuType2;

        int v = View.GONE;
        if (mSpeakerSettingMenuType2 != SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY) {
            v = View.VISIBLE;
        }

        mDecreaseButton2.setVisibility(v);
        mCurrentValueText2.setVisibility(v);
        mIncreaseButton2.setVisibility(v);
    }

    AutoRepeatButton.MyNotifyListener myNotifyListener = new AutoRepeatButton.MyNotifyListener() {
        @Override
        public void onTouchStateChanged(AutoRepeatButton v, int newState, int oldState) {
            SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener listener = null;
            boolean inc = false;
            switch (v.getId()) {
                case R.id.decreaseButton:
                    inc = false;
                    listener = mListener;
                    break;
                case R.id.increaseButton:
                    inc = true;
                    listener = mListener;
                    break;
                case R.id.decreaseButton2:
                    inc = false;
                    listener = mListener2;
                    break;
                case R.id.increaseButton2:
                    inc = true;
                    listener = mListener2;
                    break;
                default:
                    break;
            }

            if (listener != null) {
                if (newState == AutoRepeatButton.STATE_OPENING) {
                    requestDisallowInterceptTouchEvent(true);
                    listener.onAction(AutoRepeatButton.ACTION_ID_OPEN);
                } else if (newState == AutoRepeatButton.STATE_UP_OUT) {     // 誤解除を防ぐため
                    requestDisallowInterceptTouchEvent(false);
                    if (inc) {
                        listener.onIncreaseSubmitClicked();
                    } else {
                        listener.onDecreaseSubmitClicked();
                    }
                    // listener.onAction(AutoRepeatButton.ACTION_ID_CLOSE);
                    Timber.d("#onTouchStateChanged: AutoRepeatButton.STATE_UP_OUT");
                } else if (newState == AutoRepeatButton.STATE_CANCEL) {
                    requestDisallowInterceptTouchEvent(false);
                    if (inc) {                                               // 何故か STATE_UP_OUT が少なく、STATE_CANCELだけ来る
                        listener.onIncreaseSubmitClicked();
                    } else {
                        listener.onDecreaseSubmitClicked();
                    }
                    // listener.onAction(AutoRepeatButton.ACTION_ID_CLOSE);
                } else if (newState == AutoRepeatButton.STATE_IDLE) {
                    requestDisallowInterceptTouchEvent(false);
                    listener.onAction(AutoRepeatButton.ACTION_ID_CLOSE);
                }
            }

        }
    };

    /**
     * 手を離した時点に、コマンドを車載機に送る
     *
     * @param v
     */
    @OnClick({R.id.decreaseButton, R.id.increaseButton, R.id.decreaseButton2, R.id.increaseButton2})
    void onButtonClick(View v) {
        this.requestDisallowInterceptTouchEvent(false);
        SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener listener = null;
        boolean inc = false;
        switch (v.getId()) {
            case R.id.decreaseButton:
                inc = false;
                listener = mListener;
                break;
            case R.id.increaseButton:
                inc = true;
                listener = mListener;
                break;
            case R.id.decreaseButton2:
                inc = false;
                listener = mListener2;
                break;
            case R.id.increaseButton2:
                inc = true;
                listener = mListener2;
                break;
            default:
                break;
        }

        if (listener != null) {
            if (inc) {
                listener.onIncreaseSubmitClicked();
            } else {
                listener.onDecreaseSubmitClicked();
            }
        }
    }

    /**
     * 長押しして、200ms毎に1ステップUP/DOWNします（ローカルEchoのみ、車載機へコマンド送信はしない）
     *
     * @param v
     * @return
     */
    @OnLongClick({R.id.decreaseButton, R.id.increaseButton, R.id.decreaseButton2, R.id.increaseButton2})
    boolean onButtonLongClick(View v) {
        SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener listener = null;
        boolean inc = false;
        switch (v.getId()) {
            case R.id.decreaseButton:
                inc = false;
                listener = mListener;
                break;
            case R.id.increaseButton:
                inc = true;
                listener = mListener;
                break;
            case R.id.decreaseButton2:
                inc = false;
                listener = mListener2;
                break;
            case R.id.increaseButton2:
                inc = true;
                listener = mListener2;
                break;
            default:
                break;
        }

        if (listener != null) {
            if (inc) {
                listener.onIncreaseLocalClicked();
            } else {
                listener.onDecreaseLocalClicked();
            }
        }

        return true;
    }
}
