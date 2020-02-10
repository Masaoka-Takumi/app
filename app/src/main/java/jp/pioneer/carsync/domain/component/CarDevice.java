package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import java.util.Set;

import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ReadingRequestType;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;

/**
 * 車載機.
 * <p>
 * 各ソースの制御、設定、リスト処理はそれぞれ分担して実装している。
 *
 * @see AppMusicSourceController
 * @see BtAudioSourceController
 * @see CdSourceController
 * @see PandoraSourceController
 * @see RadioSourceController
 * @see SpotifySourceController
 * @see UsbSourceController
 * @see RadioFunctionSettingUpdater
 * @see CarDeviceMediaRepository
 * @see MediaListController
 */
public interface CarDevice {
    /**
     * SourceController取得.
     *
     * @param sourceType ソース種別
     * @return ソース種別に応じたSourceController
     * @throws NullPointerException {@code sourceType}がnull
     */
    @NonNull
    SourceController getSourceController(@NonNull MediaSourceType sourceType);

    /**
     * ソース選択.
     *
     * @param sourceType ソース種別
     * @throws NullPointerException {@code sourceType}がnull
     */
    void selectSource(@NonNull MediaSourceType sourceType);

    /**
     * 次のソースに変更.
     */
    void changeNextSource();

    /**
     * 車載機画面切り替え.
     *
     * @param screen 車載機画面種別
     * @param direction 遷移方向
     * @throws NullPointerException {@code screen}がnull
     * @throws NullPointerException {@code direction}がnull
     */
    void changeScreen(@NonNull CarDeviceScreen screen, @NonNull TransitionDirection direction);

    /**
     * 読み上げ開始/終了要求.
     *
     * @param type 要求種別
     * @throws NullPointerException {@code type}がnull
     */
    void requestReadNotification(@NonNull ReadingRequestType type);

    /**
     * CUSTOM発光開始/終了要求.
     *
     * @param type 要求種別
     * @throws NullPointerException {@code type}がnull
     */
    void requestCustomFlash(@NonNull CustomFlashRequestType type);

    /**
     * 衝突検知カウントダウン.
     * <p>
     * 車載機へ指定した時間のカウントダウン演出の実行を通知する
     * 0が指定された場合はキャンセルを行う
     *
     * @param timerSecond タイマー(秒)
     */
    void impactDetectionCountdown(int timerSecond);

    /**
     * 電話発信.
     * <p>
     * 車載機へ電話発信通知を送る
     *
     * @param number 電話番号
     * @throws NullPointerException {@code number}がnull
     */
    void phoneCall(@NonNull String number);

    /**
     * メニュー表示解除.
     * <p>
     * 車載機へメニュー表示解除通知を送る
     */
    void exitMenu();

    /**
     * ADAS警告イベント群更新.
     * <p>
     * デバッグ設定用
     * ADAS警告イベントを更新する
     *
     * @param warningEvents イベント群
     * @throws NullPointerException {@code warningEvents}がnull
     */
    void updateAdasWarningStatus(@NonNull Set<AdasWarningEvent> warningEvents);

    /**
     * 車載機音声認識実行通知.
     * <p>
     * 車載機に車載機側の音声認識の実行コマンドを通知する
     *
     * @param start 開始/終了
     */
    void startDeviceVoiceRecognition(boolean start);

    /**
     * 車載機ボリューム指定通知.
     * <p>
     * 車載機に車載機側ボリュームをボリューム値指定で変更するコマンドを通知する
     *
     * @param volume ボリューム値
     */
    void changeDeviceVolume(int volume);
}
