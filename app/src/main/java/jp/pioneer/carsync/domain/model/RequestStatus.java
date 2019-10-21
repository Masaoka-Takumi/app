package jp.pioneer.carsync.domain.model;

/**
 * 車載機へのリクエスト状態.
 */
public enum RequestStatus {
    /** 未送信. */
    NOT_SENT,
    /** 送信中. */
    SENDING,
    /** 送信済(未完了). */
    SENT_INCOMPLETE,
    /** 送信済(完了). */
    SENT_COMPLETE
}
