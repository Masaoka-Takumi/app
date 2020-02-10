package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import me.grantland.widget.AutofitTextView;
import timber.log.Timber;

public class VoiceRecognizeTypeSelectAdapter extends ArrayAdapter<VoiceRecognizeType> {

    @Inject
    AppSharedPreference mPreference;
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<VoiceRecognizeType> mVoiceRecognizeTypeItemArrayList;
    private int mSelectedPosition = -1; // ユーザが選択しているカスタムキー

    public VoiceRecognizeTypeSelectAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedPosition = selectedIndex;
        notifyDataSetChanged(); // Adapterの内容を即時反映
    }

    @Nonnull
    @Override
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        Timber.i("getView position=" + position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.element_item_select_singlechoice, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        VoiceRecognizeType type = getItem(position);
        if (type != null) {
            holder.VoiceRecognizeTypeName.setText(type.label); // 表示する文字列設定
        }

        // ラジオボタンのチェック状態の設定
        if (position == mSelectedPosition) {
            holder.radioButton.setChecked(true);
        } else {
            holder.radioButton.setChecked(false);
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.imageIcon)
        ImageView imageIcon;
        @BindView(android.R.id.text1)
        TextView VoiceRecognizeTypeName;
        @BindView(R.id.autofit_text1)
        AutofitTextView autofitVoiceRecognizeTypeName;
        @BindView(R.id.radioButton1)
        RadioButton radioButton;
        @BindView(R.id.separator_bottom)
        View separator_bottom;

        ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
