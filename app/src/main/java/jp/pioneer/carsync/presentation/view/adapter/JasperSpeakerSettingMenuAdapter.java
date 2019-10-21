package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.widget.JasperSpeakerSettingMenuItemView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerSettingMenuItemView;

/**
 * Created by NSW00_906320 on 2017/07/28.
 */

public class JasperSpeakerSettingMenuAdapter extends SpeakerSettingMenuAdapter {
    private boolean busy = false;

    public boolean isBusy() {            // AdvancedSettingIf
        return busy;
    }

    public void setBusy(boolean busy) {    // AdvancedSettingIf
        this.busy = busy;
    }

    public JasperSpeakerSettingMenuAdapter(Context context, String name, ColorStateList buttonBackgroundTint, ColorStateList currentValueTextColor) {
        super(context, name, buttonBackgroundTint, currentValueTextColor);
        mButtonBackgroundTint = buttonBackgroundTint;
        mCurrentValueTextColor = currentValueTextColor;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SpeakerSettingMenuItem item = getItem(position);

        if (convertView == null) {
            convertView = new JasperSpeakerSettingMenuItemView(getContext(), item.type, item.type2);
            convertView.setTag(item.type);
        }

        JasperSpeakerSettingMenuItemView v = (JasperSpeakerSettingMenuItemView) convertView;

        v.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); // 子要素のクリックイベントを拾うため
        v.setEnabled(item.enabled);
        v.setEnabled2(item.enabled2);
        v.setDecreaseEnabled(item.decreaseEnabled);
        v.setIncreaseEnabled(item.increaseEnabled);
        v.setDecreaseEnabled2(item.decreaseEnabled2);
        v.setIncreaseEnabled2(item.increaseEnabled2);
        v.setButtonBackgroundTint(mButtonBackgroundTint);
        int title = 0;
        String strTitle = null;
        switch (item.type) {
            case SUBWOOFER:
                if (item.type2 == SpeakerSettingMenuType.EMPTY) {
                    title = R.string.set_206;
                } else {
                    strTitle = getContext().getString(R.string.set_206) + "/" + getContext().getString(R.string.set_211);
                }
                break;
            case SUBWOOFER_PHASE:
                title = R.string.set_211;
                break;
            case SPEAKER_LEVEL:
                title = R.string.set_200;
                break;
            case HPF:
                title = R.string.set_090;
                break;
            case LPF:
                title = R.string.set_128;
                break;
            case HPF_CUTOFF:
            case LPF_CUTOFF:
                if (item.type2 == SpeakerSettingMenuType.EMPTY) {
                    title = R.string.set_052;
                } else {
                    strTitle = getContext().getString(R.string.set_052) + "/" + getContext().getString(R.string.set_195);
                }
                break;
            case HPF_SLOPE:
            case LPF_SLOPE:
                title = R.string.set_195;
                break;
            case HPF_CUTOFF_SLOPE:
            case LPF_CUTOFF_SLOPE:
                strTitle = getContext().getString(R.string.set_052) + "/" + getContext().getString(R.string.set_195);
                break;
            case TIME_ALIGNMENT:
                title = R.string.set_229;
                break;
            default:
                break;
        }

        if (strTitle != null) {
            v.setTitle(strTitle);
        } else {
            v.setTitle(getContext().getString(title));
        }
        v.setCurrentValueText(item.currentValue);
        v.setCurrentValueText2(item.currentValue2);

        if (item.type2 == SpeakerSettingMenuType.EMPTY) {
            v.setCurrentValueText2(item.currentValue);
        }
        v.setCurrentValueTextColor(mCurrentValueTextColor);

        v.setOnSpeakerSettingChangingListener((SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener) item.listener);
        if (item.type2 != SpeakerSettingMenuType.EMPTY) {
            v.setOnSpeakerSettingChangingListener2((SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener) item.listener2);

        }
        return convertView;
    }

    public SpeakerSettingMenuItem findItem2(SpeakerSettingMenuType type) {
        int count = getCount();
        for (int pos = 0; pos < count; pos++) {
            SpeakerSettingMenuItem item = getItem(pos);
            if (item.type2 == type) return item;
        }
        return null;
    }

}
