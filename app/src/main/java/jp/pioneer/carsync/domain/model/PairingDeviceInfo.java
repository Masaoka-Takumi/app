package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ペアリングデバイス情報.
 */
public class PairingDeviceInfo {
    /** BDアドレス. */
    public String bdAddress;

    /** BT Link Key */
    public String btLinkKey;

    /**
     * コンストラクタ.
     *
     * @param bdAddress BDアドレス
     * @param btLinkKey BT Link Key
     */
    public PairingDeviceInfo(@NonNull String bdAddress, @NonNull String btLinkKey){
        this.bdAddress = checkNotNull(bdAddress);
        this.btLinkKey = checkNotNull(btLinkKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return  MoreObjects.toStringHelper("")
                .add("bdAddress", bdAddress)
                .add("btLinkKey", btLinkKey)
                .toString();
    }
}
