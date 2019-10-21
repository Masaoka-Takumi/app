package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList.AudioConnectStatus;

/**
 * デバイス切り替えリクエスト.
 */
public class AudioDeviceSwitchRequest {
    /** デバイス切り替え対象のデバイス. */
    public final DeviceListItem targetDevice;

    /**
     * コンストラクタ.
     *
     * @param targetDevice デバイス切り替え対象のデバイス
     */
    public AudioDeviceSwitchRequest(@NonNull DeviceListItem targetDevice) {
        this.targetDevice = targetDevice;
    }

    /**
     * Audio接続状態取得.
     *
     * @param bdAddress 取得対象のBDアドレス
     * @param statusHolder ステータスホルダー
     * @return 接続状態
     */
    public AudioConnectStatus getAudioConnectStatus(@NonNull String bdAddress, @NonNull StatusHolder statusHolder){
        AudioConnectStatus status;

        if(targetDevice.bdAddress.equals(bdAddress)){
            switch(statusHolder.getSettingListInfoMap().audioDeviceSwitchStatus) {
                case COMMAND_SENT:
                case PROCESSING:
                    status = AudioConnectStatus.STATUS_CONNECTING;
                    break;
                case SUCCESS:
                    status = AudioConnectStatus.STATUS_CONNECTED;
                    break;
                case FAILED:
                    status = AudioConnectStatus.STATUS_CONNECT_FAILED;
                    break;
                default:
                    status = AudioConnectStatus.STATUS_DEFAULT;
                    break;
            }
        }
        else{
            status = AudioConnectStatus.STATUS_DEFAULT;
        }
        return status;
    }
}
