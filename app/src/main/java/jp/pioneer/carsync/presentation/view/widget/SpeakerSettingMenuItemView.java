package jp.pioneer.carsync.presentation.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import jp.pioneer.carsync.R;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter;

/**
 * Created by tsuyosh on 2016/02/25.
 */
public class SpeakerSettingMenuItemView extends FrameLayout {
    @BindView(android.R.id.title)
    TextView mTitleText;

    @BindView(R.id.decreaseButton)
    Button mDecreaseButton;

    @BindView(R.id.currentValueText)
    TextView mCurrentValueText;

    @BindView(R.id.increaseButton)
    Button mIncreaseButton;

    private boolean mDecreaseEnabled = true;
    private boolean mIncreaseEnabled = true;
    private OnSpeakerSettingChangingListener mListener;

    private SpeakerSettingMenuAdapter.SpeakerSettingMenuType mSpeakerSettingMenuType = SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpeakerSettingMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mSpeakerSettingMenuType = type;
        init(context, attrs, defStyleAttr);
    }

    public SpeakerSettingMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type) {
        super(context, attrs, defStyleAttr);
        mSpeakerSettingMenuType = type;
        init(context, attrs, defStyleAttr);
    }

    public SpeakerSettingMenuItemView(Context context, AttributeSet attrs, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type) {
        this(context, attrs, 0, type);
    }

    public SpeakerSettingMenuItemView(Context context, SpeakerSettingMenuAdapter.SpeakerSettingMenuType type) {
        this(context, null, type);
    }

    public SpeakerSettingMenuItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, null, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY);
    }

    public SpeakerSettingMenuItemView(@NonNull Context context) {
        this(context, null, SpeakerSettingMenuAdapter.SpeakerSettingMenuType.EMPTY);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        boolean isLow17 = false;

        int resource;
        if (isLow17) {
            if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_CUTOFF) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_SLOPE) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.HPF_CUTOFF_SLOPE) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_CUTOFF) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_SLOPE) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.LPF_CUTOFF_SLOPE) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else if (mSpeakerSettingMenuType == SpeakerSettingMenuAdapter.SpeakerSettingMenuType.SUBWOOFER_PHASE) {
                resource = R.layout.widget_speaker_setting_menu_item_indent;
            } else {
                resource = R.layout.widget_speaker_setting_menu_item;
            }
        } else {
            resource = R.layout.widget_speaker_setting_menu_item;
        }

        View root = inflate(context, resource, this);
        ButterKnife.bind(this, root);
    }

    public void setTitle(CharSequence title) {
        mTitleText.setText(title);
    }

    public void setCurrentValueText(CharSequence valueText) {
        mCurrentValueText.setText(valueText);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTitleText.setEnabled(enabled);
        mCurrentValueText.setEnabled(enabled);
        applyDecreaseEnabled();
        applyIncreaseEnabled();
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

    public void setButtonBackgroundTint(ColorStateList tint) {
        if (mDecreaseButton != null) {
            ViewCompat.setBackgroundTintList(mDecreaseButton, tint);
        }
        if (mIncreaseButton != null) {
            ViewCompat.setBackgroundTintList(mIncreaseButton, tint);
        }
    }

    public void setTitleTextColor(ColorStateList colors) {
        mTitleText.setTextColor(colors);
    }

    public void setCurrentValueTextColor(ColorStateList colors) {
        mCurrentValueText.setTextColor(colors);
    }

    private void applyDecreaseEnabled() {
        mDecreaseButton.setEnabled(isEnabled() && mDecreaseEnabled);
    }

    private void applyIncreaseEnabled() {
        mIncreaseButton.setEnabled(isEnabled() && mIncreaseEnabled);
    }

    public void setOnSpeakerSettingChangingListener(OnSpeakerSettingChangingListener listener) {
        mListener = listener;
    }

    @OnClick({R.id.decreaseButton, R.id.increaseButton})
    void onButtonClick(View v) {
        if (mListener == null) return;
        switch (v.getId()) {
            case R.id.decreaseButton:
                mListener.onDecreaseClicked();
                break;
            case R.id.increaseButton:
                mListener.onIncreaseClicked();
                break;
            default:
                break;
        }
    }

    public interface OnSpeakerSettingChangingListener {
        void onIncreaseClicked();

        void onDecreaseClicked();
    }

    public interface OnSpeakerSettingCommitListener extends OnSpeakerSettingChangingListener {    // 長押し対応
        void onAction(int typeId);        // AutoRepeatButton.ACTION_ID_OPEN |

        void onIncreaseLocalClicked();    // 長押し:継続中

        void onDecreaseLocalClicked();    // 長押し:継続中

        void onIncreaseSubmitClicked();    // 長押し:手を離した

        void onDecreaseSubmitClicked();    // 長押し:手を離した
    }
}
