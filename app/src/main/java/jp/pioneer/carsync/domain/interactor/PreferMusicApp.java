package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 音楽アプリケーション設定.
 */
public class PreferMusicApp {
    private static final String[] TARGET_APP_PACKAGE_NAMES;

    static {
        List<String> packageNames = new ArrayList<>();
        for (MusicApp musicApp : MusicApp.values()) {
            packageNames.add(musicApp.getPackageName());
        }

        TARGET_APP_PACKAGE_NAMES = packageNames.toArray(new String[0]);
    }

    @Inject ApplicationInfoRepository mRepository;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferMusicApp() {
    }

    /**
     * インストールされている対象アプリ一覧取得.
     *
     * @return List<ApplicationInfo>
     */
    public List<ApplicationInfo> getInstalledTargetAppList() {
        return mRepository.get(TARGET_APP_PACKAGE_NAMES);
    }

    /**
     * 選択されたアプリ一覧取得.
     * <p>
     * 対象アプリのうち、ユーザーが選んだアプリをソース選択画面表示の対象とする。
     *
     * @return AppSharedPreference.Application[]
     */
    @NonNull
    public AppSharedPreference.Application[] getSelectedAppList() {
        return mPreference.getMusicApps();
    }

    /**
     * 選択されたアプリ一覧設定.
     * <p>
     * 対象アプリかどうかの判定は行わない。
     *
     * @param appList AppSharedPreference.Application[]
     */
    public void setSelectedAppList(@NonNull AppSharedPreference.Application[] appList) {
        checkNotNull(appList);
        mPreference.setMusicApps(appList);
    }
}
