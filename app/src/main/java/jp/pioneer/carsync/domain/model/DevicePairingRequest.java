package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

/**
 * ペアリングリクエスト.
 */
public class DevicePairingRequest {
    /** ペアリング対象のデバイス. */
    public final SearchListItem targetDevice;

    /**
     * コンストラクタ.
     *
     * @param targetDevice ペアリング対象のデバイス
     */
    public DevicePairingRequest(@NonNull SearchListItem targetDevice) {
        this.targetDevice = targetDevice;
    }
}
