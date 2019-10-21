package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.UpdateAdasEvent;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.view.AdasCameraPositionSettingView;

/**
 * AdasCameraPositionSettingPresenter
 */

public class AdasCameraPositionSettingPresenter extends Presenter<AdasCameraPositionSettingView>  {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferAdas mPreferCase;
    @Inject AppSharedPreference mPreference;
    /**
     * コンストラクタ
     */
    @Inject
    public AdasCameraPositionSettingPresenter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
    }

    private void updateView() {

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setCameraSetting(mPreferCase.getAdasCameraSetting());
        });
    }

    /**
     * カメラ取付位置 高さ設定
     *
     * @param height 高さ[mm]
     */
    public void setCameraHeight(int height){
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.cameraHeight = height;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * カメラ取付位置 車体先端との距離設定
     *
     * @param distance 距離[mm]
     */
    public void setFrontNoseDistance(int distance){
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.frontNoseDistance = distance;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * 車幅設定
     *
     * @param width 幅[mm]
     */
    public void setVehicleWidth(int width) {
        AdasCameraSetting setting = mPreferCase.getAdasCameraSetting();
        setting.vehicleWidth = width;
        mPreferCase.setAdasCameraSetting(setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    public DistanceUnit getDistanceUnit() {
        return mPreference.getDistanceUnit();
    }
}