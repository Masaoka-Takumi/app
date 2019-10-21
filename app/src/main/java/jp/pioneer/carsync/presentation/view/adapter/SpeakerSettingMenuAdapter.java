package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.widget.GridSpeakerSettingMenuItemView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerSettingMenuItemView;

/**
 * スピーカー設定画面のAdapter
 * Created by tsuyosh on 2016/02/25.
 */
public class SpeakerSettingMenuAdapter extends ArrayAdapter<SpeakerSettingMenuAdapter.SpeakerSettingMenuItem> {
    protected final String myName;
    protected ColorStateList mButtonBackgroundTint;
    protected ColorStateList mCurrentValueTextColor;

    protected boolean _showing;

    public boolean isShowing() {
        return _showing;
    }

    public void setShowing(boolean showing) {
        this._showing = showing;
    }

    public SpeakerSettingMenuAdapter(Context context, String name, ColorStateList buttonBackgroundTint, ColorStateList currentValueTextColor) {
        super(context, 0);
        myName = name;
        mButtonBackgroundTint = buttonBackgroundTint;
        mCurrentValueTextColor = currentValueTextColor;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SpeakerSettingMenuItem item = getItem(position);

        if (convertView == null) {
            convertView = new SpeakerSettingMenuItemView(getContext(), item.type);
            convertView.setTag(item.type);
        }else{
            if(convertView.getTag()!=item.type){
                convertView = new GridSpeakerSettingMenuItemView(getContext(), item.type, item.type2);
                convertView.setTag(item.type);
            }
        }

        SpeakerSettingMenuItemView v = (SpeakerSettingMenuItemView) convertView;
        v.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); // 子要素のクリックイベントを拾うため
        v.setEnabled(item.enabled);
        v.setDecreaseEnabled(item.decreaseEnabled);
        v.setIncreaseEnabled(item.increaseEnabled);
        v.setButtonBackgroundTint(mButtonBackgroundTint);
        int title = 0;
        String strTitle = null;
        switch (item.type) {
            case SUBWOOFER:
                title = R.string.set_206;
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
                title = R.string.set_052;
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

        v.setCurrentValueTextColor(mCurrentValueTextColor);

        v.setOnSpeakerSettingChangingListener(item.listener);
/*        v.setOnSpeakerSettingChangingListener(new SpeakerSettingMenuItemView.OnSpeakerSettingChangingListener(){
            @Override
            public void onIncreaseClicked() {
                onClickButton(item.type,true);
            }

            @Override
            public void onDecreaseClicked() {
                onClickButton(item.type,false);
            }
        });*/

        return convertView;
    }

    /**
     * Button押下処理
     */
    protected void onClickButton(SpeakerSettingMenuType type, boolean increase) {
    }

    public SpeakerSettingMenuItem findItem(SpeakerSettingMenuType type) {
        int count = getCount();
        for (int pos = 0; pos < count; pos++) {
            SpeakerSettingMenuItem item = getItem(pos);
            if (item.type == type) return item;
        }
        return null;
    }

    public void setButtonBackgroundTint(ColorStateList tint) {
        mButtonBackgroundTint = tint;
        notifyDataSetChanged();
    }

    public void setCurrentValueTextColor(ColorStateList color) {
        mCurrentValueTextColor = color;
        notifyDataSetChanged();
    }

    public static class SpeakerSettingMenuItem {
        public boolean enabled, enabled2;

        public boolean decreaseEnabled, decreaseEnabled2;

        public boolean increaseEnabled, increaseEnabled2;

        public final SpeakerSettingMenuType type;
        public SpeakerSettingMenuType type2;

        public CharSequence currentValue, currentValue2;

        public SpeakerSettingMenuItemView.OnSpeakerSettingChangingListener listener, listener2;

        public SpeakerSettingMenuItem(SpeakerSettingMenuType type) {
            this.type = type;
            type2 = SpeakerSettingMenuType.EMPTY;
            decreaseEnabled2 = false;
            increaseEnabled2 = false;
            currentValue2 = "";
            listener2 = null;
        }
    }

    public enum SpeakerSettingMenuType {
        EMPTY(0),                // add by nakano on 2016/06/17
        SPEAKER_LEVEL(10),
        SUBWOOFER(20),
        SUBWOOFER_PHASE(21),
        HPF(30),
        HPF_CUTOFF(32),
        HPF_SLOPE(35),
        HPF_CUTOFF_SLOPE(37),
        LPF(40),
        LPF_CUTOFF(42),
        LPF_SLOPE(45),
        LPF_CUTOFF_SLOPE(47),
        TIME_ALIGNMENT(50),;

        private final int id;

        SpeakerSettingMenuType(final int id) {
            this.id = id;
        }

        public int getInt() {
            return this.id;
        }
    }

    public View findViewByType(ListView parent, SpeakerSettingMenuType type) {
//        int len = parent.getCount();
        View view;
        view = parent.findViewWithTag(type);
        return view;
    }

}
