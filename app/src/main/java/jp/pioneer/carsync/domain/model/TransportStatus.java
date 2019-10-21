package jp.pioneer.carsync.domain.model;

/**
 * 通信路の状態.
 * <p>
 * フォアグラウンドサービスを行うかの判定に使用する。
 * {@link #UNUSED}以外はフォアグラウンドサービス化する必要がある。{@link #UNUSED}の場合であっても、
 * 音楽再生中のような要因でフォアグラウンドサービス化が必要だが、ここでは対象外。
 */
public enum TransportStatus {
    /** 未使用. */
    UNUSED,
    /** 車載機からのBluetooth（SPP）接続待ち. */
    BLUETOOTH_LISTENING,
    /** 車載機とBluetooth（SPP）接続中. */
    BLUETOOTH_CONNECTING,
    /** 車載機とUSB（AOA）接続中. */
    USB_CONNECTING
}
