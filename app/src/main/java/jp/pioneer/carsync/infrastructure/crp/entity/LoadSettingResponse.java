package jp.pioneer.carsync.infrastructure.crp.entity;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.model.LoadSettingsType;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * LOAD SETTING実行応答.
 */
public class LoadSettingResponse {
    /** LOAD SETTING種別. */
    public final LoadSettingsType type;
    /** 結果. */
    public final ResponseCode result;

    /**
     * コンストラクタ.
     *
     * @param type LOAD SETTING種別
     * @param result 結果
     * @throws NullPointerException {@code type}、または、{@code result}がnull
     */
    public LoadSettingResponse(@NonNull LoadSettingsType type, @NonNull ResponseCode result) {
        this.type = checkNotNull(type);
        this.result = checkNotNull(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("type", type)
                .add("responseCode", result)
                .toString();
    }
}
