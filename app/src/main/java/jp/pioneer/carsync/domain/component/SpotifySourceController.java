package jp.pioneer.carsync.domain.component;

/**
 * SpotifyのSourceController.
 */
public interface SpotifySourceController extends SourceController {

    /**
     * Playトグルボタン処理.
     * <p>
     * PlayとPauseを切り替える。
     */
    void togglePlay();

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

    /**
     * リピートモードトグルボタン処理
     * <p>
     * リピートモードを切り替える.
     */
    void toggleRepeatMode();

    /**
     * シャッフルモードトグルボタン処理
     * <p>
     * シャッフルモードを切り替える.
     */
    void toggleShuffleMode();

    /**
     * ThumbUp処理.
     */
    void setThumbUp();

    /**
     * ThumbDown処理.
     */
    void setThumbDown();
}
