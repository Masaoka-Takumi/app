package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ペアリングデバイスリスト.
 */
public class PairingDeviceList {
    /** ペアリング規格種別. */
    public PairingSpecType type;
    /** ペアリングデバイスリスト (BDアドレス, BT Link Key). */
    public ArrayList<PairingDeviceInfo> pairingDeviceList;

    /** アドレスリスト. */
    private ArrayList<String> addressList;

    /**
     * コンストラクタ.
     *
     * @param type ペアリング規格種別.
     * @throws NullPointerException {@code type}がnull
     */
    public PairingDeviceList(@NonNull PairingSpecType type){
        this.type = checkNotNull(type);
        pairingDeviceList = new ArrayList<>();
        addressList = new ArrayList<>();
    }

    /**
     * リセット.
     */
    public void reset(){
        pairingDeviceList.clear();
        addressList.clear();
    }

    /**
     * アドレスリスト取得.
     *
     * @return アドレスリスト
     */
    public ArrayList<String> getAddressList(){
        return addressList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return  MoreObjects.toStringHelper("")
                .add("type", type)
                .add("deviceList", pairingDeviceList)
                .toString();
    }
}
