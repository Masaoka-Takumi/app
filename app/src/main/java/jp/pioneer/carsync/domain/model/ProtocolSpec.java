package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.List;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CarRemoteProtocolスペック情報.
 */
public class ProtocolSpec {
    /** アプリでサポートしているプロトコルバージョン. */
    private static final List<ProtocolVersion> SUPPORTING_PROTOCOL_VERSION = ImmutableList.of(
            ProtocolVersion.V4,
            ProtocolVersion.V3,
            ProtocolVersion.V2
    );

    /** プロトコルバージョン. */
    private ProtocolVersion mDeviceProtocolVersion;

    /** 車載機のClassID. */
    private CarDeviceClassId mCarDeviceClassId;

    /** 現在使用中のプロトコルバージョン. */
    private ProtocolVersion mConnectingProtocolVersion;

    /**
     * コンストラクタ.
     */
    public ProtocolSpec() {
        reset();
    }

    /**
     * 車載機のプロトコルバージョン取得.
     *
     * @return プロトコルバージョン
     */
    public ProtocolVersion getDeviceProtocolVersion() {
        return mDeviceProtocolVersion;
    }

    /**
     * 車載機のプロトコルバージョン設定.
     *
     * @param version プロトコルバージョン
     * @throws NullPointerException {@code version}がnull
     */
    public void setDeviceProtocolVersion(@NonNull ProtocolVersion version) {
        mDeviceProtocolVersion = checkNotNull(version);
    }

    /**
     * 車載機のClassID取得.
     *
     * @return 車載機のClassID
     */
    public CarDeviceClassId getCarDeviceClassId() {
        return mCarDeviceClassId;
    }

    /**
     * 車載器のClassID設定.
     *
     * @param classId 車載機のClassID
     * @throws NullPointerException {@code classId}がnull
     */
    public void setCarDeviceClassId(@NonNull CarDeviceClassId classId) {
        this.mCarDeviceClassId = checkNotNull(classId);
    }

    /**
     * 接続に使用しているプロトコルバージョン取得.
     *
     * @return 接続に使用しているプロトコルバージョン
     */
    public ProtocolVersion getConnectingProtocolVersion() {
        return mConnectingProtocolVersion;
    }

    /**
     * 接続に使用するプロトコルバージョン設定.
     *
     * @param version 接続に使用するプロトコルバージョン
     * @throws NullPointerException {@code version}がnull
     */
    public void setConnectingProtocolVersion(@NonNull ProtocolVersion version) {
        mConnectingProtocolVersion = checkNotNull(version);
    }

    /**
     * 接続している車載機が専用機(SPH)か否か取得.
     *
     * @return 専用機か否か {@code true}:専用機と接続している。 {@code false}:専用機以外と接続している。
     */
    public boolean isSphCarDevice(){
        if(mCarDeviceClassId != null) {
            return mCarDeviceClassId == CarDeviceClassId.SPH;
        } else {
            return false;
        }
    }

    /**
     * 接続している車載機が市販Marineモデルか否か取得.
     *
     * @return 市販Marineモデルか否か {@code true}:Marineモデルと接続している。 {@code false}:Marineモデル以外と接続している。
     */
    public boolean isMarinCarDevice(){
        if(mCarDeviceClassId != null) {
            return mCarDeviceClassId == CarDeviceClassId.MARIN;
        } else {
            return false;
        }
    }

    /**
     * 接続するのに最適なプロトコルのバージョン取得.
     *
     * @return 接続するのに最適なプロトコルのバージョン。無い場合はnull。
     */
    public ProtocolVersion getSuitableProtocolVersion() {
        return Stream.of(SUPPORTING_PROTOCOL_VERSION)
                .filter(v -> v.equals(mDeviceProtocolVersion))
                .findFirst()
                .orElse(null);
    }

    /**
     * リセット.
     */
    public void reset() {
        mCarDeviceClassId = null;
        mDeviceProtocolVersion = ProtocolVersion.UNKNOWN;
        mConnectingProtocolVersion = ProtocolVersion.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("deviceProtocolVersion", mDeviceProtocolVersion)
                .add("carDeviceClassId", mCarDeviceClassId)
                .add("connectingProtocolVersion", mConnectingProtocolVersion)
                .toString();
    }
}
