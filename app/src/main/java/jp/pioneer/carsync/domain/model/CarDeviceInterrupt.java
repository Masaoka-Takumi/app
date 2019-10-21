package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 割り込み情報.
 */
public class CarDeviceInterrupt {
    /** 割り込み情報種別. */
    public CarDeviceInterruptType type;
    /** 割り込み情報. */
    public String message;

    /**
     * コンストラクタ.
     *
     * @param type 割り込み情報種別
     * @param message 割り込み情報
     * @throws NullPointerException {@code type}がnull
     */
    public CarDeviceInterrupt(@NonNull CarDeviceInterruptType type, @Nullable String message) {
        this.type = checkNotNull(type);
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("type", type)
                .add("message", message)
                .toString();
    }
}
