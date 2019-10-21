package jp.pioneer.carsync.domain.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.HdRadioBandType;

/**
 * HD Radio のSourceController.
 */
public interface HdRadioSourceController extends SourceController {

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
     * お気に入り選択.
     *
     * @param index 周波数インデックス
     * @param bandType バンド種別.
     * @param multicastChannelNumber マルチキャストCH番号
     * @throws NullPointerException {@code bandType} がnull.
     */
    void selectFavorite(int index, @NonNull HdRadioBandType bandType, int multicastChannelNumber);

    /**
     * 周波数プリセットアップ.
     */
    void presetUp();

    /**
     * 周波数プリセットダウン.
     */
    void presetDown();

    /**
     * 周波数マニュアルアップ.
     */
    void manualUp();

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
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}
