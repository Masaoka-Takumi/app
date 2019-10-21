package jp.pioneer.carsync.domain.interactor;

import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知読み上げ設定.
 */
public class PreferReadNotification {
    private static final String[] TARGET_APP_PACKAGE_NAMES;

    static {
        Stream<String> stream = Stream.of(MessagingApp.values())
                .map(MessagingApp::getPackageName);

        TARGET_APP_PACKAGE_NAMES = stream.toArray(String[]::new);
    }

    @Inject ApplicationInfoRepository mRepository;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferReadNotification() {
    }

    /**
     * 通知読み上げが有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     */
    public boolean isEnabled() {
        return mPreference.isReadNotificationEnabled();
    }

    /**
     * 通知読み上げ有効設定.
     *
     * @param enabled {@code true}:有効。｛@code false}:無効。
     */
    public void setEnabled(boolean enabled) {
        mPreference.setReadNotificationEnabled(enabled);
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
     * 対象アプリのうち、ユーザーが選んだアプリを通知読み上げの対象とする。
     *
     * @return AppSharedPreference.Application[]
     */
    @NonNull
    public AppSharedPreference.Application[] getSelectedAppList() {
        return mPreference.getReadNotificationApps();
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
        mPreference.setReadNotificationApps(appList);
    }
}
