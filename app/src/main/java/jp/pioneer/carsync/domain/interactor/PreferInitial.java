package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.InitialSettingUpdater;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 初期設定.
 */
public class PreferInitial {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject InitialSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferInitial() {

    }

    /**
     * FM STEPトグル処理.
     * <p>
     * FM STEP設定が無効な場合何もしない
     */
    public void toggleFmStep() {
        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().fmStepSettingEnabled) {
                Timber.w("setFmStep() fm step setting disabled.");
                return;
            }

            mUpdater.setFmStep(mStatusHolder.getInitialSetting().fmStep.toggle());
        });
    }

    /**
     * AM STEPトグル処理.
     * <p>
     * AM STEP設定が無効な場合何もしない
     */
    public void toggleAmStep() {
        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().amStepSettingEnabled) {
                Timber.w("setAmStep() am step setting disabled.");
                return;
            }

            mUpdater.setAmStep(mStatusHolder.getInitialSetting().amStep.toggle());
        });
    }

    /**
     * REAR出力/PREOUT出力設定
     * <p>
     * REAR出力/PREOUT出力設定が無効な場合何もしない
     */
    public void toggleRearOutputPreoutOutput() {
        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().rearOutputPreoutOutputSettingEnabled) {
                Timber.w("setRearOutputPreoutOutput() rear output preout output setting disabled.");
                return;
            }

            mUpdater.setRearOutputPreoutOutput(mStatusHolder.getInitialSetting().rearOutputPreoutOutputSetting.toggle());
        });
    }

    /**
     * REAR出力トグル処理.
     * <p>
     * REAR出力設定が無効な場合何もしない
     */
    public void toggleRearOutput() {
        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().rearOutputSettingEnabled) {
                Timber.w("setRearOutput() rear output setting disabled.");
                return;
            }

            mUpdater.setRearOutput(mStatusHolder.getInitialSetting().rearOutputSetting.toggle());
        });
    }

    /**
     * MENU表示言語設定.
     * <p>
     * MENU表示言語設定が無効な場合何もしない
     *
     * @param type 設定種別
     * @throws NullPointerException {@code type}がnull
     */
    public void setMenuDisplayLanguage(@NonNull MenuDisplayLanguageType type) {
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().menuDisplayLanguageSettingEnabled) {
                Timber.w("setMenuDisplayLanguage() menu display language setting disabled.");
                return;
            }

            mUpdater.setMenuDisplayLanguage(type);
        });
    }

    /**
     * DAB ANT PW設定.
     * <p>
     * DAB ANT PW設定が無効な場合何もしない
     *
     * @param isOn 設定
     */
    public void setDabAntennaPowerEnabled(boolean isOn) {
        mHandler.post(() -> {
            if (!mStatusHolder.getInitialSettingStatus().dabAntennaPowerEnabled) {
                Timber.w("setDabAntennaPower() dab antenna power setting disabled.");
                return;
            }

            mUpdater.setDabAntennaPower(isOn);
        });
    }
}
