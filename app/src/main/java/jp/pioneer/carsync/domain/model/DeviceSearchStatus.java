package jp.pioneer.carsync.domain.model;

/**
 * デバイス検索状態.
 */
public enum DeviceSearchStatus {
    /** 初期状態. */
    NONE(true, false, false),
    /** 検索開始コマンド送信. */
    START_COMMAND_SENT(false, true, true),
    /** 検索中. */
    SEARCHING(false, true, true),
    /** 完了. */
    COMPLETED(true, false, false),
    /** 検索停止コマンド送信. */
    STOP_COMMAND_SENT(false, false, true),
    /** 失敗. */
    FAILED(true, false, false);

    /** 開始できるか否か. */
    public final boolean startable;
    /** 停止できるか否か. */
    public final boolean stoppable;
    /** 検索中か否か. */
    public final boolean searching;

    /**
     * コンストラクタ.
     *
     * @param startable 開始できるか否か
     * @param stoppable 停止できるか否か
     * @param searching 検索中か否か
     */
    DeviceSearchStatus(boolean startable, boolean stoppable, boolean searching) {
        this.startable = startable;
        this.stoppable = stoppable;
        this.searching = searching;
    }
}
