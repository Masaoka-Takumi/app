package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.AdasCameraSetting;

/**
 * ADAS設定画面のinterface.
 */

public interface AdasCameraPositionSettingView {
    /**
     * カメラ設定.
     *
     * @param setting 設定内容
     */
    void setCameraSetting(AdasCameraSetting setting);

}
