package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * ナビガイド音声設定.
 */
public class NaviGuideVoiceSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** ナビガイド音声設定ON. */
    public boolean naviGuideVoiceSetting;
    /** ナビガイド音声ボリューム設定. */
    public NaviGuideVoiceVolumeSetting naviGuideVoiceVolumeSetting;

    /**
     * コンストラクタ.
     */
    public NaviGuideVoiceSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        naviGuideVoiceSetting = false;
        naviGuideVoiceVolumeSetting = null;
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
                .add("naviGuideVoiceSetting", naviGuideVoiceSetting)
                .add("naviGuideVoiceVolumeSetting", naviGuideVoiceVolumeSetting)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String NAVI_GUIDE_VOICE = "navi_guide_voice";
        public static final String NAVI_GUIDE_VOICE_VOLUME = "navi_guide_voice_volume";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(NAVI_GUIDE_VOICE);
                add(NAVI_GUIDE_VOICE_VOLUME);
            }};
        }
    }
}
