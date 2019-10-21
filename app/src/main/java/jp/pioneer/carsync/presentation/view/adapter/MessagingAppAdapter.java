package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import me.grantland.widget.AutofitTextView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Messageアダプター.
 */
public class MessagingAppAdapter extends BaseExpandableListAdapter {
    public static final int MESSAGE_READING = 0;
    public static final int MESSAGE_APP_LIST = 1;
    public static final int MESSAGE_COLOR = 2;
    private Context mContext;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private List<ApplicationInfo> mApps = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int mOrientation;
    private boolean mMessageReading;
    private List<ApplicationInfo> mSelectedApps = new ArrayList<>();
    private boolean mIsMessageColorEnabled;
    @Nullable private IncomingMessageColorSetting mMessageColor;


    public MessagingAppAdapter(@NonNull Context context, @NonNull ArrayList<String> objects) {
        mContext = checkNotNull(context);
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = checkNotNull(objects);
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    /**
     * アプリケーション情報設定.
     *
     * @param apps         インストール済アプリケーション一覧
     * @param selectedApps 選択されているアプリケーション
     */
    public void setApps(@NonNull List<ApplicationInfo> apps, @NonNull List<ApplicationInfo> selectedApps) {
        checkNotNull(apps);
        checkNotNull(selectedApps);

        mApps.clear();
        mApps = apps;
        mSelectedApps = selectedApps;
        notifyDataSetChanged();
    }

    /**
     * メッセージ読み上げ設定.
     *
     * @param setting 設定内容
     */
    public void setMessageReading(boolean setting) {
        mMessageReading = setting;
        notifyDataSetChanged();
    }

    /**
     * メッセージ読み上げ設定取得.
     *
     * @return メッセージ読み上げ設定
     */
    public boolean getMessageReading() {
        return mMessageReading;
    }

    /**
     * メッセージカラー設定.
     *
     * @param isEnabled 設定可能か否か
     * @param setting   設定内容
     */
    public void setMessageColor(boolean isEnabled, @Nullable IncomingMessageColorSetting setting) {
        mIsMessageColorEnabled = isEnabled;
        mMessageColor = setting;
        notifyDataSetChanged();
    }

    /**
     * メッセージカラー設定 設定可能か否か取得
     *
     * @return メッセージカラー設定 設定可能か否か
     */
    public boolean isMessageColorEnabled(){
        return mIsMessageColorEnabled;
    }

    // MARK - protected method

    /**
     * スイッチ押下処理
     *
     * @param setting メッセージ読み上げ設定
     */
    protected void onClickSwitch(boolean setting) {
    }

    // MARK - BaseExpandableListAdapter

    @Override
    public int getGroupCount() {
        return mTypeArray.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == MESSAGE_APP_LIST) {
            if (mApps.size() > 0) {
                return mApps.size();
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mTypeArray.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == MESSAGE_APP_LIST) {
            if (mApps.size() > 0) {
                return mApps.get(childPosition);
            }
        }
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
        switch (groupPosition) {
            case MESSAGE_READING:
                MessageReadingViewHolder switchHolder;
                if (convertView == null || !(convertView.getTag() instanceof MessageReadingViewHolder)) {
                    convertView = mLayoutInflater.inflate(R.layout.element_list_item_switch, parent, false);
                    switchHolder = new MessageReadingViewHolder(convertView);
                    switchHolder.mTitle.setText(mTypeArray.get(groupPosition));
                    switchHolder.mSeparator.setVisibility(View.INVISIBLE);
                    convertView.setTag(switchHolder);
                } else {
                    switchHolder = (MessageReadingViewHolder) convertView.getTag();
                }

                switchHolder.mSwitch.setChecked(mMessageReading);
                switchHolder.mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onClickSwitch(isChecked));
                break;
            case MESSAGE_APP_LIST:
                MessageReadingAppViewHolder groupHolder;
                if (convertView == null || !(convertView.getTag() instanceof MessageReadingAppViewHolder)) {
                    convertView = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
                    groupHolder = new MessageReadingAppViewHolder(convertView);
                    groupHolder.mTitle.setText(mTypeArray.get(groupPosition));
                    convertView.setTag(groupHolder);
                } else {
                    groupHolder = (MessageReadingAppViewHolder) convertView.getTag();
                }
                groupHolder.mSummary.setText("");
                groupHolder.mSummary.setVisibility(View.GONE);
                groupHolder.mTitle.setEnabled(mMessageReading);
                groupHolder.mSummary.setEnabled(mMessageReading);
                break;
            case MESSAGE_COLOR:
                IncomingMessageColorViewHolder summaryHolder;
                if (convertView == null || !(convertView.getTag() instanceof IncomingMessageColorViewHolder)) {
                    convertView = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
                    summaryHolder = new IncomingMessageColorViewHolder(convertView);
                    summaryHolder.mTitle.setText(mTypeArray.get(groupPosition));
                    convertView.setTag(summaryHolder);
                } else {
                    summaryHolder = (IncomingMessageColorViewHolder) convertView.getTag();
                }

                if (mMessageColor != null) {
                    summaryHolder.mSummary.setText(mMessageColor.label);
                    summaryHolder.mSummary.setVisibility(View.VISIBLE);
                } else {
                    summaryHolder.mSummary.setText("");
                    summaryHolder.mSummary.setVisibility(View.GONE);
                }

                summaryHolder.mTitle.setEnabled(mIsMessageColorEnabled);
                summaryHolder.mSummary.setEnabled(mIsMessageColorEnabled);
                convertView.setEnabled(mIsMessageColorEnabled);
                summaryHolder.separatorBottom.setVisibility(View.VISIBLE);
                break;
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (mApps.size() > 0) {
            AppViewHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof AppViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_message_app, parent, false);
                holder = new AppViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }

            ApplicationInfo app = mApps.get(childPosition);
            if (app != null) {
                PackageManager pm = mContext.getPackageManager();
                holder.mCheckedTextView.setText(app.loadLabel(pm));
                Drawable icon = app.loadIcon(pm);
                float iconSize = mContext.getResources().getDimension(R.dimen.source_app_icon_size);
                icon.setBounds(0, 0, (int) iconSize, (int) iconSize);
                holder.mCheckedTextView.setCompoundDrawables(icon, null, null, null);
                holder.mCheckedTextView.setEnabled(mMessageReading);
                //holder.mCheckedTextView.setChecked(mSelectedApps.contains(app));
                holder.checkBox.setEnabled(mMessageReading);
                holder.checkBox.setChecked(mSelectedApps.contains(app));
            }
            holder.separatorBottom.setVisibility(View.GONE);
            if(getGroupCount()==2&&getChildrenCount(groupPosition)-1 == childPosition){
                holder.separatorBottom.setVisibility(View.VISIBLE);
            }
        } else {
            NotExistAppViewHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof NotExistAppViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
                holder = new NotExistAppViewHolder(convertView);
                holder.mTitle.setText(R.string.set_241);
                convertView.setTag(holder);
            } else {
                holder = (NotExistAppViewHolder) convertView.getTag();
            }

