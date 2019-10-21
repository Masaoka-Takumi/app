package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.HdRadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * HD Radio Function設定.
 */
public class PreferHdRadioFunction {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject HdRadioFunctionSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferHdRadioFunction() {
    }

    /**
     * LOCAL設定.
     * <p>
     * LOCALを設定する。
     * LOCAL設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    public void setLocal(@NonNull LocalSetting setting) {
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getHdRadioFunctionSettingStatus().localSettingEnabled) {
                Timber.w("setLocal() Disabled.");
                return;
            }

            mUpdater.setLocal(setting);
        });
    }

    /**
     * SEEK設定.
     * <p>
     * SEEKを設定する。
     * SEEK設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setSeek(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getHdRadioFunctionSettingStatus().hdSeekSettingEnabled) {
                Timber.w("setSeek() Disabled.");
                return;
            }

            mUpdater.setSeek(setting);
        });
    }

    /**
     * BLENDING設定.
     * <p>
     * BLENDINGを設定する。
     * BLENDING設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setBlending(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getHdRadioFunctionSettingStatus().blendingSettingEnabled) {
                Timber.w("setBlending() Disabled.");
                return;
            }

            mUpdater.setBlending(setting);
        });
    }

    /**
     * ACTIVE RADIO設定.
     * <p>
     * ACTIVE RADIOを設定する。
     * ACTIVE RADIO設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setActiveRadio(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getHdRadioFunctionSettingStatus().activeRadioSettingEnabled) {
                Timber.w("setActiveRadio() Disabled.");
                return;
            }

            mUpdater.setActiveRadio(setting);
        });
    }
}
