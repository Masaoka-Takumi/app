package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;

/**
 * Created by NSW00_008320 on 2018/03/06.
 */

public interface MenuDisplayLanguageDialogView {

    /**
     * MENU表示言語設定.
     *
     * @param setting 設定内容
     */
    void setMenuDisplayLanguageSetting(MenuDisplayLanguageType setting);


    /**
     * ダイアログクローズ.
     */
    void callbackClose();
}
