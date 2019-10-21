package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Phone設定.
 */
public class PhoneSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** AUTO ANSWER設定ON. */
    public boolean autoAnswerSetting;
    /** AUTO PAIRING設定ON. */
    public boolean autoPairingSetting;

    /**
     * コンストラクタ.
     */
    public PhoneSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        autoAnswerSetting = false;
        autoPairingSetting = false;
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
                .add("autoAnswerSetting", autoAnswerSetting)
                .add("autoPairingSetting", autoPairingSetting)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String AUTO_ANSWER = "auto_answer";
        public static final String AUTO_PAIRING = "auto_pairing";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(AUTO_ANSWER);
                add(AUTO_PAIRING);
            }};
        }
    }
}