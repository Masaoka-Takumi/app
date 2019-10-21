package jp.pioneer.carsync.domain.component;

/**
 * BtAudioのSourceController
 */
public interface BtAudioSourceController extends SourceController {

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
