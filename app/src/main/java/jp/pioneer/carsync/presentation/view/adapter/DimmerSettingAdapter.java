package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.presentation.model.DimmerListType;
import jp.pioneer.carsync.presentation.view.widget.DimmerTimePicker;

/**
 * Created by NSW00_007906 on 2017/09/04.
 */

public class DimmerSettingAdapter extends BaseExpandableListAdapter {
    private ArrayList<DimmerListType> mTypeArray;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private DimmerSetting.Dimmer mSelectedDimmer;
    private int mStartHour = 0;
    private int mStartMinute = 0;
    private int mEndHour = 0;
    private int mEndMinute = 0;
    private int mOrientation;
    private boolean mIsEnabled = true;
    private TimeFormatSetting mTimeFormatSetting;
    /**
     * Constructor
     */
    public DimmerSettingAdapter(@NonNull Context context, @NonNull ArrayList<DimmerListType> objects) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    public void setTimeFormatSetting(TimeFormatSetting setting) {
        mTimeFormatSetting = setting;
        DimmerTimePicker.mTimeFormatSetting = setting;
    }

    public DimmerSetting.Dimmer getSelectedDimmer() {
        return mSelectedDimmer;
    }

    public void setSelectedIndex(DimmerSetting.Dimmer selectedDimmer) {
        mSelectedDimmer = selectedDimmer;
        notifyDataSetChanged();
    }

