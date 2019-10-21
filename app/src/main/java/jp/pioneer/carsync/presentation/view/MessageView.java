package jp.pioneer.carsync.presentation.view;

import android.content.pm.ApplicationInfo;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;

/**
 * Message設定画面の抽象クラス
 */

public interface MessageView {

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<String> types);

    /**
     * アプリケーション一覧設定.
     *
     * @param apps        アプリケーション一覧
     * @param selectedApp 選択されているアプリケーション
     */
    void setApplicationList(List<ApplicationInfo> apps,
                            List<ApplicationInfo> selectedApp);

    /**
     * メッセージ読み上げ設定.
     *
     * @param setting 設定内容
     */
    void setMessageReading(boolean setting);

    /**
     * メッセージカラー設定.
     *
     * @param isEnabled 設定可能か否か
     * @param setting   設定内容
     */
    void setMessageColor(boolean isEnabled,
                         @Nullable IncomingMessageColorSetting setting);
}
