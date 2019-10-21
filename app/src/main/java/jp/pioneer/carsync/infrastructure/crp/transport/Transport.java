package jp.pioneer.carsync.infrastructure.crp.transport;

import android.support.annotation.NonNull;

import java.io.IOException;

import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 車載機との通信路.
 *
 * @see BluetoothTransport
 * @see UsbTransport
 */
public interface Transport {
    /**
     * 接続.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     * @throws IllegalStateException 接続中、切断済
     * @throws IOException I/Oエラー発生
     */
    void connect(@NonNull StatusHolder statusHolder) throws IOException;

    /**
     * 切断.
     */
    void disconnect();

    /**
     * 接続されているか否か取得.
     *
     * @return {@code true}:接続中。{@code false}:未接続。
     */
    boolean isConnected();

    /**
     * 送信.
     *
     * @param bytes 送信データ
     * @throws NullPointerException {@code bytes}がnull
     * @throws IllegalStateException 未接続
     * @throws IOException I/Oエラー発生
     */
    void send(@NonNull byte[] bytes) throws IOException;

    /**
     * 受信.
     *
     * @param bytes データの読み込み先のバッファ
     * @return バッファに読み込まれたバイトの合計数。ストリームの終わりに達してデータがない場合は-1。
     * @throws NullPointerException {@code bytes}がnull
     * @throws IllegalStateException 未接続
     * @throws IOException I/Oエラー発生
     */
    int read(@NonNull byte[] bytes) throws IOException;
}
