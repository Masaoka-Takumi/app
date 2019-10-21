package jp.pioneer.carsync.domain.model;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

/**
 * 設定リスト.
 */
public abstract class SettingListItem {
    /**
     * BDアドレス
     */
    public final String bdAddress;

    /**
     * デバイス名
     */
    public final String deviceName;

    /**
     * Audio接続対応
     */
    public final boolean audioSupported;

    /**
     * Phone接続対応
     */
    public final boolean phoneSupported;

    public SettingListItem(
            @NonNull String bdAddress, @NonNull String deviceName,
            boolean audioSupported, boolean phoneSupported
    ) {
        this.bdAddress = bdAddress;
        this.deviceName = deviceName;
        this.audioSupported = audioSupported;
        this.phoneSupported = phoneSupported;
    }

    /**
     * 文字列追加.
     * <p>
     * {@link #toString()}でスーパークラスとサブクラスの情報を同列に出力するための仕掛け。
     * サブクラスはスーパークラスの本メソッドを呼んだ後にサブクラスで追加したフィールドの
     * 情報を追加する。
     * サブクラスは{@link #toString()}をオーバーライドしないこと。
     *
     * <pre>{@code
     *  MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
     *      return super.addToString(helper)
     *              .add("サブクラスで追加したフィールド1", 値)
     *              .add("サブクラスで追加したフィールド2", 値)
     *              …
     *  }
     * }</pre>
     *
     * @param helper MoreObjects.ToStringHelper
     * @return MoreObjects.ToStringHelper
     */
    @CallSuper
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return MoreObjects.toStringHelper("")
                .add("bdAddress", bdAddress)
                .add("deviceName", deviceName)
                .add("audioSupported", audioSupported)
                .add("phoneSupported", phoneSupported);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return addToString(MoreObjects.toStringHelper(""))
                .toString();
    }
}
