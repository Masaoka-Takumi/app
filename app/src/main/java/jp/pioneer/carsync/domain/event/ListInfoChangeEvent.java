package jp.pioneer.carsync.domain.event;

import jp.pioneer.carsync.domain.model.ListInfo;

/**
 * リスト情報変更イベント.
 * <p>
 * リスト情報:
 *  <pre>{@code
 *      ListInfo info = statusHolder.getListInfo();
 *  }</pre>
 *
 *  トランザクション情報（{@link ListInfo#transactionInfo}）は本変更イベントの
 *  対象外である。（そもそもinfrastructure層でしか使用しない）
 */
public class ListInfoChangeEvent {
}
