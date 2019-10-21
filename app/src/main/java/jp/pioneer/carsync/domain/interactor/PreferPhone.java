package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.PhoneSettingUpdater;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

/**
 * Phone設定.
 */
public class PreferPhone {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject PhoneSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferPhone() {

    }

    /**
     * AUTO ANSWER設定.
     * <p>
     * AUTO ANSWER設定が無効な場合は何もしない
     *
     * @param enabled 有効か否か {@code true}:AUTO ANSWER設定有効 {@code false}:AUTO ANSWER設定無効
     */
    public void setAutoAnswer(boolean enabled) {
        mHandler.post(() -> {
            if (!mStatusHolder.getPhoneSettingStatus().autoAnswerSettingEnabled) {
                Timber.w("setAutoAnswer() auto answer setting disabled.");
                return;
            }

            mUpdater.setAutoAnswer(enabled);
        });
    }

    /**
     * AUTO PAIRING設定.
     * <p>
     * AUTO PAIRING設定が無効な場合は何もしない
     *
     * @param enabled 有効か否か {@code true}:AUTO PAIRING設定有効 {@code false}:AUTO PAIRING設定無効
     */
    public void setAutoPairing(boolean enabled) {
        mHandler.post(() -> {
            if (!mStatusHolder.getPhoneSettingStatus().autoPairingSettingEnabled) {
                Timber.w("setAutoPairing() auto pairing setting disabled.");
                return;
            }

            mUpdater.setAutoPairing(enabled);
        });
    }
}
