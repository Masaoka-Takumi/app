package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * イルミ設定ステータス.
 */
public class IlluminationSettingStatus extends SerialVersion {
    /** 蛍の光風設定有効. */
    public boolean hotaruNoHikariLikeSettingEnabled;
    /** BT PHONE COLOR設定有効. */
    public boolean btPhoneColorSettingEnabled;
    /** BRIGHTNESS設定有効. */
    public boolean brightnessSettingEnabled;
    /** DIMMER設定有効. */
    public boolean dimmerSettingEnabled;
    /** COLOR CUSTOM (DISP)設定有効. */
    public boolean colorCustomDispSettingEnabled;
    /** COLOR CUSTOM (KEY)設定有効. */
    public boolean colorCustomKeySettingEnabled;
    /** DISP COLOR 設定有効. */
    public boolean dispColorSettingEnabled;
    /** KEY COLOR 設定有効. */
    public boolean keyColorSettingEnabled;
    /** メッセージ受信通知COLOR設定有効. */
    public boolean incomingMessageColorSettingEnabled;
    /** COLOR CUSTOM設定(共通設定モデル用)有効. */
    public boolean commonColorCustomSettingEnabled;
    /** COLOR設定(共通設定モデル用)有効. */
    public boolean commonColorSettingEnabled;
    /** (SPH) BT PHONE COLOR設定有効. */
    public boolean sphBtPhoneColorSettingEnabled;
    /** オーディオレベルメータ連動設定有効. */
    public boolean audioLevelMeterLinkedSettingEnabled;
    /** カスタム発光パターン設定有効. */
    public boolean customFlashPatternSettingEnabled;
    /** DISP BRIGHTNESS設定（個別設定モデル用）有効. */
    public boolean dispBrightnessSettingEnabled;
    /** KEY BRIGHTNESS設定（個別設定モデル用）有効. */
    public boolean keyBrightnessSettingEnabled;

    /**
     * コンストラクタ.
     */
    public IlluminationSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        hotaruNoHikariLikeSettingEnabled = false;
        btPhoneColorSettingEnabled = false;
        brightnessSettingEnabled = false;
        dimmerSettingEnabled = false;
        colorCustomDispSettingEnabled = false;
        colorCustomKeySettingEnabled = false;
        dispColorSettingEnabled = false;
        keyColorSettingEnabled = false;
        incomingMessageColorSettingEnabled = false;
        commonColorCustomSettingEnabled = false;
        commonColorSettingEnabled = false;
        sphBtPhoneColorSettingEnabled = false;
        audioLevelMeterLinkedSettingEnabled = false;
        customFlashPatternSettingEnabled = false;
        dispBrightnessSettingEnabled = false;
        keyBrightnessSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("hotaruNoHikariLikeSettingEnabled", hotaruNoHikariLikeSettingEnabled)
                .add("btPhoneColorSettingEnabled", btPhoneColorSettingEnabled)
                .add("brightnessSettingEnabled", brightnessSettingEnabled)
                .add("dimmerSettingEnabled", dimmerSettingEnabled)
                .add("colorCustomDispSettingEnabled", colorCustomDispSettingEnabled)
                .add("colorCustomKeySettingEnabled", colorCustomKeySettingEnabled)
                .add("dispColorSettingEnabled", dispColorSettingEnabled)
                .add("keyColorSettingEnabled", keyColorSettingEnabled)
                .add("incomingMessageColorSettingEnabled", incomingMessageColorSettingEnabled)
                .add("commonColorCustomSettingEnabled", commonColorCustomSettingEnabled)
                .add("commonColorSettingEnabled", commonColorSettingEnabled)
                .add("sphBtPhoneColorSettingEnabled", sphBtPhoneColorSettingEnabled)
                .add("audioLevelMeterLinkedSettingEnabled", audioLevelMeterLinkedSettingEnabled)
                .add("customFlashPatternSettingEnabled", customFlashPatternSettingEnabled)
                .add("dispBrightnessSettingEnabled", dispBrightnessSettingEnabled)
                .add("keyBrightnessSettingEnabled", keyBrightnessSettingEnabled)
                .toString();
    }
}
