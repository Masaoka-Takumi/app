package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.EnumSet;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.repository.SettingListRepository;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Bluetooth設定操作.
 */
public class ControlBtSetting {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject SettingListRepository mSettingListRepository;
    @Inject BtSettingController mBtSettingController;

    /**
     * コンストラクタ
     */
    @Inject
    public ControlBtSetting() {

    }

    /**
     * A2DPデバイス切り替え
     * <p>
     * 車載機がA2DPの切り替えをBT-Audioソースでしか行えないため、
     * アプリ側からは切り替えを実施しないようにする。
     * <p>
     * 以下状態の場合、実行できない。
     * ・切り替え対象のデバイスが見つからない
     * ・切り替え対象のデバイスが既にA2DP接続されている
     *
     * @param bdAddress 切り替え対象のBDアドレス
     * @throws NullPointerException {@code bdAddress} がnull
     */
    @Deprecated
    public void switchAudioDevice(@NonNull String bdAddress) {
        checkNotNull(bdAddress);

        mHandler.post(() -> {
            DeviceListItem deviceListItem = mSettingListRepository.findByBdAddress(bdAddress, SettingListType.DEVICE_LIST);
            if (deviceListItem != null && !deviceListItem.audioConnected) {
                mBtSettingController.switchAudioDevice(deviceListItem);
            } else {
                Timber.w("switchAudioDevice() Already connected.");
            }
        });
    }

    /**
     * サービス接続
     * 以下状態の場合、実行できない。
     * ・接続対象のデバイスが見つからない
     *
     * @param bdAddress    接続対象のBDアドレス
     * @param serviceTypes サービス種別
     */
    public void connectPhoneServiceCommand(@NonNull String bdAddress, @NonNull EnumSet<ConnectServiceType> serviceTypes) {
        checkNotNull(bdAddress);
        checkNotNull(serviceTypes);

        mHandler.post(() -> {
            DeviceListItem deviceListItem = mSettingListRepository.findByBdAddress(bdAddress, SettingListType.DEVICE_LIST);
            if (deviceListItem != null) {
                mBtSettingController.sendPhoneServiceCommand(deviceListItem, serviceTypes, PhoneConnectRequestType.CONNECT);
            } else {
                Timber.w("connectPhoneServiceCommand() Target device not found.");
            }
        });
    }

    /**
     * サービス切断
     * 以下状態の場合、実行できない。
     * ・切断対象のデバイスが見つからない
     *
     * @param bdAddress    切断対象のBDアドレス
     * @param serviceTypes サービス種別
     */
    public void disconnectPhoneServiceCommand(@NonNull String bdAddress, @NonNull EnumSet<ConnectServiceType> serviceTypes) {
        checkNotNull(bdAddress);
        checkNotNull(serviceTypes);

        mHandler.post(() -> {
            DeviceListItem deviceListItem = mSettingListRepository.findByBdAddress(bdAddress, SettingListType.DEVICE_LIST);
            if (deviceListItem != null) {
                mBtSettingController.sendPhoneServiceCommand(deviceListItem, serviceTypes, PhoneConnectRequestType.DISCONNECT);
            } else {
                Timber.w("disconnectPhoneServiceCommand() Target device not found.");
            }
        });
    }

    /**
     * デバイスペアリング処理
     * <p>
     * サーチにより取得した端末とペアリングする。
     * 以下状態の場合、実行できない。
     * ・ペアリング対象のデバイスが見つからない
     * <p>
     * 既にペアリングされているデバイスに対してペアリングを実施すると、
     * ペアリングに対しての完了通知が発行されないので、常に実行中というステータスになってしまう。
     * そのため、実行前にペアリングされているかをチェックしペアリングされている場合は失敗(false)を返すようにする。
     *
     * @param bdAddress    ペアリング対象のBDアドレス
     * @param serviceTypes 接続するサービスの種別
     * @return 成功したか否か {@code true}:成功 {@code false}:失敗(対象が既にペアリングされている)
     */
    public boolean pairingDevice(@NonNull String bdAddress, @NonNull EnumSet<ConnectServiceType> serviceTypes) {
        checkNotNull(bdAddress);
        checkNotNull(serviceTypes);

        // 既にペアリングしているかチェック
        if (mSettingListRepository.findByBdAddress(bdAddress, SettingListType.DEVICE_LIST) != null) {
            return false;
        }

        mHandler.post(() -> {
            SearchListItem searchListItem = mSettingListRepository.findByBdAddress(bdAddress, SettingListType.SEARCH_LIST);
            if (searchListItem != null) {
                mBtSettingController.pairingDevice(searchListItem, serviceTypes);
            } else {
                Timber.w("pairingDevice() Target device not found.");
            }
        });
        return true;
    }

    /**
     * ペアリング解除
     * 以下状態の場合、実行できない。
     * ・ペアリング解除対象のデバイスが見つからない
     *
     * @param bdAddress 解除対象のBDアドレス
     */
    public void deleteDevice(@NonNull String bdAddress) {
        checkNotNull(bdAddress);

        mHandler.post(() -> {
            DeviceListItem deviceListItem = mSettingListRepository.findByBdAddress(bdAddress, SettingListType.DEVICE_LIST);
            if (deviceListItem != null) {
                mBtSettingController.deleteDevice(deviceListItem);
            } else {
                Timber.w("deleteDevice() Target device not found.");
            }
        });
    }

    /**
     * Bluetoothデバイスサーチ開始
     * <p>
     * A2DP接続をしている場合は切断する。
     */
    public void startPhoneSearch() {

        mHandler.post(() -> {
            DeviceListItem deviceListItem = mSettingListRepository.getAudioConnectedDevice();
            mBtSettingController.startPhoneSearch(deviceListItem);
        });
    }

    /**
     * Bluetoothデバイスサーチ停止(A2DP再接続しない)
     * <p>
     * ペアリングを実行する時やデバイス検索画面で検索中止をする際に使用する。
     */
    public void stopPhoneSearch() {

        mHandler.post(() -> mBtSettingController.stopPhoneSearch(false));
    }

    /**
     * Bluetoothデバイスサーチ停止(A2DP再接続する)
     * <p>
     * 開始時にA2DP接続していた場合は復帰する。
     * デバイス検索画面を終了する際に使用する。
     */
    public void stopPhoneSearchAndReconnectA2dp() {

        mHandler.post(() -> mBtSettingController.stopPhoneSearch(true));
    }
}
