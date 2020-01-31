package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.presenter.ImpactDetectionSettingsPresenter;
import jp.pioneer.carsync.presentation.view.ImpactDetectionSettingsView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 衝突検知機能設定の画面
 */
@RuntimePermissions
public class ImpactDetectionSettingsFragment extends AbstractPreferenceFragment<ImpactDetectionSettingsPresenter, ImpactDetectionSettingsView>
        implements ImpactDetectionSettingsView, StatusPopupDialogFragment.Callback {

    @Inject ImpactDetectionSettingsPresenter mPresenter;
    private SwitchPreferenceCompat mImpactDetection;
    private Preference mImpactMethod;
    private Preference mImpactContact;

    /**
     * コンストラクタ
     */
    @Inject
    public ImpactDetectionSettingsFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return ImpactDetectionSettingsFragment
     */
    public static ImpactDetectionSettingsFragment newInstance(Bundle args) {
        ImpactDetectionSettingsFragment fragment = new ImpactDetectionSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_car_safety_impact_deteciton, rootKey);

        mImpactDetection = (SwitchPreferenceCompat) findPreference(getString(R.string.key_impact_detection));
        mImpactDetection.setLayoutResource(R.layout.element_preference_switch);
        mImpactDetection.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isSettingEnable = (boolean) newValue;
            if(isSettingEnable){
                getPresenter().checkContactNumber();
            }else {
                mPresenter.onImpactDetectionChange(false);
            }
            return true;
        });
        mImpactMethod = findPreference(getString(R.string.key_contact_method));
        mImpactMethod.setOnPreferenceClickListener((preference) -> {
            mPresenter.onImpactNotificationMethodChange();
            return true;
        });
        mImpactContact = findPreference(getString(R.string.key_emergency_contact));
        mImpactContact.setOnPreferenceClickListener((preference) -> {
            getPresenter().onImpactNotificationContactAction();
            return true;
        });
    }

    @Override
    public void checkPermission(){
        ImpactDetectionSettingsFragmentPermissionsDispatcher.enabledImpactDetectionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ImpactDetectionSettingsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE})
    public void enabledImpactDetection() {
        getPresenter().onImpactDetectionChange(true);
    }

    @OnPermissionDenied({Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE})
    public void deniedPermission() {
        getPresenter().onImpactDetectionChange(false);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ImpactDetectionSettingsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.IMPACT_DETECTION_SETTINGS;
    }

    /**
     * 衝突検知機能無効端末設定
     */
    @Override
    public void invalidImpactDetection() {
        mImpactDetection.setEnabled(false);
        mImpactDetection.setLayoutResource(R.layout.element_preference_switch_summary);
        mImpactDetection.setSummary(getString(R.string.set_264));
        mImpactMethod.setEnabled(false);
        mImpactContact.setEnabled(false);
    }

    /**
     * 衝突検知機能有効/無効設定
     * @param enabled 有効/無効
     */
    @Override
    public void setImpactDetectionEnabled(boolean enabled) {
        mImpactDetection.setChecked(enabled);
        mImpactDetection.setSummary("");
        mImpactMethod.setEnabled(enabled);
        mImpactContact.setEnabled(enabled);

    }

    /**
     * 衝突検知機能通知方法設定
     * @param method 通知方法
     */
    @Override
    public void setImpactNotificationMethod(ImpactNotificationMethod method) {
        mImpactMethod.setSummary(method.getLabel());
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if (tag.equals(ImpactDetectionSettingsPresenter.DIALOG_TAG_CONTACT_ALERT)) {
            getPresenter().showContactConfirmDialog();
        }else if(tag.equals(ImpactDetectionSettingsPresenter.DIALOG_TAG_CONTACT_CONFIRM)){
            checkPermission();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {

    }
}
