package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.VoiceRecognizeMicType;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;

/**
 * Voice設定画面の抽象クラス.
 */
public interface VoiceSettingView {
    void setVoiceRecognitionVisible(boolean isVisible);
    void setVoiceRecognitionTypeVisible(boolean isVisible);
    /**
     * 音声認識機能有効設定.
     *
     * @param isEnabled {@code true}:音声認識機能有効 {@code false}:音声認識機能無効
     */
    void setVoiceRecognitionEnabled(boolean isEnabled);

    /**
     * 音声認識機能種別有効設定.
     *
     * @param isEnabled {@code true}:音声認識機能種別有効 {@code false}:音声認識機能種別無効
     */
    void setVoiceRecognitionTypeEnabled(boolean isEnabled);

    /**
     * 音声認識機能種別設定.
     *
     * @param type VoiceRecognizeType
     */
    void setVoiceRecognitionType(VoiceRecognizeType type);

    void setVoiceRecognitionMicTypeVisible(boolean isVisible);

    void setVoiceRecognitionMicTypeEnabled(boolean isEnabled);
    /**
     * 音声認識マイク種別設定.
     *
     * @param type VoiceRecognizeMicType
     */
    void setVoiceRecognitionMicType(VoiceRecognizeMicType type);

}
