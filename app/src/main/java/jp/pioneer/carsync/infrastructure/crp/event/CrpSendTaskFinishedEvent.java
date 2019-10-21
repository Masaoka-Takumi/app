package jp.pioneer.carsync.infrastructure.crp.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.infrastructure.crp.task.SendTaskId;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link SendTask}の終了イベント.
 */
public class CrpSendTaskFinishedEvent {
    /** 終了したタスクのID. */
    @NonNull public SendTaskId id;

    /**
     * コンストラクタ.
     *
     * @param id 終了したタスクのID
     */
    public CrpSendTaskFinishedEvent(@NonNull SendTaskId id){
        this.id = checkNotNull(id);
    }
}
