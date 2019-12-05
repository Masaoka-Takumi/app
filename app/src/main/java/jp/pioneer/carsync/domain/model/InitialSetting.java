package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * 初期設定.
 */
public class InitialSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** FM STEP. */
    public FmStep fmStep;
    /** AM STEP. */
    public AmStep amStep;
    /** REAR出力設定 / REAR出力設定.. */
    public RearOutputPreoutOutputSetting rearOutputPreoutOutputSetting;
    /** REAR出力設定. */
    public RearOutputSetting rearOutputSetting;
    /** MENU表示言語. */
    public MenuDisplayLanguageType menuDisplayLanguageType;

    /**
     * コンストラクタ.
     */
    public InitialSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        fmStep = FmStep._50KHZ;
        amStep = AmStep._9KHZ;
        rearOutputPreoutOutputSetting = RearOutputPreoutOutputSetting.REAR_REAR;
        rearOutputSetting = RearOutputSetting.REAR;
        menuDisplayLanguageType = MenuDisplayLanguageType.ENGLISH;
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
                .add("fmStep", fmStep)
                .add("amStep", amStep)
                .add("rearOutputPreoutOutputSetting", rearOutputPreoutOutputSetting)
                .add("rearOutputSetting", rearOutputSetting)
                .add("menuDisplayLanguageType", menuDisplayLanguageType)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String FM_STEP = "fm_step";
        public static final String AM_STEP = "am_step";
        public static final String REAR_OUTPUT_PREOUT_OUTPUT = "rear_output_preout_output";
        public static final String REAR_OUTPUT = "rear_output";
        public static final String MENU_DISPLAY_LANGUAGE = "menu_display_language";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(FM_STEP);
                add(AM_STEP);
                add(REAR_OUTPUT_PREOUT_OUTPUT);
                add(REAR_OUTPUT);
                add(MENU_DISPLAY_LANGUAGE);
            }};
        }
    }
}
