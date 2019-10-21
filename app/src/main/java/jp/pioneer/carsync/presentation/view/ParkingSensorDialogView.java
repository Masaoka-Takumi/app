package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.ParkingSensorStatus;

/**
 * パーキングセンサー画面の抽象クラス
 */

public interface ParkingSensorDialogView {

    /**
     * パーキングセンサーステータスの設定
     *
     * @param status パーキングセンサーステータス
   */
    void setSensor(ParkingSensorStatus status);

    /**
     * 楽曲タイトルの設定
     */
    void dismissDialog();
}
