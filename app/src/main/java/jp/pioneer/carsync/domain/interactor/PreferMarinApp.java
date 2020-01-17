package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.MarinApp;
import jp.pioneer.carsync.domain.model.MarinAppCategory;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static com.google.common.base.Preconditions.checkNotNull;

public class PreferMarinApp {
    private static final String[] PACKAGE_NAMES;
    private static final String[] WEATHER_PACKAGE_NAMES;
    private static final String[] BOATING_PACKAGE_NAMES;
    private static final String[] FISHING_PACKAGE_NAMES;

    static {
        List<String> packageNames = new ArrayList<>();
        List<String> weatherPackageNames = new ArrayList<>();
        List<String> boatingPackageNames = new ArrayList<>();
        List<String> fishingPackageNames = new ArrayList<>();
        for (MarinApp marinApp : MarinApp.values()) {
            packageNames.add(marinApp.getPackageName());
            if(marinApp.getCategory()== MarinAppCategory.WEATHER){
                weatherPackageNames.add(marinApp.getPackageName());
            }
            if(marinApp.getCategory()== MarinAppCategory.BOATING){
                boatingPackageNames.add(marinApp.getPackageName());
            }
            if(marinApp.getCategory()== MarinAppCategory.FISHING){
                fishingPackageNames.add(marinApp.getPackageName());
            }
        }
        //Marin用App一覧には車載器用NaviAppも含める
        for (NaviApp naviApp : NaviApp.values()) {
            packageNames.add(naviApp.getPackageName());
        }
        PACKAGE_NAMES = packageNames.toArray(new String[0]);
        WEATHER_PACKAGE_NAMES = weatherPackageNames.toArray(new String[0]);
        BOATING_PACKAGE_NAMES = boatingPackageNames.toArray(new String[0]);
        FISHING_PACKAGE_NAMES = fishingPackageNames.toArray(new String[0]);
    }

    @Inject ApplicationInfoRepository mRepository;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferMarinApp() {
    }

    /**
     * ナビアプリ情報一覧取得.
     * <P>
     * 本アプリと連携対象となっているナビアプリのうち、インストール
     * されているアプリの一覧を取得する。
     * 連携対象となるのは、{@link NaviApp}と{@link MarinApp}で定義されたアプリである。
     *
     * @return インストールされているナビアプリに関する {@link ApplicationInfo} のリスト
     */
    @NonNull
    public List<ApplicationInfo> getInstalledTargetAppList() {
        return mRepository.get(PACKAGE_NAMES);
    }

    /**
     * ナビアプリ情報一覧取得（WEATHER）.
     * <P>
     * 本アプリと連携対象となっているナビアプリのうち、インストール
     * されているアプリの一覧を取得する。
     * 連携対象となるのは、{@link MarinApp}で定義されたアプリである。
     *
     * @return インストールされているナビアプリに関する {@link ApplicationInfo} のリスト
     */
    @NonNull
    public List<ApplicationInfo> getInstalledWeatherTargetAppList() {
        return mRepository.get(WEATHER_PACKAGE_NAMES);
    }

    /**
     * ナビアプリ情報一覧取得（BOATING）.
     * <P>
     * 本アプリと連携対象となっているナビアプリのうち、インストール
     * されているアプリの一覧を取得する。
     * 連携対象となるのは、{@link MarinApp}で定義されたアプリである。
     *
     * @return インストールされているナビアプリに関する {@link ApplicationInfo} のリスト
     */
    @NonNull
    public List<ApplicationInfo> getInstalledBoatingTargetAppList() {
        return mRepository.get(BOATING_PACKAGE_NAMES);
    }

    /**
     * ナビアプリ情報一覧取得（FISHING）.
     * <P>
     * 本アプリと連携対象となっているナビアプリのうち、インストール
     * されているアプリの一覧を取得する。
     * 連携対象となるのは、{@link MarinApp}で定義されたアプリである。
     *
     * @return インストールされているナビアプリに関する {@link ApplicationInfo} のリスト
     */
    @NonNull
    public List<ApplicationInfo> getInstalledFishingTargetAppList() {
        return mRepository.get(FISHING_PACKAGE_NAMES);
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
        return mPreference.getNavigationMarinApp();
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
        mPreference.setNavigationMarinApp(app);
    }
}