    public void setDimmerTime(int startHour, int startMinute, int endHour, int endMinute) {
        mStartHour = startHour;
        mStartMinute = startMinute;
        mEndHour = endHour;
        mEndMinute = endMinute;
        notifyDataSetChanged();
    }

    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mTypeArray.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
         return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mTypeArray.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        DimmerListType type = mTypeArray.get(groupPosition);
        if (type == DimmerListType.SYNC_CLOCK_START || type == DimmerListType.SYNC_CLOCK_STOP) {
            DimmerSettingAdapter.GroupViewHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof DimmerSettingAdapter.GroupViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_time_setting, parent, false);
                holder = new DimmerSettingAdapter.GroupViewHolder(convertView);
                holder.mTitle.setText(type.label);
                convertView.setTag(holder);
            } else {
                holder = (DimmerSettingAdapter.GroupViewHolder) convertView.getTag();
            }

            //最終行は下線を表示
            holder.separatorBottom.setVisibility(View.GONE);
            if (groupPosition==getGroupCount()-1&&!isExpanded) {
                holder.separatorBottom.setVisibility(View.VISIBLE);
            }

            String start;
            String end;
            if (mTimeFormatSetting == TimeFormatSetting.TIME_FORMAT_24) {
                // 24時間設定である
                String format = mContext.getResources().getString(R.string.setting_theme_dimmer_time_format);
                start = String.format(Locale.ENGLISH, format, mStartHour, mStartMinute);
                end = String.format(Locale.ENGLISH, format, mEndHour, mEndMinute);
            } else {
                // 12時間設定である
                String[] amPmString = {"AM", "PM"};
                int startAmPmIndex = mStartHour / 12;
                int endAmPmIndex = mEndHour / 12;

                int startHour12h = mStartHour % 12;
                int endHour12h = mEndHour % 12;
                if (!AppUtil.isZero2ElevenIn12Hour(mContext)) {
                    // 日本以外(日本以外では0時のことを12時と表記する慣習である)
                    if (startHour12h == 0) {
                        startHour12h = 12;
                    }
                    if (endHour12h == 0) {
                        endHour12h = 12;
                    }
                }
                String format = mContext.getResources().getString(R.string.setting_theme_dimmer_time_format_12h);
                start = String.format(Locale.ENGLISH, format, startHour12h, mStartMinute, amPmString[startAmPmIndex]);
                end = String.format(Locale.ENGLISH, format, endHour12h, mEndMinute, amPmString[endAmPmIndex]);
            }

            if (type == DimmerListType.SYNC_CLOCK_START) {
                holder.mTime.setText(start);
            } else {
                holder.mTime.setText(end);
            }
           if (mSelectedDimmer==DimmerSetting.Dimmer.SYNC_CLOCK) {
                holder.mTitle.setAlpha(1.0f);
                holder.mTime.setAlpha(1.0f);
            } else {
                holder.mTitle.setAlpha(0.6f);
                holder.mTime.setAlpha(0.6f);
            }
            holder.mTitle.setEnabled(mIsEnabled);
            holder.mTime.setEnabled(mIsEnabled);
        } else {
            DimmerSettingAdapter.ViewHolder vh;
            if (convertView == null || !(convertView.getTag() instanceof DimmerSettingAdapter.ViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_check, parent, false);
                vh = new DimmerSettingAdapter.ViewHolder(convertView);
                vh.mTitle.setText(type.label);

                if(mOrientation == Configuration.ORIENTATION_PORTRAIT){

                    ViewGroup.LayoutParams lp = vh.mTitle.getLayoutParams();
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)lp;
                    mlp.setMargins((int)mContext.getResources().getDimension(R.dimen.dimmer_setting_portrait_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
                    //マージンを設定
                    vh.mTitle.setLayoutParams(mlp);

                }
                convertView.setTag(vh);
            } else {
                vh = (DimmerSettingAdapter.ViewHolder) convertView.getTag();
            }

            if (groupPosition == 0) {
                vh.separator.setVisibility(View.GONE);
            }
            if (type.dimmer == mSelectedDimmer) {
                vh.mCheck.setVisibility(View.VISIBLE);
            } else {
                vh.mCheck.setVisibility(View.INVISIBLE);
            }
            vh.mTitle.setEnabled(mIsEnabled);
        }

        return convertView;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        /*DimmerListType type = mTypeArray.get(groupPosition);
        DimmerSettingAdapter.ChildViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof DimmerSettingAdapter.ChildViewHolder)) {
            convertView = mLayoutInflater.inflate(R.layout.element_time_picker, parent, false);
            holder = new DimmerSettingAdapter.ChildViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DimmerSettingAdapter.ChildViewHolder) convertView.getTag();
        }
        ViewGroup.LayoutParams lp = holder.separatorBottom.getLayoutParams();
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)lp;
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mlp.setMargins((int)mContext.getResources().getDimension(R.dimen.dimmer_setting_list_portrait_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        }else{
            mlp.setMargins((int)mContext.getResources().getDimension(R.dimen.dimmer_setting_list_land_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        }
        holder.separatorBottom.setLayoutParams(mlp);

        //最終行は下線を表示
        holder.separatorBottom.setVisibility(View.GONE);
        if (groupPosition==getGroupCount()-1&&isLastChild) {
            holder.separatorBottom.setVisibility(View.VISIBLE);
        }

        holder.mTimePicker.setTimeFormatSetting(mTimeFormatSetting);

        if (type == DimmerListType.SYNC_CLOCK_START) {
            holder.mTimePicker.set(mStartHour, mStartMinute);
        } else {
            holder.mTimePicker.set(mEndHour, mEndMinute);
        }
        holder.mCancel.setOnClickListener(v -> onClickCancelButton(groupPosition));
        holder.mOk.setOnClickListener(v -> {
            int hour = holder.mTimePicker.getHour();
            int minute = holder.mTimePicker.getMinute();

            onClickOkButton(groupPosition ,type, hour, minute);
        });*/

        return convertView;
    }

    protected void onClickCancelButton(int groupPosition) {
    }

    protected void onClickOkButton(int groupPosition, DimmerListType clickListType, int hour, int minute) {
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class ViewHolder {
        @BindView(R.id.separator) View separator;
        @BindView(R.id.textView) TextView mTitle;
        @BindView(R.id.check) ImageView mCheck;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupViewHolder {
        @BindView(R.id.textView) TextView mTitle;
        @BindView(R.id.time_text) TextView mTime;
        @BindView(R.id.separator_bottom) View separatorBottom;
        public GroupViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

/*    static class ChildViewHolder {
        @BindView(R.id.time_picker) DimmerTimePicker mTimePicker;
        @BindView(R.id.cancel_button) TextView mCancel;
        @BindView(R.id.ok_button) TextView mOk;
        @BindView(R.id.separator_bottom) View separatorBottom;
        public ChildViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }*/
}
