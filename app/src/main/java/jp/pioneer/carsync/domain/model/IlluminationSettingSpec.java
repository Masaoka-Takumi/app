package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

/**
 * イルミ設定スペック.
 */
public class IlluminationSettingSpec {
    /** 蛍の光風設定対応. */
    public boolean hotaruNoHikariLikeSettingSupported;
    /** BT PHONE COLOR設定対応. */
    public boolean btPhoneColorSettingSupported;
    /** BRIGHTNESS設定対応. */
    public boolean brightnessSettingSupported;
    /** DIMMER設定対応. */
    public boolean dimmerSettingSupported;
    /** COLOR CUSTOM (DISP)設定対応. */
    public boolean colorCustomDispSettingSupported;
    /** COLOR CUSTOM (KEY)設定対応. */
    public boolean colorCustomKeySettingSupported;
    /** DISP COLOR 設定対応. */
    public boolean dispColorSettingSupported;
    /** KEY COLOR 設定対応. */
    public boolean keyColorSettingSupported;
    /** メッセージ受信通知COLOR設定対応. */
    public boolean incomingMessageColorSettingSupported;
    /** COLOR CUSTOM設定(共通設定モデル用)対応. */
    public boolean commonColorCustomSettingSupported;
    /** COLOR設定(共通設定モデル用)対応. */
    public boolean commonColorSettingSupported;
    /** (SPH) BT PHONE COLOR設定対応. */
    public boolean sphBtPhoneColorSettingSupported;
    /** オーディオレベルメータ連動設定対応. */
    public boolean audioLevelMeterLinkedSettingSupported;
    /** カスタム発光パターン設定対応. */
    public boolean customFlashPatternSettingSupported;
    /** DISP BRIGHTNESS設定（個別設定モデル用）対応. */
    public boolean dispBrightnessSettingSupported;
    /** KEY BRIGHTNESS設定（個別設定モデル用）対応. */
    public boolean keyBrightnessSettingSupported;
    /** サポートDimmer群. */
    public Set<DimmerSetting.Dimmer> supportedDimmers;

    /**
     * コンストラクタ.
     */
    public IlluminationSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        hotaruNoHikariLikeSettingSupported = false;
        btPhoneColorSettingSupported = false;
        brightnessSettingSupported = false;
        dimmerSettingSupported = false;
        colorCustomDispSettingSupported = false;
        colorCustomKeySettingSupported = false;
        dispColorSettingSupported = false;
        keyColorSettingSupported = false;
        incomingMessageColorSettingSupported = false;
        commonColorCustomSettingSupported = false;
        commonColorSettingSupported = false;
        sphBtPhoneColorSettingSupported = false;
        audioLevelMeterLinkedSettingSupported = false;
        customFlashPatternSettingSupported = false;
        dispBrightnessSettingSupported = false;
        keyBrightnessSettingSupported = false;
        supportedDimmers = EnumSet.noneOf(DimmerSetting.Dimmer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("hotaruNoHikariLikeSettingSupported", hotaruNoHikariLikeSettingSupported)
                .add("btPhoneColorSettingSupported", btPhoneColorSettingSupported)
                .add("brightnessSettingSupported", brightnessSettingSupported)
                .add("dimmerSettingSupported", dimmerSettingSupported)
                .add("colorCustomDispSettingSupported", colorCustomDispSettingSupported)
                .add("colorCustomKeySettingSupported", colorCustomKeySettingSupported)
                .add("dispColorSettingSupported", dispColorSettingSupported)
                .add("keyColorSettingSupported", keyColorSettingSupported)
                .add("incomingMessageColorSettingSupported", incomingMessageColorSettingSupported)
                .add("commonColorCustomSettingSupported", commonColorCustomSettingSupported)
                .add("commonColorSettingSupported", commonColorSettingSupported)
                .add("sphBtPhoneColorSettingSupported", sphBtPhoneColorSettingSupported)
                .add("audioLevelMeterLinkedSettingSupported", audioLevelMeterLinkedSettingSupported)
                .add("customFlashPatternSettingSupported", customFlashPatternSettingSupported)
                .add("dispBrightnessSettingSupported", dispBrightnessSettingSupported)
                .add("keyBrightnessSettingSupported", keyBrightnessSettingSupported)
                .add("supportDimmers", supportedDimmers)
                .toString();
    }
}
