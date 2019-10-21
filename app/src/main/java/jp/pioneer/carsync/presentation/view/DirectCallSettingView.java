package jp.pioneer.carsync.presentation.view;

import android.net.Uri;

/**
 * Phone機能設定画面の抽象クラス
 */

public interface DirectCallSettingView {

    /**
     * 連絡先項目設定
     * @param name 連絡先名
     * @param photoUri 連絡先アイコン
     */
    void setContactItem(String name, Uri photoUri);

    /**
     * 番号項目設定
     * @param number 連絡先番号
     * @param type 連絡先タイプ
     */
    void setPhoneItem(String number, int type);

    /**
     * 無効設定
     */
    void setDisable();

    /**
     * 登録ボタン有効無効設定
     * @param isEnabled 有効無効
     */
    void setRegisterEnabled(boolean isEnabled);
}
