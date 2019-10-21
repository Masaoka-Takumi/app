package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;

/**
 * メディアリスト制御者.
 */
public interface MediaListController {
    /**
     * リスト入場.
     *
     * @param listType 遷移するリスト種別
     * @throws NullPointerException {@code listType}がnull
     */
    void enterList(@NonNull ListType listType);

    /**
     * リスト退場.
     */
    void exitList();

    /**
     * 選択リスト情報通知.
     *
     * @param hasParent 上方向の階層を持つか否か
     * @param hasChild 下方向の階層を持つか否か
     * @param currentPosition リスト階層数
     * @param subDisplayInfo サブディスプレイに表示する現在のカテゴリ情報
     * @param text 車載機側の画面に表示する文字列
     * @throws NullPointerException {@code subDisplayInfo}、または、{@code text}がnull
     */
    void notifySelectedListInfo(boolean hasParent, boolean hasChild, int currentPosition,
                                @NonNull SubDisplayInfo subDisplayInfo, @NonNull String text);

    /**
     * リスト項目選択.
     *
     * @param listItem リスト項目
     * @throws NullPointerException {@code listItem}がnull
     */
    void selectListItem(@NonNull ListInfo.ListItem listItem);

    /**
     * 戻る.
     */
    void goBack();
}

