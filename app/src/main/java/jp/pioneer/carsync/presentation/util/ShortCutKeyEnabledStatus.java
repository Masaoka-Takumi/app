package jp.pioneer.carsync.presentation.util;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;

/**
 * ショートカット領域表示/非表示を取得するクラス
 */
public class ShortCutKeyEnabledStatus {

    @Inject AppSharedPreference mPreference;
    @Inject YouTubeLinkStatus mYouTubeLinkStatus;
    @Inject GetStatusHolder mGetStatusHolder;

    @Inject
    public ShortCutKeyEnabledStatus() {
    }

    /**
     * ショートカット領域を表示するかどうかの取得
     *
     * @return
     * {@code true}:ショートカット領域表示
     * {@code false}:ショートカット領域非表示
     */
    public boolean isShortCutKeyEnabled(){
        // アプリ設定のショートカットボタン設定がONか
        boolean isShortCutButtonEnabled = mPreference.isShortCutButtonEnabled();
        // 音声認識がAlexaかどうか
        boolean isVoiceRecognitionAlexa = mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry
                && mPreference.getVoiceRecognitionType() == VoiceRecognizeType.ALEXA;
        // YouTubeLink機能がONかどうか
        boolean isYouTubeLinkEnabled = mYouTubeLinkStatus.isYouTubeLinkEnabled();

        return isVoiceRecognitionAlexa || isShortCutButtonEnabled || isYouTubeLinkEnabled;
    }

}
