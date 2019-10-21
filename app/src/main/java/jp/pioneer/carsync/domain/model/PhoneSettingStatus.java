package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Phone設定ステータス.
 */
public class PhoneSettingStatus extends SerialVersion {
    /** AUTO PAIRING設定. */
    public boolean autoPairingSettingEnabled;
    /** AUTO ANSWER設定有効. */
    public boolean autoAnswerSettingEnabled;
    /** ペアリング解除有効. */
    public boolean pairingClearEnabled;
    /** ペアリング追加有効. */
    public boolean pairingAddEnabled;
    /** インクワイアリ有効. */
    public boolean inquiryEnabled;
    /** デバイスリスト有効. */
    public boolean deviceListEnabled;
    /** Audioサービス接続/切断有効. */
    public boolean audioServiceEnabled;
    /** Phoneサービス接続/切断有効. */
    public boolean phoneServiceEnabled;
    /** HF接続数. */
    public ConnectedDevicesCountStatus hfDevicesCountStatus;
    /** ペアリング端末数. */
    public ConnectedDevicesCountStatus pairingDevicesCountStatus;
    /** ANCS Relay Service. */
    public BleServiceConnectStatus ancsRelayServiceConnectStatus;
    /** ANCS Service. */
    public BleServiceConnectStatus ancsServiceConnectStatus;

    /**
     * コンストラクタ.
     */
    public PhoneSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        autoPairingSettingEnabled = false;
        autoAnswerSettingEnabled = false;
        pairingClearEnabled = false;
        pairingAddEnabled = false;
        inquiryEnabled = false;
        deviceListEnabled = false;
        audioServiceEnabled = false;
        phoneServiceEnabled = false;
        hfDevicesCountStatus = ConnectedDevicesCountStatus.NONE;
        pairingDevicesCountStatus = ConnectedDevicesCountStatus.NONE;
        ancsRelayServiceConnectStatus = BleServiceConnectStatus.NOT_CONNECT;
        ancsServiceConnectStatus = BleServiceConnectStatus.NOT_CONNECT;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("autoPairingSettingEnabled", autoPairingSettingEnabled)
                .add("autoAnswerSettingEnabled", autoAnswerSettingEnabled)
                .add("pairingClearEnabled", pairingClearEnabled)
                .add("pairingAddEnabled", pairingAddEnabled)
                .add("inquiryEnabled", inquiryEnabled)
                .add("deviceListEnabled", deviceListEnabled)
                .add("audioServiceEnabled", audioServiceEnabled)
                .add("phoneServiceEnabled", phoneServiceEnabled)
                .add("hfDevicesCountStatus", hfDevicesCountStatus)
                .add("pairingDevicesCountStatus", pairingDevicesCountStatus)
                .add("ancsRelayServiceConnectStatus", ancsRelayServiceConnectStatus)
                .add("ancsServiceConnectStatus", ancsServiceConnectStatus)
                .toString();
    }
}
