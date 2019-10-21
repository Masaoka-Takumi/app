package jp.pioneer.carsync.presentation.view;

import android.content.Intent;

import java.util.ArrayList;

import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;

/**
 * 電話帳 発着信履歴リストの抽象クラス
 */

public interface ContactsHistoryView {

    /**
     * 発着信履歴表示
     * @param list 発着信履歴のリスト
     */
    void setHistoryList(ArrayList<ContactsHistoryItem> list);

    /**
     * 連絡先に発信
     * @param intent インテント
     */
    void dial(Intent intent);
}
