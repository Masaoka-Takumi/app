package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import jp.pioneer.carsync.domain.content.QuerySettingListParams;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.SettingListItem;
import jp.pioneer.carsync.domain.model.SettingListType;

/**
 * 設定リストリポジトリ.
 */
public interface SettingListRepository {

    /**
     * 設定リスト取得.
     *
     * @param params 取得パラメータ
     * @return 設定リストCursorLoader
     * @throws NullPointerException {@code params} がnull
     */
    CursorLoader getSettingList(@NonNull QuerySettingListParams params);

    /**
     * A2DP接続中デバイス情報取得
     *
     * @return A2DP接続中のデバイス情報
     */
    DeviceListItem getAudioConnectedDevice();

    /**
     * デバイス検索
     * <p>
     * BDアドレスからデバイスを検索する
     *
     * @param bdAddress BDアドレス
     * @param listType  リスト種別
     * @return 検索結果
     */
    <T extends SettingListItem> T findByBdAddress(@NonNull String bdAddress, @NonNull SettingListType listType);
}
