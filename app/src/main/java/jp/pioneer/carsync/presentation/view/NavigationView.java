package jp.pioneer.carsync.presentation.view;

import android.content.pm.ApplicationInfo;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;

/**
 * Navigation設定画面の抽象クラス
 */
public interface NavigationView {

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    void setAdapter(ArrayList<String> types, boolean isMarin);

    /**
     * アプリケーション一覧設定.
     *
     * @param apps アプリケーション一覧
     * @param selectedApp 選択されているアプリケーション
     */
    void setApplicationList(List<ApplicationInfo> apps,
                            @Nullable ApplicationInfo selectedApp);
    /**
     * アプリケーション一覧設定(Marin).
     *
     * @param weatherApps weatherアプリケーション一覧
     * @param boatingApps boatingアプリケーション一覧
     * @param fishingApps fishingアプリケーション一覧
     * @param naviApps naviアプリケーション一覧
     * @param selectedApp 選択されているアプリケーション
     */
    void setApplicationList( List<ApplicationInfo> weatherApps, List<ApplicationInfo> boatingApps, List<ApplicationInfo> fishingApps, List<ApplicationInfo> naviApps,
                            @Nullable ApplicationInfo selectedApp);
    /**
     * Navi Guidance設定.
     *
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setMixingSetting(boolean isEnabled,
                          boolean setting);

    /**
     * Navi Guidance Volume設定.
     *
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setMixingVolumeSetting(boolean isEnabled,
                                @Nullable NaviGuideVoiceVolumeSetting setting);
}
