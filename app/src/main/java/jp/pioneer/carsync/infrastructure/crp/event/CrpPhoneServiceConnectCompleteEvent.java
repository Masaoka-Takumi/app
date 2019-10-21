package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * サービスコネクトコマンド完了イベント.
 * <p>
 * サービスコネクトコマンドが完了した場合に発生する。
 */
public class CrpPhoneServiceConnectCompleteEvent {
    /** 結果. */
    public final ResponseCode result;
    /** BDアドレス. */
    public final String bdAddress;
    /** 接続種別. */
    public final PhoneConnectRequestType requestType;
    /** サービス種別. */
    public final EnumSet<ConnectServiceType> serviceTypes;

    /**
     * コンストラクタ.
     *
     * @param result 結果
     * @param bdAddress BDアドレス
     * @param requestType 接続種別
     * @param serviceTypes サービス種別
     * @throws NullPointerException {@code result}、{@code bdAddress}、{@code requestType}、{@code serviceTypes}、のいずれかがnull
     * @throws IllegalArgumentException {@code serviceTypes}が不正
     */
    public CrpPhoneServiceConnectCompleteEvent(
            @NonNull ResponseCode result,
            @NonNull String bdAddress,
            @NonNull PhoneConnectRequestType requestType,
            @NonNull @Size(min = 1) EnumSet<ConnectServiceType> serviceTypes) {
        this.result = checkNotNull(result);
        this.bdAddress = checkNotNull(bdAddress);
        this.requestType = checkNotNull(requestType);
        checkArgument(1 <= checkNotNull(serviceTypes).size());
        this.serviceTypes = serviceTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("result", result)
                .add("bdAddress", bdAddress)
                .add("requestType", requestType)
                .add("serviceTypes", serviceTypes)
                .toString();
    }
}
