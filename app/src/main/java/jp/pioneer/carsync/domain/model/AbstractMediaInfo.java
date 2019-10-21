package jp.pioneer.carsync.domain.model;

import android.support.annotation.CallSuper;

import com.google.common.base.MoreObjects;

/**
 * メディア系共通情報.
 */
public abstract class AbstractMediaInfo extends SerialVersion {
    /** 総再生時間（秒）. */
    public int totalSecond;
    /** 総再生時間（秒）. */
    public int currentSecond;
    /** シャッフルモード. */
    public ShuffleMode shuffleMode;
    /** リピートモード. */
    public CarDeviceRepeatMode repeatMode;
    /** 再生状態. */
    public PlaybackMode playbackMode;

    /**
     * リセット.
     */
    @CallSuper
    public void reset() {
        totalSecond = 0;
        currentSecond = 0;
        shuffleMode = ShuffleMode.OFF;
        repeatMode = CarDeviceRepeatMode.ALL;
        playbackMode = PlaybackMode.STOP;
        updateVersion();
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
                .add("totalSecond", totalSecond)
                .add("currentSecond", currentSecond)
                .add("shuffleMode", shuffleMode)
                .add("repeatMode", repeatMode)
                .add("playbackMode", playbackMode);
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
