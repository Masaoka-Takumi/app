package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.SeekBarPreference;

import java.util.Locale;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.presenter.AdasCameraPositionSettingPresenter;
import jp.pioneer.carsync.presentation.view.AdasCameraPositionSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_007906 on 2018/07/04.
 */

public class AdasCameraPositionSettingFragment extends AbstractPreferenceFragment<AdasCameraPositionSettingPresenter, AdasCameraPositionSettingView>
        implements AdasCameraPositionSettingView {
    private static final double UNIT_CHANGE_M_TO_FT = 3.2808;
    @Inject AdasCameraPositionSettingPresenter mPresenter;
    private SeekBarPreference mCameraHeight;
    private SeekBarPreference mFrontNoseDistance;
    private SeekBarPreference mVehicleWidth;


    /**
     * コンストラクタ
     */
    public AdasCameraPositionSettingFragment(){
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AdasCameraPositionSettingFragment
     */
    public static AdasCameraPositionSettingFragment newInstance(Bundle args) {
        AdasCameraPositionSettingFragment fragment = new AdasCameraPositionSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_adas_camera_position, rootKey);
        mCameraHeight = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_camera_height));
        mCameraHeight.setIcon(R.drawable.p1510_adasseticon_1);
        mCameraHeight.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setCameraHeight((int) newValue * 100);
            return true;
        });

        mFrontNoseDistance = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_front_nose_distance));
        mFrontNoseDistance.setIcon(R.drawable.p1511_adasseticon_2);
        mFrontNoseDistance.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setFrontNoseDistance((int) newValue * 100);
            return true;
        });

        mVehicleWidth = (SeekBarPreference) findPreference(getString(R.string.key_adas_camera_vehicle_width));
        mVehicleWidth.setIcon(R.drawable.p1512_adasseticon_3);
        mVehicleWidth.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().setVehicleWidth((int) newValue * 100);
            return true;
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected AdasCameraPositionSettingPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_CAMERA_SETTING;
    }

    @Override
    public void setCameraSetting(AdasCameraSetting setting) {
        int displayHeight = setting.cameraHeight / 100;
        mCameraHeight.setMin(10);
        mCameraHeight.setMax(15);
        mCameraHeight.setValue(displayHeight);

        int displayDistance = setting.frontNoseDistance / 100;
        mFrontNoseDistance.setMin(1);
        mFrontNoseDistance.setMax(20);
        mFrontNoseDistance.setValue(displayDistance);

        int displayWidth = setting.vehicleWidth / 100;
        mVehicleWidth.setMin(15);
        mVehicleWidth.setMax(21);
        mVehicleWidth.setValue(displayWidth);

        if (getPresenter().getDistanceUnit() == DistanceUnit.METER_KILOMETER) {
            mCameraHeight.setSummary(((double) displayHeight / 10) + getString(R.string.unt_003));
            mFrontNoseDistance.setSummary(((double) displayDistance / 10) + getString(R.string.unt_003));
            mVehicleWidth.setSummary(((double) displayWidth / 10) + getString(R.string.unt_003));
        } else {
            mCameraHeight.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayHeight / 10 * UNIT_CHANGE_M_TO_FT)));
            mFrontNoseDistance.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayDistance / 10 * UNIT_CHANGE_M_TO_FT)));
            mVehicleWidth.setSummary(String.format(Locale.ENGLISH,"%.1f" + getString(R.string.unt_004), ((double) displayWidth / 10 * UNIT_CHANGE_M_TO_FT)));
        }
    }


}
