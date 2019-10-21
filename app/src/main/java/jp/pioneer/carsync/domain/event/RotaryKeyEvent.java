package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.RotaryKeyAction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ロータリーキーイベント.
 * <p>
 * 車載機からSmartPhoneのListを操作する場合に発生する。
 * {@link RotaryKeyAction#PUSH}時は、{@link #value}は固定値となる。（参照不要）
 */
public class RotaryKeyEvent {
    /** アクション. */
    public final RotaryKeyAction action;
    /** 回転量. */
    public final int value;

    /**
     * コンストラクタ.
     *
     * @param action アクション
     * @param value 回転量
     * @throws NullPointerException {@code action}がnull
     */
    public RotaryKeyEvent(@NonNull RotaryKeyAction action, int value) {
        this.action = checkNotNull(action);
        this.value = value;
    }
}
