package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.RadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Radio Function設定.
 */
public class PreferRadioFunction {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject RadioFunctionSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferRadioFunction() {
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
            if (!mStatusHolder.getTunerFunctionSettingStatus().localSettingEnabled) {
                Timber.w("setLocal() Disabled.");
                return;
            }

            mUpdater.setLocal(setting);
        });
    }

    /**
     * FM TUNER設定.
     * <p>
     * FM TUNERを設定する。
     * FM TUNER設定が無効な場合、何もしない。
     */
    public void toggleFmTuner() {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().fmSettingEnabled) {
                Timber.w("toggleFmTuner() Disabled.");
                return;
            }

            mUpdater.setFmTuner(mStatusHolder.getTunerFunctionSetting().fmTunerSetting.toggle());
        });
    }

    /**
     * REG広域設定.
     * <p>
     * REG広域を設定する。
     * REG広域設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setReg(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().regSettingEnabled) {
                Timber.w("setReg() Disabled.");
                return;
            }

            mUpdater.setReg(setting);
        });
    }

    /**
     * TA設定.
     * <p>
     * TAを設定する。
     * TA設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setTa(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().taSettingEnabled) {
                Timber.w("setTa() Disabled.");
                return;
            }

            mUpdater.setTa(setting);
        });
    }

    /**
     * AF設定.
     * <p>
     * AFを設定する。
     * AF設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setAf(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().afSettingEnabled) {
                Timber.w("setAf() Disabled.");
                return;
            }

            mUpdater.setAf(setting);
        });
    }

    /**
     * NEWS設定.
     * <p>
     * NEWSを設定する。
     * NEWS設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setNews(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().newsSettingEnabled) {
                Timber.w("setNews() Disabled.");
                return;
            }

            mUpdater.setNews(setting);
        });
    }

    /**
     * ALARM設定.
     * <p>
     * ALARMを設定する。
     * ALARM設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     */
    public void setAlarm(boolean setting) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().alarmSettingEnabled) {
                Timber.w("setAlarm() Disabled.");
                return;
            }

            mUpdater.setAlarm(setting);
        });
    }

    /**
     * P.CH/MANUAL設定.
     * <p>
     * P.CH/MANUALを設定する。
     * P.CH/MANUAL設定が無効な場合、何もしない。
     */
    public void togglePchManual() {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().pchManualEnabled) {
                Timber.w("togglePchManual() Disabled.");
                return;
            }

            mUpdater.setPchManual(mStatusHolder.getTunerFunctionSetting().pchManualSetting.toggle());
        });
    }

}
