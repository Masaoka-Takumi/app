package jp.pioneer.carsync.domain.component;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.SearchListItem;

/**
 * Bluetooth設定リストController
 */
public interface BtSettingController {

    /**
     * デバイス切り替え
     * <p>
     * A2DPデバイス切り替えに使用する。
     *
     * @param targetDevice 切り替え対象のデバイス情報
     * @throws NullPointerException {@code targetDevice}がnull
     */
    void switchAudioDevice(@NonNull DeviceListItem targetDevice);

    /**
     * サービス接続/切断
     * <p>
     * Bluetoothデバイス画面のAudio/Phoneサービスの接続/切断に使用する。
     *
     * @param targetDevice 対象のデバイス情報
     * @param serviceTypes サービスの種別
     * @param connectType  接続する種別
     * @throws NullPointerException {@code targetDevice}がnull
     * @throws NullPointerException {@code serviceTypes}がnull
     * @throws NullPointerException {@code connectType}がnull
     */
    void sendPhoneServiceCommand(@NonNull DeviceListItem targetDevice, @NonNull EnumSet<ConnectServiceType> serviceTypes, @NonNull PhoneConnectRequestType connectType);

    /**
     * Bluetoothデバイスサーチ開始
     * <p>
     * Bluetoothデバイス検索に使用する。
     * 開始する前にA2DP接続している場合は切断を実行する必要がある。
     * コントローラー内で接続しているデバイスを取得して切断するのではなく、
     * 呼び出し元で接続しているデバイス情報を取得し引数として渡す。
     * {@code targetDevice}がnullの場合はサーチ開始のみ行い、null以外の場合はA2DP切断を実行する。
     *
     * @param targetDevice A2DP接続中のデバイス情報
     */
    void startPhoneSearch(@Nullable DeviceListItem targetDevice);

    /**
     * Bluetoothデバイスサーチ停止
     *
     * @param isReconnectA2dp A2DP歳接続するかどうか {@code true}:再接続を実施する {@code false}:再接続を実施しない
     */
    void stopPhoneSearch(boolean isReconnectA2dp);

    /**
     * デバイスペアリング
     * <p>
     * サーチ中はペアリングできないため、
     * ペアリング実施前にサーチ停止{@link #stopPhoneSearch(boolean)}を実行する。
     *
     * @param targetDevice ペアリング対象のデバイス情報
     * @param serviceTypes サービスの種別
     * @throws NullPointerException {@code targetDevice}がnull
     * @throws NullPointerException {@code serviceTypes}がnull
     */
    void pairingDevice(@NonNull SearchListItem targetDevice, @NonNull EnumSet<ConnectServiceType> serviceTypes);

    /**
     * デバイスペアリング解除
     *
     * @param targetDevice ペアリング解除対象のデバイス情報
     * @throws NullPointerException {@code targetDevice}がnull
     */
    void deleteDevice(@NonNull DeviceListItem targetDevice);

    /**
     * Audio接続状態取得
     *
     * @param bdAddress BDアドレス
     * @return Audio接続状態
     * @throws NullPointerException {@code bdAddress}がnull
     */
    DeviceList.AudioConnectStatus getAudioConnectStatus(@NonNull String bdAddress);

    /**
     * Phone接続状態取得
     *
     * @param bdAddress BDアドレス
     * @return Phone接続状態
     * @throws NullPointerException {@code bdAddress}がnull
     */
    DeviceList.PhoneConnectStatus getPhoneConnectStatus(@NonNull String bdAddress);

    /**
     * 削除状態取得.
     *
     * @param bdAddress BDアドレス
     * @return 削除状態
     * @throws NullPointerException {@code bdAddress}がnull
     */
    DeviceList.DeleteStatus getDeleteStatus(@NonNull String bdAddress);
}
