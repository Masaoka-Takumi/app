package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.PairingDeviceInfo;

/**
 * ペアリングデバイスリスト画面の抽象クラス
 */

public interface ClassicBtDeviceListView {
    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<PairingDeviceInfo> types);
}
