package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.InformationPresenter;
import jp.pioneer.carsync.presentation.view.InformationView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * Information設定の画面
 */

public class InformationFragment extends AbstractPreferenceFragment<InformationPresenter, InformationView>
        implements InformationView ,View.OnLongClickListener{

    @Inject InformationPresenter mPresenter;
    private static final int TAP_TIME = 5000; //5秒間に6回連打
    private static final int TAP_COUNT = 6;
    private Preference mDeviceInfo;
    private Preference mDeviceFarmVersion;
    private Preference mLicense;
    private Preference mEula;
    private Preference mPrivacyPolicy;
    private Preference mAppVersion;
    private int mCount = 0;
    private long mStartMillis = 0;
    private boolean mDebugAction = false;
    @Inject
    public InformationFragment() {
    }

    public static InformationFragment newInstance(Bundle args) {
        InformationFragment fragment = new InformationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_information, rootKey);
        mDeviceInfo = findPreference(getString(R.string.key_device_information));
        if(BuildConfig.DEBUG) {
            mDeviceInfo.setOnPreferenceClickListener((preference) -> {

                long time = System.currentTimeMillis();

                //if it is the first time, or if it has been more than 5 seconds since the first tap ( so it is like a new try), we reset everything
                if (mStartMillis == 0 || (time - mStartMillis > TAP_TIME)) {
                    mStartMillis = time;
                    mCount = 1;
                    mDebugAction = false;
                }
                //it is not the first, and it has been  less than 5 seconds since the first
                else { //  time-startMillis< 3000
                    mCount++;
                }

                if (mCount == TAP_COUNT) {
                    RecyclerView lv = getListView();
                    //Timber.d("mAppVersion"+mAppVersion.());
                    RecyclerView.ViewHolder holder = lv.findViewHolderForLayoutPosition(5);
                    if (holder != null) {
                        holder.itemView.setOnLongClickListener(this);
                    }else{
                        holder = lv.findViewHolderForLayoutPosition(4);
                        if(holder != null) {
                            holder.itemView.setOnLongClickListener(this);
                        }
                    }
                    mDebugAction = true;
                }
                return true;
            });
        }
        mDeviceFarmVersion = findPreference(getString(R.string.key_device_farm_version_information));

        mLicense = findPreference(getString(R.string.key_licence));
        mLicense.setOnPreferenceClickListener((preference) -> {
            mPresenter.onLicenseAction();
            return true;
        });
        mEula = findPreference(getString(R.string.key_eula));
        mEula.setOnPreferenceClickListener((preference) -> {
            mPresenter.onEulaAction();
            return true;
        });
        mPrivacyPolicy = findPreference(getString(R.string.key_privacy_policy));
        mPrivacyPolicy.setOnPreferenceClickListener((preference) -> {
            mPresenter.onPrivacyPolicyAction();

            return true;
        });
        mAppVersion = findPreference(getString(R.string.key_app_version));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected InformationPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_INFORMATION;
    }

    /**
     * ブラウザー起動
     * @param intent intent
     */
    @Override
    public void startBrowser(Intent intent){
        startActivity(intent);
    }

    /**
     * デバイス情報設定
     * @param deviceInformation デバイス情報
     */
    @Override
    public void setDeviceInformation(String deviceInformation){
        mDeviceInfo.setSummary(deviceInformation);
    }

    /**
     * デバイスバージョン設定
     * @param isVisible 表示/非表示
     * @param deviceFarmVersion デバイスバージョン
     */
    @Override
    public void setDeviceFarmVersion(boolean isVisible, String deviceFarmVersion){
        mDeviceFarmVersion.setVisible(isVisible);
        mDeviceFarmVersion.setSummary(deviceFarmVersion);
    }

    /**
     * アプリバージョン設定
     * @param appVersion アプリバージョン
     */
    @Override
    public void setAppVersion(String appVersion){
        mAppVersion.setSummary(appVersion);
    }

    @Override
    public boolean onLongClick(View v) {
        if(BuildConfig.DEBUG) {
            if (mDebugAction) {
                mPresenter.onDebugSettingAction();
            }
        }
        return true;
    }

}
