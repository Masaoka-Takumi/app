package jp.pioneer.carsync.infrastructure.crp.entity;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.model.VoiceRecognitionResponseType;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 音声認識通知応答.
 */
public class VoiceRecognitionResponse {
    /** 音声認識応答種別. */
    public final VoiceRecognitionResponseType type;
    /** 結果. */
    public final ResponseCode result;

    /**
     * コンストラクタ.
     *
     * @param type 音声認識応答種別
     * @param result 結果
     * @throws NullPointerException {@code type}、または、{@code result}がnull
     */
    public VoiceRecognitionResponse(@NonNull VoiceRecognitionResponseType type, @NonNull ResponseCode result) {
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
