package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.CarSafetyPresenter;
import jp.pioneer.carsync.presentation.view.CarSafetyView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * CarSafety設定画面
 */
public class CarSafetyFragment extends AbstractPreferenceFragment<CarSafetyPresenter, CarSafetyView> implements CarSafetyView, StatusPopupDialogFragment.Callback {
    public static final String TAG_DIALOG_ADAS_BILLING = "adas_billing";
    @Inject CarSafetyPresenter mPresenter;
    private Preference mImpactDetection;
    private Preference mParkingSensor;
    private Preference mAdas;

    /**
     * コンストラクタ
     */
    @Inject
    public CarSafetyFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ImpactDetectionSettingsFragment
     */
    public static CarSafetyFragment newInstance(Bundle args) {
        CarSafetyFragment fragment = new CarSafetyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_car_safety, rootKey);

        mParkingSensor = findPreference(getString(R.string.key_parking_sensor_setting));
        mParkingSensor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectParkingSensorAction();
            return true;
        });
        mImpactDetection = findPreference(getString(R.string.key_impact_detection_setting));
        mImpactDetection.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectImpactDetectionAction();
            return true;
        });
        mAdas = findPreference(getString(R.string.key_adas_setting));
        mAdas.setOnPreferenceClickListener((preference) -> {
            getPresenter().onSelectAdasAction();
            return true;
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected CarSafetyPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.CAR_SAFETY_SETTINGS;
    }

    @Override
    public void setParkingSensorSetting(boolean isSupported,boolean isEnabled) {
        mParkingSensor.setVisible(isSupported);
        mParkingSensor.setEnabled(isEnabled);
    }

    @Override
    public void setImpactDetectionSetting(boolean isSupported) {
        mImpactDetection.setVisible(isSupported);
    }

    @Override
    public void setAdasSetting(boolean isSupported) {
        mAdas.setVisible(isSupported);
    }

    @Override
    public void setPurchaseIcon(boolean isPurchased) {
        if(isPurchased) {
            mAdas.setIcon(null);
        }else{
            mAdas.setIcon(R.drawable.p1551_purchaseicon);
        }
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if (tag.equals(TAG_DIALOG_ADAS_BILLING)) {
            getPresenter().onAdasBillingAction();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
    }
}
