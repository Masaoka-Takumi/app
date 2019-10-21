package jp.pioneer.carsync.domain.repository;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;

/**
 * 車載機メディアリポジトリ.
 * <p>
 * {@link MediaSourceType#APP_MUSIC}以外のリストを取得する際に使用する。
 */
public interface CarDeviceMediaRepository {
    /**
     * 車載機メディアリスト取得.
     *
     * @param sourceType ソース種別
     * @param bandType バンド種別。Tuner系で結果のリストを特定のバンド種別で絞り込みたい場合に指定する。絞り込まない場合はnull。
     * @return CursorLoader
     * @throws NullPointerException {@code sourceType}がnull
     * @throws IllegalArgumentException {@code sourceType}がプリセットチャンネルリスト非対応
     */
    CursorLoader getPresetChannelList(@NonNull MediaSourceType sourceType, @Nullable BandType bandType);

    /**
     * USBリスト取得.
     *
     * @return CursorLoader
     */
    CursorLoader getUsbList();

    /**
     * DABリスト取得.
     *
     * @return CursorLoader
     */
    CursorLoader getDabList();

    /**
     * リスト項目取得.
     *
     * @param sourceType ソース種別
     * @param listIndex リストインデックス
     * @return リスト項目。ソース種別固有の情報はダウンキャストする。非対応のソース種別、{@code listIndex}が不正の場合nullを返す。
     * @throws NullPointerException {@code sourceType}がnull
     * @throws IllegalArgumentException {@code listIndex}が1以上でない
     */
    @Nullable
    ListInfo.ListItem getListItem(@NonNull MediaSourceType sourceType, @IntRange(from = 1) int listIndex);

    /**
     * 取得したいUSBリストのアイテムインデックス追加.
     * <p>
     * 追加されたインデックスのリストアイテムを取得する
     * 既に取得されている場合は何もしない
     *
     * @param index インデックス
     */
    void addWantedUsbListItemIndex(int index);

    /**
     * 取得したいUSBリストのアイテムインデックス削除.
     * <p>
     * 取得待ちしているインデックスを削除する
     * 取得待ちに存在しない場合は何もしない
     *
     * @param index インデックス
     */
    void removeWantedUsbListItemIndex(int index);
}
