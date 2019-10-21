package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.NaviGuideVoiceSettingUpdater;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ナビガイド音声設定.
 */
public class PreferNaviGuideVoice {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject NaviGuideVoiceSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferNaviGuideVoice(){
    }

    /**
     * ナビガイド音声設定.
     * <p>
     * ナビガイド音声設定が無効な場合は何もしない
     *
     * @param enabled 有効か否か {@code true}:ナビガイド音声設定有効 {@code false}:ナビガイド音声設定無効
     */
    public void setNaviGuideVoice(boolean enabled) {
        mHandler.post(() -> {
            if (!mStatusHolder.getCarDeviceStatus().naviGuideVoiceSettingEnabled) {
                Timber.w("setNaviGuideVoice() navi guide voice setting disabled.");
                return;
            }
            mUpdater.setNaviGuideVoice(enabled);
        });
    }

    /**
     * ナビガイド音声ボリューム設定.
     * <p>
     * ナビガイド音声設定が無効な場合は何もしない
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    public void setNaviGuideVoiceVolume(@NonNull NaviGuideVoiceVolumeSetting setting){
        checkNotNull(setting);

        mHandler.post(() -> {
            if(!mStatusHolder.getCarDeviceStatus().naviGuideVoiceSettingEnabled){
                Timber.w("setNaviGuideVoiceVolume() navi guide voice setting disabled.");
                return;
            }
            mUpdater.setNaviGuideVoiceVolume(setting);
        });
    }
}
