package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * プロトコルバージョン.
 */
public class ProtocolVersion {
    private static final int NONE = -1;
    /** 不明（未接続）. */
    public static final ProtocolVersion UNKNOWN = new ProtocolVersion(NONE, NONE);
    /** Version1. */
    public static final ProtocolVersion V1 = new ProtocolVersion(1);
    /** Version2. */
    public static final ProtocolVersion V2 = new ProtocolVersion(2);
    /** Version3&#046;x. */
    public static final ProtocolVersion V3 = new ProtocolVersion(3, 0);
    /** Version4&#046;x. */
    public static final ProtocolVersion V4 = new ProtocolVersion(4, 0);

    /** メジャーバージョン. */
    public final int major;
    /** マイナーバージョン. */
    public final int minor;

    /**
     * コンストラクタ.
     * <p>
     * v2以前用。v3以降は{@link #ProtocolVersion(int, int)}を使用する。
     *
     * @param major メジャーバージョン
     */
    public ProtocolVersion(int major) {
        this(major, NONE);
    }

    /**
     * コンストラクタ.
     * <p>
     * v3以降用。
     *
     * @param major メジャーバージョン
     * @param minor マイナーバージョン
     */
    public ProtocolVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * 本インスタンス > 比較対象か否か.
     *
     * @param v 比較対象のプロトコルバージョン
     * @return {@code true}:{@code v}より大きい。{@code false}:それ以外。
     */
    public boolean isGreaterThan(@NonNull ProtocolVersion v) {
        checkNotNull(v);

        if (this.major > v.major) {
            return true;
        } else if (this.major == v.major) {
            return this.minor > v.minor;
        } else {
            return false;
        }
    }

    /**
     * 本インスタンス >= 比較対象か否か.
     *
     * @param v 比較対象のプロトコルバージョン
     * @return {@code true}:{@code v}以上。{@code false}:それ以外。
     */
    public boolean isGreaterThanOrEqual(@NonNull ProtocolVersion v) {
        return isGreaterThan(v) || equals(v);
    }

    /**
     * 本インスタンス < 比較対象か否か.
     *
     * @param v 比較対象のプロトコルバージョン
     * @return {@code true}:{@code v}より小さい。{@code false}:それ以外。
     */
    public boolean isLessThan(@NonNull ProtocolVersion v) {
        return !isGreaterThan(v) && !equals(v);
    }

    /**
     * 本インスタンス <= 比較対象か否か.
     *
     * @param v 比較対象のプロトコルバージョン
     * @return {@code true}:{@code v}以下。{@code false}:それ以外。
     */
    public boolean isLessThanOrEqual(@NonNull ProtocolVersion v) {
        return isLessThan(v) || equals(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("major", major)
                .add("minor", minor)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ProtocolVersion)) {
            return false;
        }

        ProtocolVersion other = (ProtocolVersion) obj;
        return Objects.equal(major, other.major)
                && Objects.equal(minor, other.minor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(major, minor);
    }
}
