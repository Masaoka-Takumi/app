package jp.pioneer.carsync.domain.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.DabBandType;

/**
 * DAB のSourceController.
 */
public interface DabSourceController extends SourceController {

    /**
     * バンド切り替え.
     */
    void toggleBand();

    /**
     * ライブモード切り替え.
     */
    void toggleLiveMode();

    /**
     * タイムシフトモード切り替え.
     */
    void toggleTimeShiftMode();

    /**
     * トグルPlay.
     * <p>
     * PlayとPauseを切り替える。
     */
    void togglePlay();

    /**
     * Seek UP.
     */
    void seekUp();

    /**
     * Seek Cancel.
     */
    void seekCancel();

    /**
     * P.CHアップ.
     */
    void presetUp();

    /**
     * P.CHダウン.
     */
    void presetDown();

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
     * リスト更新
     */
    void updateList();

    /**
     * お気に入り選択.
     *
     * @param index 周波数インデックス
     * @param bandType バンド種別.
     * @param eid EID
     * @param sid SID
     * @param scids SCIdS
     * @throws NullPointerException {@code bandType} がnull.
     */
    void selectFavorite(int index, @NonNull DabBandType bandType, int eid, long sid, int scids);

    /**
     * ABCサーチ実行
     *
     * @param word サーチ文字
     */
    void executeAbcSearch(@NonNull String word);

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}
