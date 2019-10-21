package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * イルミ設定.
 */
public class IlluminationSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** KEY COLOR設定色. */
    public IlluminationColor keyColor;
    /** DISP COLOR設定色. */
    public IlluminationColor dispColor;
    /** KEY COLOR設定色マップ. */
    public IlluminationColorMap keyColorSpec = new IlluminationColorMap();
    /** DISP COLOR設定色マップ. */
    public IlluminationColorMap dispColorSpec = new IlluminationColorMap();
    /** DIMMER設定. */
    public DimmerSetting dimmerSetting = new DimmerSetting();
    /** BRIGHTNESS設定. */
    public BrightnessSetting brightnessSetting = new BrightnessSetting();
    /** DISP BRIGHTNESS設定（個別設定モデル用）. */
    public BrightnessSetting dispBrightnessSetting = new BrightnessSetting();
    /** KEY BRIGHTNESS設定（個別設定モデル用）. */
    public BrightnessSetting keyBrightnessSetting = new BrightnessSetting();
    /** BT PHONE COLOR設定. */
    public BtPhoneColor btPhoneColor;
    /** 蛍の光風設定ON. */
    public boolean illuminationEffect;
    /** オーディオレベルメータ連動設定ON. */
    public boolean audioLevelMeterLinkedSetting;
    /** [SPH] BT PHONE COLOR設定. */
    public SphBtPhoneColorSetting sphBtPhoneColorSetting;
    /** COLOR設定色(共通設定モデル用). */
    public IlluminationColor commonColor;
    /** COLOR設定色マップ(共通設定モデル用). */
    public IlluminationColorMap commonColorSpec = new IlluminationColorMap();
    /** メッセージ受信通知COLOR設定. */
    public IncomingMessageColorSetting incomingMessageColor;
    /** CUSTOM発光パターンの車載機へのリクエスト状態. */
    public RequestStatus customFlashPatternRequestStatus;

    /**
     * コンストラクタ.
     */
    public IlluminationSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        keyColor = null;
        dispColor = null;
        keyColorSpec.reset();
        dispColorSpec.reset();
        dimmerSetting.reset();
        brightnessSetting.reset();
        dispBrightnessSetting.reset();
        keyBrightnessSetting.reset();
        btPhoneColor = null;
        illuminationEffect = false;
        audioLevelMeterLinkedSetting = false;
        sphBtPhoneColorSetting = null;
        commonColor = null;
        commonColorSpec.reset();
        incomingMessageColor = null;
        customFlashPatternRequestStatus = RequestStatus.NOT_SENT;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("keyColor", keyColor)
                .add("dispColor", dispColor)
                .add("keyColorSpec", keyColorSpec)
                .add("dispColorSpec", dispColorSpec)
                .add("dimmerSetting", dimmerSetting)
                .add("brightnessSetting", brightnessSetting)
                .add("dispBrightnessSetting", dispBrightnessSetting)
                .add("keyBrightnessSetting", keyBrightnessSetting)
                .add("btPhoneColor", btPhoneColor)
                .add("illuminationEffect", illuminationEffect)
                .add("audioLevelMeterLinkedSetting", audioLevelMeterLinkedSetting)
                .add("sphBtPhoneColorSetting", sphBtPhoneColorSetting)
                .add("commonColor", commonColor)
                .add("commonColorSpec", commonColorSpec)
                .add("incomingMessageColor", incomingMessageColor)
                .add("customFlashPatternRequestStatus", customFlashPatternRequestStatus)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String KEY_COLOR = "key_color";
        public static final String DISP_COLOR = "disp_color";
        public static final String KEY_COLOR_SPEC = "key_color_spec";
        public static final String DISP_COLOR_SPEC = "disp_color_spec";
        public static final String DIMMER = "dimmer";
        public static final String BRIGHTNESS = "brightness";
        public static final String KEY_BRIGHTNESS = "key_brightness";
        public static final String DISP_BRIGHTNESS = "disp_brightness";
        public static final String BT_PHONE_COLOR = "bt_phone_color";
        public static final String ILLUMINATION_EFFECT = "illumination_effect";
        public static final String AUDIO_LEVEL_MATER = "audio_level_meter";
        public static final String SPH_BT_PHONE_COLOR = "sph_bt_phone_color";
        public static final String COMMON_COLOR = "common_color";
        public static final String COMMON_COLOR_SPEC = "common_color_spec";
        public static final String INCOMING_MESSAGE_COLOR = "incoming_message_color";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(KEY_COLOR);
                add(DISP_COLOR);
                add(KEY_COLOR_SPEC);
                add(DISP_COLOR_SPEC);
                add(DIMMER);
                add(BRIGHTNESS);
                add(BT_PHONE_COLOR);
                add(ILLUMINATION_EFFECT);
                add(AUDIO_LEVEL_MATER);
                add(SPH_BT_PHONE_COLOR);
                add(COMMON_COLOR);
                add(COMMON_COLOR_SPEC);
                add(INCOMING_MESSAGE_COLOR);
            }};
        }
    }
}
