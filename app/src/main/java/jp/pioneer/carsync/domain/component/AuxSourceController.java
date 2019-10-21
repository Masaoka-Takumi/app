package jp.pioneer.carsync.domain.component;

/**
 * AUXのSourceController.
 */
public interface AuxSourceController extends SourceController {
    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}
