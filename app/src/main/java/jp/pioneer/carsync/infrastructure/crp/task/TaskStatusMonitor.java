package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

/**
 * タスク状態監視.
 */
public interface TaskStatusMonitor {
    /**
     * タスク開始ハンドラ.
     *
     * @param sendTask タスク
     */
    void onTaskStarted(@NonNull SendTask sendTask);

    /**
     * タスク終了ハンドラ.
     *
     * @param sendTask タスク
     */
    void onTaskFinished(@NonNull SendTask sendTask);

    /**
     * タスク失敗ハンドラ.
     *
     * @param sendTask タスク
     * @param t 失敗要因
     */
    void onTaskFailed(@NonNull SendTask sendTask, @NonNull Throwable t);

    /**
     * タスクキャンセルハンドラ.
     *
     * @param sendTask タスク
     */
    void onTaskCanceled(@NonNull SendTask sendTask);
}
