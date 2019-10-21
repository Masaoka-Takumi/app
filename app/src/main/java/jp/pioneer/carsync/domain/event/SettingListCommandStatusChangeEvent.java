package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リストコマンド実行状態変更イベント.
 * <p>
 * 実行状態:
 *  <pre>{@code
 *      CommandStatus status = statusHolder.getSettingListInfoMap().各CommandStatus;
 *  }</pre>
 */
public class SettingListCommandStatusChangeEvent {
    /** コマンドステータス種別. */
    @NonNull public final CommandStatusType statusType;

    /**
     * コンストラクタ.
     *
     * @param commandStatusType ステータス種別
     * @throws NullPointerException {@code notification}がnull
     */
    public SettingListCommandStatusChangeEvent(@NonNull CommandStatusType commandStatusType) {
        this.statusType = checkNotNull(commandStatusType);
    }

    /**
     * コマンドステータス種別
     * <p>
     * {@link CommandStatusType#ALL}は初期化した際に設定される.
     */
    public enum CommandStatusType {
        /** A2DPデバイス切り替えコマンド */
        SWITCH_DEVICE,
        /** デバイスサーチコマンド */
        SEARCH_DEVICE,
        /** サービスコネクトコマンド */
        SERVICE_CONNECT,
        /** デバイスペアリングコマンド */
        PAIRING_DEVICE,
        /** デバイスペアリング解除コマンド */
        DELETE_DEVICE,
        /** 全て */
        ALL
    }
}
