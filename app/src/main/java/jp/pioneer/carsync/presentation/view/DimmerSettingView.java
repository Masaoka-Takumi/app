package jp.pioneer.carsync.presentation.view;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.presentation.model.DimmerListType;

/**
 * ディマー設定画面のinterface
 */

public interface DimmerSettingView {
    /**
     * ページ設定
     *
     * @param page ページ
     */
    void setPage(int page);

    /**
     * ディマー時間設定
     *
     * @param startHour 開始時刻
     * @param startMinute 開始時刻
     * @param endHour   終了時刻
     * @param endMinute   終了時刻
     */
    void setDimmerSchedule(int startHour, int startMinute, int endHour, int endMinute) ;

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<DimmerListType> types);

    /**
     * 選択中項目設定
     *
     * @param selected 選択中Dimmer設定値
     */
    void setSelectedItem(DimmerSetting.Dimmer selected);

    /**
     * 設定無効状態の設定
     *
     * @param isEnabled 有効/無効
     */
    void setEnable(boolean isEnabled);
}
