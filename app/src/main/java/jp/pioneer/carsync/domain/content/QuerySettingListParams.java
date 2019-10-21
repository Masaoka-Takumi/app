package jp.pioneer.carsync.domain.content;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.model.SettingListType;

/**
 * 設定リスト取得用パラメータ.
 * <p>
 * 設定リストを取得する{@link QuerySettingList#execute(QuerySettingListParams)} に使用するパラメータ。
 * 設定リスト種別のSettingListItemから抽出する条件の通りのリストを取得する。
 * 現状falseであることという条件は必要ないため、各抽出条件がtrueであればその条件でリスト取得を実施する。
 * そのため、抽出条件なしでリストを取得する場合は全てfalseにすることで可能となる。
 */
public class QuerySettingListParams {
    /** 設定リストの種別 */
    public final SettingListType settingListType;
    /** 抽出条件：Audio接続対応 */
    public final boolean audioSupported;
    /** 抽出条件：Phone接続対応 */
    public final boolean phoneSupported;
    /** 抽出条件：Audio接続中 */
    public final boolean audioConnected;

    /**
     * コンストラクタ.
     *
     * @param settingListType 設定リストの種別
     * @param audioSupported  Audio接続対応状態
     * @param phoneSupported  Phone接続対応状態
     * @param audioConnected  Audio接続状態
     */
    QuerySettingListParams(@NonNull SettingListType settingListType, boolean audioSupported,
                           boolean phoneSupported, boolean audioConnected) {
        this.settingListType = settingListType;
        this.audioSupported = audioSupported;
        this.phoneSupported = phoneSupported;
        this.audioConnected = audioConnected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("settingListType", settingListType)
                .add("audioSupported", audioSupported)
                .add("phoneSupported", phoneSupported)
                .add("audioConnected", audioConnected)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        QuerySettingListParams other = (QuerySettingListParams) obj;
        return Objects.equal(settingListType, other.settingListType)
                && Objects.equal(audioSupported, other.audioSupported)
                && Objects.equal(phoneSupported, other.phoneSupported)
                && Objects.equal(audioConnected, other.audioConnected);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(settingListType, audioSupported, phoneSupported, audioConnected);
    }
}
