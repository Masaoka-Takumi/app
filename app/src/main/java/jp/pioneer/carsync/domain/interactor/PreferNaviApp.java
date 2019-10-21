package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ナビアプリケーション設定
 */
public class PreferNaviApp {
    private static final String[] PACKAGE_NAMES;

    static {
        List<String> packageNames = new ArrayList<>();
        for (NaviApp naviApp : NaviApp.values()) {
            packageNames.add(naviApp.getPackageName());
        }

        PACKAGE_NAMES = packageNames.toArray(new String[0]);
    }

    @Inject ApplicationInfoRepository mRepository;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferNaviApp() {
    }

    /**
     * ナビアプリ情報一覧取得.
     * <P>
     * 本アプリと連携対象となっているナビアプリのうち、インストール
     * されているアプリの一覧を取得する。
     * 連携対象となるのは、{@link NaviApp}で定義されたアプリである。
     *
     * @return インストールされているナビアプリに関する {@link ApplicationInfo} のリスト
     */
    @NonNull
    public List<ApplicationInfo> getInstalledTargetAppList() {
        return mRepository.get(PACKAGE_NAMES);
    }

    /**
     * 選択されたナビアプリ取得.
     * <p>
     * 対象アプリのうち、ユーザーが選んだアプリをナビゲーションアプリの対象とする。
     *
     * @return AppSharedPreference.Application
     */
    @NonNull
    public AppSharedPreference.Application getSelectedApp() {
            return mPreference.getNavigationApp();
    }

    /**
     * 選択されたナビアプリ設定.
     * <p>
     * 対象アプリかどうかの判定は行わない。
     *
     * @param app AppSharedPreference.Application
     */
    public void setSelectedApp(@NonNull AppSharedPreference.Application app) {
        checkNotNull(app);
        mPreference.setNavigationApp(app);
    }
}
