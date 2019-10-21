package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 車載機のステータスやアプリ内の再生楽曲情報リポジトリ.
 */
public interface StatusHolderRepository {
    /**
     * 取得.
     *
     * @return StatusHolder
     */
    @NonNull
    StatusHolder get();

    /**
     * ステータス更新リスナー設定.
     * <p>
     * 設定可能なリスナーは1個。後勝ち。
     *
     * @param listener リスナー。解除する場合null。
     */
    void setOnStatusUpdateListener(@Nullable OnStatusUpdateListener listener);

    /**
     * ステータス更新リスナー.
     */
    interface OnStatusUpdateListener {
        /**
         * ステータス更新ハンドラ.
         */
        void onStatusUpdate();
    }
}
