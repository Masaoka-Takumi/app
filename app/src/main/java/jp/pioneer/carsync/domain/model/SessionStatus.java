package jp.pioneer.carsync.domain.model;

/**
 * セッション状態.
 */
public enum SessionStatus {
    /** 開始中. */
    STARTING,
    /** 開始した. */
    STARTED,
    /** 停止中. */
    STOPPING,
    /** 停止した. */
    STOPPED,
    /** セッション開始保留中. */
    PENDING
}
