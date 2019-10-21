package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.content.QuerySettingListParams;
import jp.pioneer.carsync.domain.repository.SettingListRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 設定リスト取得
 * <p>
 * 取得パラメータ内容のリストを取得する。
 */
public class QuerySettingList {
    @Inject SettingListRepository mSettingListRepository;

    /**
     * コンストラクタ
     */
    @Inject
    public QuerySettingList() {

    }

    /**
     * 実行.
     *
     * @param params 取得パラメータ
     * @return A2DP用Bluetoothデバイスリストがnull
     * @throws NullPointerException {@code params}がnull
     */
    public CursorLoader execute(@NonNull QuerySettingListParams params) {
        checkNotNull(params);

        return mSettingListRepository.getSettingList(params);
    }

}
