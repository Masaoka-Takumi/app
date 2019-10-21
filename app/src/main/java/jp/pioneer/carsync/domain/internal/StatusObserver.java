package jp.pioneer.carsync.domain.internal;

import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * ステータス監視.
 */
public interface StatusObserver {

    /**
     * 初期化.
     *
     * @param statusHolder ステータスホルダー
     */
    void initialize(StatusHolder statusHolder);

    /**
     * ステータス更新.
     *
     * @param statusHolder ステータスホルダー
     */
    void onStatusUpdate(StatusHolder statusHolder);
}
