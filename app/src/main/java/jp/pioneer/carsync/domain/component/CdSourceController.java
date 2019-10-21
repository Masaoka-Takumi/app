package jp.pioneer.carsync.domain.component;

/**
 * CDのSourceController.
 */
public interface CdSourceController extends SourceController {

    /**
     * Playトグルボタン処理.
     * <p>
     * PlayとPauseを切り替える.
     */
    void togglePlay();

    /**
     * リピートモードトグルボタン処理.
     */
    void toggleRepeatMode();

    /**
     * シャッフルモードトグルボタン処理.
     */
    void toggleShuffleMode();

    /**
     * 次の曲へ移動.
     */
    void skipNextTrack();

    /**
     * 前の曲へ移動.
     */
    void skipPreviousTrack();

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}
