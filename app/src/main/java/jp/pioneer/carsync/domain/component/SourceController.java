package jp.pioneer.carsync.domain.component;

/**
 * SourceControllerクラス
 */
public interface SourceController {

    /**
     * アクティブ判定.
     *
     * @return 有効になっているかどうか. {@code true}:有効状態 {@code false}:無効状態
     */
    boolean isActive();
}
