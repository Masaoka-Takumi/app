package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList.PhoneConnectStatus;

/**
 * Phoneサービスコネクトリクエスト.
 */
public class PhoneServiceRequest {
    /** Phoneサービスコネクト対象デバイス. */
    public final DeviceListItem targetDevice;
    /** 接続リクエスト種別. */
    public final PhoneConnectRequestType connectType;
    /** 接続サービス種別. */
    public final EnumSet<ConnectServiceType> serviceTypes;

    /**
     * コンストラクタ.
     *
     * @param targetDevice Phoneサービスコネクト対象デバイス
     * @param connectType 接続リクエスト種別
     * @param serviceTypes 接続サービス種別
     */
    public PhoneServiceRequest(@NonNull DeviceListItem targetDevice, PhoneConnectRequestType connectType, EnumSet<ConnectServiceType> serviceTypes) {
        this.targetDevice = targetDevice;
        this.connectType = connectType;
        this.serviceTypes = serviceTypes;
    }

    /** 切断リクエストが実行されているか否か. */
    public boolean isDisconnectAction() {
        return connectType == PhoneConnectRequestType.DISCONNECT;
    }

    /** 接続リクエストが実行されているか否か. */
    public boolean isConnectAction() {
        return connectType == PhoneConnectRequestType.DISCONNECT;
    }

    /** Phoneサービスのリクエストが実行されているか否か. */
    public boolean isPhoneService() {
        return serviceTypes.contains(ConnectServiceType.PHONE);
    }

    /**
     * Phone接続状態取得.
     *
     * @param bdAddress 取得対象のBDアドレス
     * @param statusHolder ステータスホルダー
     * @return Phone接続状態
     */
    public PhoneConnectStatus getPhoneConnectStatus(@NonNull String bdAddress, @NonNull StatusHolder statusHolder){
        PhoneConnectStatus status;

        if(targetDevice.bdAddress.equals(bdAddress)){
            if(connectType == PhoneConnectRequestType.CONNECT){
                switch(statusHolder.getSettingListInfoMap().phoneServiceStatus){
                    case COMMAND_SENT:
                    case PROCESSING:
                        status = PhoneConnectStatus.STATUS_CONNECTING;
                        break;
                    case SUCCESS:
                        status = PhoneConnectStatus.STATUS_CONNECTED;
                        break;
                    case FAILED:
                        status = PhoneConnectStatus.STATUS_CONNECT_FAILED;
                        break;
                    default:
                        status = PhoneConnectStatus.STATUS_DEFAULT;
                        break;
                }
            }else if(connectType == PhoneConnectRequestType.DISCONNECT){
                switch(statusHolder.getSettingListInfoMap().phoneServiceStatus){
                    case COMMAND_SENT:
                    case PROCESSING:
                        status = PhoneConnectStatus.STATUS_DISCONNECTING;
                        break;
                    case SUCCESS:
                        status = PhoneConnectStatus.STATUS_DISCONNECTED;
                        break;
                    case FAILED:
                        status = PhoneConnectStatus.STATUS_DISCONNECT_FAILED;
                        break;
                    default:
                        status = PhoneConnectStatus.STATUS_DEFAULT;
                        break;
                }
            }else{
                status = PhoneConnectStatus.STATUS_DEFAULT;
            }
        }
        else{
            status = PhoneConnectStatus.STATUS_DEFAULT;
        }
        return status;
    }
}
