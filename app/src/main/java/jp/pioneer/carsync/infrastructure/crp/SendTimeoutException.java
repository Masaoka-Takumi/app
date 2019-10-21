package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 送信タイムアウト例外.
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public class SendTimeoutException extends Exception {
    private static final long serialVersionUID = -3309309054393051542L;
    /** 送信タイムアウトになったパケット. */
    public final OutgoingPacket packet;

    /**
     * コンストラクタ.
     *
     * @param packet 送信タイムアウトになったパケット
     * @throws NullPointerException {@code packet}がnull
     */
    public SendTimeoutException(@NonNull OutgoingPacket packet) {
        this.packet = checkNotNull(packet);
    }
}
