package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.SettingEntrance;

/**
 * Created by NSW00_906320 on 2017/06/16.
 */

public class SettingsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Integer> mIconArray = new ArrayList<>();
    private ArrayList<SettingEntrance> mTitleArray = new ArrayList<>();
    private ArrayList<Boolean> mEnabledArray = new ArrayList<>();

    public SettingsAdapter(Context context,
                           ArrayList<Integer> iconArray,
                           ArrayList<SettingEntrance> titleArray,
                           ArrayList<Boolean> enabledArray) {
        mContext = context;
        mIconArray = iconArray;
        mTitleArray = titleArray;
        mEnabledArray = enabledArray;
    }

    public int getCount() {
        return mTitleArray.size();
    }

    public Object getItem(int position) {
        return mIconArray.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.element_list_item_settings, parent, false);
            holder = new SettingsAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SettingsAdapter.ViewHolder) convertView.getTag();
        }
        holder.settingTitle.setText(mTitleArray.get(position).getResource());
        Glide.with(mContext)
                .load(mIconArray.get(position))
                .error(R.drawable.ic_launcher)
                .into(holder.settingIcon);

        boolean isEnabled = Objects.equals(mEnabledArray.get(position), Boolean.TRUE);
        holder.settingTitle.setEnabled(isEnabled);
        holder.settingIcon.setEnabled(isEnabled);
        holder.settingIcon.setColorFilter(ContextCompat.getColor(mContext, isEnabled ? R.color.drawable_white_color : R.color.text_color_disable));
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.setting_view) ImageView settingIcon;
        @BindView(R.id.setting_title) TextView settingTitle;
        long id;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
