package jp.pioneer.carsync.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 設定.
 * <p>
 * 各設定の共通クラス.
 */
public class Setting extends SerialVersion {
    private Set<String> acquiredSettingsTag;

    /**
     * クリア.
     */
    public void clear(){
        acquiredSettingsTag = new HashSet<>();
    }

    /**
     * 設定取得.
     *
     * @param tag 設定のタグ
     */
    public void acquiredSetting(String tag){
        if(!acquiredSettingsTag.contains(tag)) {
            acquiredSettingsTag.add(tag);
        }
    }

    /**
     * 設定を取得したか否か.
     *
     * @param tag 設定のタグ
     */
    public boolean isSettingAcquisition(String tag){
        return acquiredSettingsTag.contains(tag);
    }

    /**
     * 全設定を取得したか否か.
     */
    public boolean isAllSettingAcquisition(List<String> tags){
        return acquiredSettingsTag.containsAll(tags);
    }
}
