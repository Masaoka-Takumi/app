package jp.pioneer.carsync.domain.component;

/**
 * PandoraのSourceController.
 */
public interface PandoraSourceController extends SourceController {

    /**
     * Playトグルボタン処理.
     * <p>
     * PlayとPauseを切り替える.
     */
    void togglePlay();

    /**
     * 次の曲へ移動.
     */
    void skipNextTrack();

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();

    /**
     * ThumbUp処理
     */
    void setThumbUp();

    /**
     * ThumbDown処理
     */
    void setThumbDown();
}
