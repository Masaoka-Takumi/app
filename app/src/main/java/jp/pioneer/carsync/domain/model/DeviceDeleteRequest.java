package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList.DeleteStatus;

/**
 * ペアリング解除リクエスト.
 */
public class DeviceDeleteRequest {
    /** ペアリング解除対象のデバイス. */
    public final DeviceListItem targetDevice;

    /**
     * コンストラクタ.
     *
     * @param targetDevice ペアリング解除対象のデバイス
     */
    public DeviceDeleteRequest(@NonNull DeviceListItem targetDevice) {
        this.targetDevice = targetDevice;
    }

    /**
     * ペアリング解除状態取得.
     *
     * @param bdAddress 取得対象のBDアドレス
     * @param statusHolder ステータスホルダー
     * @return ペアリング解除状態
     */
    public DeleteStatus getDeleteStatus(@NonNull String bdAddress, @NonNull StatusHolder statusHolder){
        DeleteStatus status;
        if(targetDevice.bdAddress.equals(bdAddress)){
            switch(statusHolder.getSettingListInfoMap().deviceDeleteStatus){
                case COMMAND_SENT:
                case PROCESSING:
                    status = DeleteStatus.STATUS_DELETING;
                    break;
                case SUCCESS:
                    status = DeleteStatus.STATUS_DELETED;
                    break;
                case FAILED:
                    status = DeleteStatus.STATUS_DELETE_FAILED;
                    break;
                default:
                    status = DeleteStatus.STATUS_DEFAULT;
            }
        }
        else{
            status = DeleteStatus.STATUS_DEFAULT;
        }

        return status;
    }
}
