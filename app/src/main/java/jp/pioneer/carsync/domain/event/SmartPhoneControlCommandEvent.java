package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SmartPhone操作コマンドイベント.
 * <p>
 * 車載機からSmartPhoneを操作する場合に発生する。
 */
public class SmartPhoneControlCommandEvent {
    /** SmartPhone操作コマンド. */
    public final SmartPhoneControlCommand command;

    /**
     * コンストラクタ.
     *
     * @param command アクション
     * @throws NullPointerException {@code command}がnull
     */
    public SmartPhoneControlCommandEvent(@NonNull SmartPhoneControlCommand command) {
        this.command = checkNotNull(command);
    }
}