            if (childPosition == 0) {
                ViewGroup.LayoutParams lp = holder.mTitle.getLayoutParams();
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    holder.mTitle.setPadding((int) mContext.getResources().getDimension(R.dimen.application_list_not_install_landscape_left_margin), holder.mTitle.getPaddingTop(), holder.mTitle.getPaddingRight(), holder.mTitle.getPaddingBottom());
                } else {
                    holder.mTitle.setPadding((int) mContext.getResources().getDimension(R.dimen.application_list_not_install_portrait_left_margin), holder.mTitle.getPaddingTop(), holder.mTitle.getPaddingRight(), holder.mTitle.getPaddingBottom());
                }
                holder.mTitle.setLayoutParams(mlp);

                lp = holder.mSeparator.getLayoutParams();
                mlp = (ViewGroup.MarginLayoutParams) lp;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.application_list_landscape_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
                } else {
                    mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.application_list_portrait_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
                }
                holder.mSeparator.setLayoutParams(mlp);
            }

            holder.mTitle.setEnabled(false);
            holder.mSummary.setText("");
            holder.mSummary.setVisibility(View.GONE);
            if(getGroupCount()==1){
                holder.separatorBottom.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // MARK - view holder

    static class MessageReadingViewHolder {
        @BindView(R.id.titleText) AutofitTextView mTitle;
        @BindView(R.id.switchWidget) SwitchCompat mSwitch;
        @BindView(R.id.separator) View mSeparator;

        public MessageReadingViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class MessageReadingAppViewHolder {
        @BindView(R.id.titleText) AutofitTextView mTitle;
        @BindView(R.id.summaryText) TextView mSummary;
        @BindView(R.id.separator) View mSeparator;

        public MessageReadingAppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class IncomingMessageColorViewHolder {
        @BindView(R.id.titleText) AutofitTextView mTitle;
        @BindView(R.id.summaryText) TextView mSummary;
        @BindView(R.id.separator) View mSeparator;
        @BindView(R.id.separator_bottom) View separatorBottom;
        public IncomingMessageColorViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class AppViewHolder {
        @BindView(android.R.id.text1) TextView mCheckedTextView;
        @BindView(R.id.separator) View mSeparator;
        @BindView(R.id.separator_bottom) View separatorBottom;
        @BindView(R.id.checkBox1) CheckBox checkBox;
        AppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class NotExistAppViewHolder {
        @BindView(R.id.titleText) AutofitTextView mTitle;
        @BindView(R.id.summaryText) TextView mSummary;
        @BindView(R.id.separator) View mSeparator;
        @BindView(R.id.separator_bottom) View separatorBottom;
        public NotExistAppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
