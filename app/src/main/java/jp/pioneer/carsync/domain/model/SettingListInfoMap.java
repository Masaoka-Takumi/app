package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リスト情報とトランザクションの管理.
 */
public class SettingListInfoMap {
    private Map<SettingListType, SettingListTransaction> mTransactions = new EnumMap<>(SettingListType.class);
    public CommandStatus audioDeviceSwitchStatus;
    public CommandStatus phoneServiceStatus;
    public CommandStatus devicePairingStatus;
    public CommandStatus deviceDeleteStatus;
    public DeviceSearchStatus deviceSearchStatus;

    /**
     * コンストラクタ.
     */
    public SettingListInfoMap() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        mTransactions.clear();
    }

    /**
     * ステータスリセット.
     */
    public void resetStatus() {
        audioDeviceSwitchStatus = null;
        phoneServiceStatus = null;
        devicePairingStatus = null;
        deviceDeleteStatus = null;
        deviceSearchStatus = DeviceSearchStatus.NONE;
    }

    /**
     * 設定リストトランザクション取得.
     *
     * @param type 設定リスト種別
     * @return 設定リストトランザクション
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public SettingListTransaction getTransaction(@NonNull SettingListType type) {
        SettingListTransaction info = mTransactions.get(checkNotNull(type));
        if (info == null) {
            info = new SettingListTransaction();
            mTransactions.put(type, info);
        }

        return info;
    }

    /**
     * 設定リストトランザクション取得.
     *
     * @param id トランザクションID
     * @return 設定リストトランザクション。存在しない場合null。
     */
    @Nullable
    public SettingListTransaction getTransaction(int id) {
        for (Map.Entry<SettingListType, SettingListTransaction> entry : mTransactions.entrySet()) {
            if (entry.getValue().id == id) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * コマンドステータス.
     * <p>
     * 設定リストに関するコマンドを実行した際のステータス
     */
    public enum CommandStatus {
        /** コマンド送信中 */
        COMMAND_SENT,
        /** コマンド実行中 */
        PROCESSING,
        /** コマンド成功 */
        SUCCESS,
        /** コマンド失敗 */
        FAILED;

        public boolean isAvailable() {
            return this == SUCCESS || this == FAILED;
        }
    }
}
