package jp.pioneer.carsync.presentation.util;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;

public class AlexaAvailableStatus {

    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;

    @Inject
    public AlexaAvailableStatus() {
    }

    /**
     * 音声認識設定値がAlexaでAlexa利用可能国かどうかを判定
     * @return {@code true}:音声認識設定値がAlexaかつAlexa利用可能国　{@code false}:それ以外
     */
    public boolean isVoiceRecognitionTypeAlexaAndAvailable() {
        // Alexa SIM判定でAlexa利用可能国かどうか
        boolean isAlexaAvailableCountry = mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry;
        // 音声認識設定値がAlexaかどうか
        boolean isVoiceRecognitionAlexa = mPreference.getVoiceRecognitionType() == VoiceRecognizeType.ALEXA;

        return isAlexaAvailableCountry && isVoiceRecognitionAlexa;
    }
}
