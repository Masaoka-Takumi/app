package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import me.grantland.widget.AutofitTextView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Navigationアダプター
 */
public class NavigationAdapter extends BaseExpandableListAdapter {
    public static final int APPLICATION_LIST = 0;
    public static final int WEATHER_APPLICATION_LIST = 0;
    public static final int BOATING_APPLICATION_LIST = 1;
    public static final int FISHING_APPLICATION_LIST = 2;
    public static final int MARIN_NAVI_APPLICATION_LIST = 3;
    public static int mixingSettingIndex = 1;
    public static int mixingVolumeIndex = 2;
    private Context mContext;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private List<ApplicationInfo> mApps = new ArrayList<>();
    private List<ApplicationInfo> mWeatherApps = new ArrayList<>();
    private List<ApplicationInfo> mBoatingApps = new ArrayList<>();
    private List<ApplicationInfo> mFishingApps = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int mOrientation;
    @Nullable
    private ApplicationInfo mSelectedApp;
    private boolean mIsMixingSettingEnabled;
    private boolean mMixingSetting;
    private boolean mIsMixingVolumeEnabled;
    @Nullable
    private NaviGuideVoiceVolumeSetting mMixingVolume;
    private boolean mIsMarin = false;

    /**
     * コンストラクタ.
     */
    public NavigationAdapter(@NonNull Context context, @NonNull ArrayList<String> objects, boolean isMarin) {
        mContext = checkNotNull(context);
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = checkNotNull(objects);
        mIsMarin = isMarin;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
        if (mIsMarin) {
            mixingSettingIndex = MARIN_NAVI_APPLICATION_LIST + 1;
            mixingVolumeIndex = MARIN_NAVI_APPLICATION_LIST + 2;
        } else {
            mixingSettingIndex = 1;
            mixingVolumeIndex = 2;
        }
    }

    /**
     * アプリケーション情報設定.
     *
     * @param apps        インストール済アプリケーション一覧
     * @param selectedApp 選択されているアプリケーション
     */
    public void setApps(@NonNull List<ApplicationInfo> apps, @Nullable ApplicationInfo selectedApp) {
        checkNotNull(apps);

        mApps.clear();
        mApps = apps;
        mSelectedApp = selectedApp;
        notifyDataSetChanged();
    }

    /**
     * アプリケーション情報設定.(Marin用)
     *
     * @param weatherApps インストール済アプリケーション一覧
     * @param selectedApp 選択されているアプリケーション
     */
    public void setMarinApps(@NonNull List<ApplicationInfo> weatherApps, @NonNull List<ApplicationInfo> boatingApps, @NonNull List<ApplicationInfo> fishingApps, @NonNull List<ApplicationInfo> naviApps, @Nullable ApplicationInfo selectedApp) {
        checkNotNull(weatherApps);
        checkNotNull(boatingApps);
        checkNotNull(fishingApps);
        checkNotNull(naviApps);
        mWeatherApps.clear();
        mWeatherApps = weatherApps;
        mBoatingApps.clear();
        mBoatingApps = boatingApps;
        mFishingApps.clear();
        mFishingApps = fishingApps;
        mSelectedApp = selectedApp;
        mApps.clear();
        mApps = naviApps;
        notifyDataSetChanged();
    }

    /**
     * ナビガイド音声設定.
     *
     * @param isEnabled 設定が可能か否か
     * @param setting   設定内容
     */
    public void setMixingSetting(boolean isEnabled, boolean setting) {
        mIsMixingSettingEnabled = isEnabled;
        mMixingSetting = setting;
        notifyDataSetChanged();
    }

    /**
     * ナビガイド音声設定取得.
     *
     * @return ナビガイド音声設定
     */
    public boolean getMixingSetting() {
        return mMixingSetting;
    }

    /**
     * ナビガイド音声ボリューム設定.
     *
     * @param isEnabled 設定可能か否か
     * @param setting   設定内容
     */
    public void setMixingVolume(boolean isEnabled, @Nullable NaviGuideVoiceVolumeSetting setting) {
        mIsMixingVolumeEnabled = isEnabled;
        mMixingVolume = setting;
        notifyDataSetChanged();
    }

    /**
     * ナビガイド音声ボリューム設定取得.
     *
     * @return ナビガイド音声ボリューム設定
     */
    @Nullable
    public NaviGuideVoiceVolumeSetting getMixingVolume() {
        return mMixingVolume;
    }

