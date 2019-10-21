package jp.pioneer.carsync.presentation.util;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;

/**
 * YouTubeLinkのON/OFF取得するクラス
 */
public class YouTubeLinkStatus {

    @Inject AppSharedPreference mPreference;

    @Inject
    public YouTubeLinkStatus(){
    }

    /**
     * YouTubeLink設定の表示できる状態かの取得
     *
     * @return
     * {@code true}:YouTubeLink対応の車載機に接続しているor最後に連携した車載機がYouTubeLink対応の場合
     * {@code false}:その他
     */
    public boolean isYouTubeLinkSettingAvailable(){
        CarDeviceClassId carDeviceClassId = mPreference.getLastConnectedCarDeviceClassId();
        CarDeviceDestinationInfo carDeviceDestination =
                CarDeviceDestinationInfo.valueOf((byte)mPreference.getLastConnectedCarDeviceDestination());

        // 車載機情報がない場合(初回起動)は非対応車載機とする
        if(carDeviceDestination == CarDeviceDestinationInfo.UNKNOWN){
            return false;
        }

        // 対応車載機かどうかを判別
        // TODO：対応する車載機の表で判別するほうが拡張性がある？
        if(carDeviceClassId == CarDeviceClassId.SPH && carDeviceDestination != CarDeviceDestinationInfo.UC){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * YouTubeLink機能がONかOFFかの取得
     * ON：YouTubeLink対応車載機連携中かつYouTubeLink設定画面でYouTubeLinkが有効
     * ON：最後に連携した車載機がYouTubeLink対応かつYouTubeLink設定画面でYouTubeLinkが有効
     * OFF：その他
     *
     * @return {@code true}:YouTubeLinkがON　{@code false}:YouTubeLinkがOFF
     */
    public boolean isYouTubeLinkEnabled(){
        return isYouTubeLinkSettingAvailable() && mPreference.isYouTubeLinkSettingEnabled();
    }
}
