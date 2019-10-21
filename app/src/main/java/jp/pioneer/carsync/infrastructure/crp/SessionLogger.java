package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;

/**
 * セッション開始～終了までのロガー.
 */
public abstract class SessionLogger {
    /**
     * ログタイプ.
     */
    enum Type {
        /** セッション開始中. */
        STARTING,
        /** セッション開始. */
        STARTED,
        /** パケット送信中. */
        SENDING,
        /** パケット送信完了. */
        SENT,
        /** パケット送信ドロップ. */
        SENT_DROPPED,
        /** パケット送信エラー. */
        SEND_ERROR,
        /** パケット送信タイムアウト. */
        SEND_TIMEOUT,
        /** パケット受信. */
        READ,
        /** 受信パケットドロップ. */
        READ_DROPPED,
        /** 無受信タイムアウト. */
        RECEIVE_TIMEOUT,
        /** セッション終了. */
        STOPPED,
        /** デバッグ. */
        DEBUG
    }

    /**
     * セッション開始中.
     */
    public void starting() {
        doInitialize();
        log(Type.STARTING.name(), "");
    }

    /**
     * セッション開始.
     */
    public void started() {
        log(Type.STARTED.name(), "");
    }

    /**
     * パケット送信中.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void sending(@NonNull String format, Object... args) {
        log(Type.SENDING.name(), format, args);
    }

    /**
     * パケット送信完了.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void sent(@NonNull String format, Object... args) {
        log(Type.SENT.name(), format, args);
    }

    /**
     * パケット送信ドロップ.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void sendDropped(@NonNull String format, Object... args) {
        log(Type.SENT_DROPPED.name(), format, args);
    }

    /**
     * パケット送信エラー.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void sendError(@NonNull String format, Object... args) {
        log(Type.SEND_ERROR.name(), format, args);
    }

    /**
     * パケット送信タイムアウト.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void sendTimeout(@NonNull String format, Object... args) {
        log(Type.SEND_TIMEOUT.name(), format, args);
    }

    /**
     * パケット受信.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void read(@NonNull String format, Object... args) {
        log(Type.READ.name(), format, args);
    }

    /**
     * 受信パケットドロップ.
     *
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code format}がnull
     */
    public void readDropped(@NonNull String format, Object... args) {
        log(Type.READ_DROPPED.name(), format, args);
    }

    /**
     * 無受信タイムアウト.
     */
    public void receiveTimeout() {
        log(Type.RECEIVE_TIMEOUT.name(), "");
    }

    /**
     * ログ.
     *
     * @param type タイプ
     * @param format 書式文字列
     * @param args 書式文字列の書式指示子により参照される引数
     * @throws NullPointerException {@code type}、または、{@code format}がnull
     */
    public abstract void log(@NonNull String type, @NonNull String format, Object... args);

    /**
     * セッション終了.
     */
    public void stopped() {
        log(Type.STOPPED.name(), "");
        doTerminate();
    }

    /**
     * ロガー初期化.
     */
    abstract void doInitialize();

    /**
     * ロガー終了.
     */
    abstract void doTerminate();
}