    /**
     * ナビガイド音声設定可能か取得.
     *
     * @return ナビガイド音声設定可能か否か
     */
    public boolean getMixingVolumeEnabled() {
        return mIsMixingVolumeEnabled;
    }

    // MARK - protected method

    /**
     * スイッチ押下処理
     *
     * @param setting ナビガイド音声設定
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
        if (mIsMarin) {
            if (groupPosition == WEATHER_APPLICATION_LIST) {
                if (mWeatherApps.size() > 0) {
                    return mWeatherApps.size();
                } else {
                    return 1;
                }
            } else if (groupPosition == BOATING_APPLICATION_LIST) {
                if (mBoatingApps.size() > 0) {
                    return mBoatingApps.size();
                } else {
                    return 1;
                }
            } else if (groupPosition == FISHING_APPLICATION_LIST) {
                if (mFishingApps.size() > 0) {
                    return mFishingApps.size();
                } else {
                    return 1;
                }
            } else if (groupPosition == MARIN_NAVI_APPLICATION_LIST) {
                if (mApps.size() > 0) {
                    return mApps.size();
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            if (groupPosition == APPLICATION_LIST) {
                if (mApps.size() > 0) {
                    return mApps.size();
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mTypeArray.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (mIsMarin) {
            if (groupPosition == WEATHER_APPLICATION_LIST) {
                if (mWeatherApps.size() > 0) {
                    return mWeatherApps.get(childPosition);
                }
            } else if (groupPosition == BOATING_APPLICATION_LIST) {
                if (mBoatingApps.size() > 0) {
                    return mBoatingApps.get(childPosition);
                }
            } else if (groupPosition == FISHING_APPLICATION_LIST) {
                if (mFishingApps.size() > 0) {
                    return mFishingApps.get(childPosition);
                }
            } else if (groupPosition == MARIN_NAVI_APPLICATION_LIST) {
                if (mApps.size() > 0) {
                    return mApps.get(childPosition);
                }
            }
        } else {
            if (groupPosition == APPLICATION_LIST) {
                if (mApps.size() > 0) {
                    return mApps.get(childPosition);
                }
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
        if (groupPosition == mixingSettingIndex) {
            MixingViewHolder switchHolder;
            if (convertView == null || !(convertView.getTag() instanceof MixingViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_switch, parent, false);
                switchHolder = new MixingViewHolder(convertView);
                convertView.setTag(switchHolder);
            } else {
                switchHolder = (MixingViewHolder) convertView.getTag();
            }
            switchHolder.mTitle.setText(mTypeArray.get(groupPosition));
            switchHolder.mTitle.setEnabled(mIsMixingSettingEnabled);
            switchHolder.mSwitch.setEnabled(mIsMixingSettingEnabled);
            switchHolder.mSwitch.setChecked(mMixingSetting);
            switchHolder.mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onClickSwitch(isChecked));
            convertView.setEnabled(mIsMixingSettingEnabled);
        } else if (groupPosition == mixingVolumeIndex) {
            VolumeViewHolder summaryHolder;
            if (convertView == null || !(convertView.getTag() instanceof VolumeViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
                summaryHolder = new VolumeViewHolder(convertView);
                convertView.setTag(summaryHolder);
            } else {
                summaryHolder = (VolumeViewHolder) convertView.getTag();
            }

            if (mMixingVolume != null) {
                summaryHolder.mSummary.setText(mMixingVolume.label);
                summaryHolder.mSummary.setVisibility(View.VISIBLE);
            } else {
                summaryHolder.mSummary.setText("");
                summaryHolder.mSummary.setVisibility(View.GONE);
            }
            summaryHolder.mTitle.setText(mTypeArray.get(groupPosition));
            summaryHolder.mTitle.setEnabled(mIsMixingVolumeEnabled);
            summaryHolder.mSummary.setEnabled(mIsMixingVolumeEnabled);
            convertView.setEnabled(mIsMixingVolumeEnabled);
            summaryHolder.separatorBottom.setVisibility(View.VISIBLE);
        } else {
            NavigationAppViewHolder groupHolder;
            if (convertView == null || !(convertView.getTag() instanceof NavigationAppViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
                groupHolder = new NavigationAppViewHolder(convertView);
                groupHolder.mSeparator.setVisibility(View.INVISIBLE);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (NavigationAppViewHolder) convertView.getTag();
            }
            if (groupPosition != 0) {
                groupHolder.mSeparator.setVisibility(View.VISIBLE);
            }
            groupHolder.mTitle.setText(mTypeArray.get(groupPosition));
            groupHolder.mSummary.setText("");
            groupHolder.mSummary.setVisibility(View.GONE);
        }

        return convertView;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        List<ApplicationInfo> apps = new ArrayList<>();
        if (mIsMarin) {
            switch (groupPosition) {
                case WEATHER_APPLICATION_LIST:
                    apps = mWeatherApps;
                    break;
                case BOATING_APPLICATION_LIST:
                    apps = mBoatingApps;
                    break;
                case FISHING_APPLICATION_LIST:
                    apps = mFishingApps;
                    break;
                case MARIN_NAVI_APPLICATION_LIST:
                    apps = mApps;
                    break;
            }
        } else {
            apps = mApps;
        }
        if (apps.size() > 0) {
            AppViewHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof AppViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.element_list_item_navi_app, parent, false);
                holder = new AppViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            ApplicationInfo app = apps.get(childPosition);
            if (app != null) {
                PackageManager pm = mContext.getPackageManager();
                holder.mAppIcon.setImageDrawable(app.loadIcon(pm));
                holder.mAppName.setText(app.loadLabel(pm));

                if (mSelectedApp != null && mSelectedApp.packageName.equals(app.packageName)) {
                    holder.mCheckIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.mCheckIcon.setVisibility(View.INVISIBLE);
                }
            }
            holder.separatorBottom.setVisibility(View.GONE);
            if ((!mIsMarin && getGroupCount() == APPLICATION_LIST + 1 && getChildrenCount(groupPosition) - 1 == childPosition)
                    || (mIsMarin && groupPosition == MARIN_NAVI_APPLICATION_LIST
                    && getGroupCount() == MARIN_NAVI_APPLICATION_LIST + 1
                    && getChildrenCount(groupPosition) - 1 == childPosition)) {
                holder.separatorBottom.setVisibility(View.VISIBLE);
            }
        } else {
            convertView = setNotExistAppCell(groupPosition, childPosition, convertView, parent);
        }
        return convertView;
    }

    private View setNotExistAppCell(int groupPosition, int childPosition, View convertView, ViewGroup parent) {
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
        holder.separatorBottom.setVisibility(View.GONE);
        if ((!mIsMarin && getGroupCount() == APPLICATION_LIST + 1) || (mIsMarin && groupPosition == MARIN_NAVI_APPLICATION_LIST && getGroupCount() == MARIN_NAVI_APPLICATION_LIST + 1)) {
            holder.separatorBottom.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // MARK - view holder
    static class NavigationAppViewHolder {
        @BindView(R.id.titleText)
        AutofitTextView mTitle;
        @BindView(R.id.summaryText)
        TextView mSummary;
        @BindView(R.id.separator)
        View mSeparator;

        public NavigationAppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class MixingViewHolder {
        @BindView(R.id.titleText)
        AutofitTextView mTitle;
        @BindView(R.id.switchWidget)
        SwitchCompat mSwitch;

        public MixingViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class VolumeViewHolder {
        @BindView(R.id.titleText)
        AutofitTextView mTitle;
        @BindView(R.id.summaryText)
        TextView mSummary;
        @BindView(R.id.separator)
        View mSeparator;
        @BindView(R.id.separator_bottom)
        View separatorBottom;

        public VolumeViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class AppViewHolder {
        @BindView(R.id.app_icon)
        ImageView mAppIcon;
        @BindView(R.id.app_name)
        AutofitTextView mAppName;
        @BindView(R.id.check_icon)
        ImageView mCheckIcon;
        @BindView(R.id.separator)
        View mSeparator;
        @BindView(R.id.separator_bottom)
        View separatorBottom;

        AppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    static class NotExistAppViewHolder {
        @BindView(R.id.titleText)
        AutofitTextView mTitle;
        @BindView(R.id.summaryText)
        TextView mSummary;
        @BindView(R.id.separator)
        View mSeparator;
        @BindView(R.id.separator_bottom)
        View separatorBottom;

        public NotExistAppViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
