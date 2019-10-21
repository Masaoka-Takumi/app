package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.entity.SmartPhoneMediaCommand;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SmartPhoneメディアコマンドイベント.
 * <p>
 * 車載機からSmartPhoneのApp Musicソースを操作する場合に発生する。
 * FF（Fast Forward）、RW（Rewind）は一定間隔で発生し、その度に
 * Seekを行うことで実現する。（開始/終了の組み合わせではない）
 */
public class CrpSmartPhoneMediaCommandEvent {
    /** SmartPhoneメディアコマンド. */
    public final SmartPhoneMediaCommand command;

    /**
     * コンストラクタ.
     *
     * @param command アクション
     * @throws NullPointerException {@code command}がnull
     */
    public CrpSmartPhoneMediaCommandEvent(@NonNull SmartPhoneMediaCommand command) {
        this.command = checkNotNull(command);
    }
}
