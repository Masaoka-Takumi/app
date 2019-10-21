package jp.pioneer.carsync.presentation.view;

import android.content.pm.ApplicationInfo;
import android.util.SparseBooleanArray;

import java.util.List;

/**
 * Created by NSW00_007906 on 2017/10/20.
 */

public interface SourceAppSettingView {

    /**
     * インストールされている対象アプリケーションの一覧を設定する。
     * 対象アプリケーションについては、{@link jp.pioneer.carsync.domain.model.MusicApp}を参照
     *
     * @param musicApps アプリケーション一覧
     */
    void setInstalledMusicApps(List<ApplicationInfo> musicApps);

    /**
     * 選択中アプリケーションを設定する。
     *
     * @param positions 有効なアプリケーションの位置
     */
    void setCheckedItemPositions(SparseBooleanArray positions);

    /**
     * アプリケーション一覧の取得
     *
     * @return SparseBooleanArray 有効なアプリケーションの位置
     */
    SparseBooleanArray getCheckedItemPositions();

    void setPass(String pass);
}
