package jp.pioneer.carsync.domain.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioBandType;

/**
 * ラジオのSourceController.
 */
public interface RadioSourceController extends SourceController {

    /**
     * バンド切り替え.
     */
    void toggleBand();

    /**
     * プリセットCH呼び出し.
     * <p>
     * 引数のプリセットCH番号に変更する。
     *
     * @param presetNo プリセット番号.
     * @throws IllegalArgumentException {@code presetNo} の値が不正.
     */
    void callPreset(@IntRange(from = 1, to = 6) int presetNo);

    /**
     * プリセット登録.
     * <p>
     * 引数のプリセットリストインデックスを登録する。
     *
     * @param listIndex プリセットリストインデックス.
     * @throws IllegalArgumentException {@code listIndex} の値が不正.
     */
    void registerPreset(@IntRange(from = 1) int listIndex);

    /**
     * お気に入り選択.
     *
     * @param index    周波数index.
     * @param bandType バンド種別.
     * @param pi       PI.
     * @throws NullPointerException {@code bandType} がnull.
     */
    void selectFavorite(int index, @NonNull RadioBandType bandType, int pi);

    /**
     * 周波数マニュアルアップ.
     */
    void manualUp();

    /**
     * 周波数マニュアルダウン.
     */
    void manualDown();

    /**
     * 周波数マニュアルアップ.
     */
    void presetUp();

    /**
     * 周波数プリセットダウン.
     */
    void presetDown();

    /**
     * Seek UP.
     */
    void seekUp();

    /**
     * BSM開始.
     *
     * @param isStart true:開始 false:終了
     */
    void startBsm(boolean isStart);

    /**
     * PTY Search開始.
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    void startPtySearch(@NonNull PtySearchSetting setting);

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}
