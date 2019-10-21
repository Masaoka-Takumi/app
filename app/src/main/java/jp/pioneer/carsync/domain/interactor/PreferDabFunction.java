package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.DabFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TASetting;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * DAB Function設定.
 */
public class PreferDabFunction {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject DabFunctionSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferDabFunction() {
    }

    /**
     * TA設定.
     * <p>
     * TAを設定する。
     * TA設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setTa(@NonNull TASetting setting) {
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getDabFunctionSettingStatus().taSettingEnabled) {
                Timber.w("setTa() Disabled.");
                return;
            }

            mUpdater.setTa(setting);
        });
    }

    /**
     * SERVICE FOLLOW設定.
     * <p>
     * SERVICE FOLLOWを設定する。
     * SERVICE FOLLOW設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setServiceFollow(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getDabFunctionSettingStatus().serviceFollowSettingEnabled) {
                Timber.w("setServiceFollow() Disabled.");
                return;
            }

            mUpdater.setServiceFollow(setting);
        });
    }

    /**
     * SOFT LINK設定.
     * <p>
     * SOFT LINKを設定する。
     * SOFT LINK設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setSoftLink(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getDabFunctionSettingStatus().softlinkSettingEnabled) {
                Timber.w("setSoftLink() Disabled.");
                return;
            }

            mUpdater.setSoftLink(setting);
        });
    }
}
