package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.SystemSettingUpdater;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * システムの設定.
 */
public class PreferSystem {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject SystemSettingUpdater mUpdater;

    /**
     * コンストラクタ.
     */
    @Inject
    public PreferSystem(){
    }

    /**
     * BEEP TONE設定.
     * <p>
     * BEEP TONE設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:BEEP TONE設定有効 {@code false}:BEEP TONE設定無効
     */
    public void setBeepTone(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().beepToneSettingEnabled) {
                Timber.w("setBeepTone() beep tone setting disabled.");
                return;
            }

            mUpdater.setBeepTone(enabled);
        });
    }

    /**
     * ATT/MUTEトグル処理.
     * <p>
     * ATT/MUTE設定が無効な場合何もしない
     */
    public void toggleAttMute(){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().attMuteSettingEnabled) {
                Timber.w("setAttMute() att mute setting disabled.");
                return;
            }

            mUpdater.setAttMute(mStatusHolder.getSystemSetting().attMuteSetting.toggle());
        });
    }

    /**
     * DistanceUnitトグル処理.
     * <p>
     * DistanceUnit設定が無効な場合何もしない
     */
    public void toggleDistanceUnit(){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().distanceUnitSettingEnabled) {
                Timber.w("setDistanceUnit() distance unit setting disabled.");
                return;
            }

            mUpdater.setDistanceUnit(mStatusHolder.getSystemSetting().distanceUnit.toggle());
        });
    }

    /**
     * DEMO設定.
     * <p>
     * DEMO設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:DEMO設定有効 {@code false}:DEMO設定無効
     */
    public void setDemo(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().demoSettingEnabled) {
                Timber.w("setDemo() demo setting disabled.");
                return;
            }

            mUpdater.setDemo(enabled);
        });
    }

    /**
     * POWER SAVE設定.
     * <p>
     * POWER SAVE設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:POWER SAVE設定有効 {@code false}:POWER SAVE設定無効
     */
    public void setPowerSave(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().powerSaveSettingEnabled) {
                Timber.w("setPowerSave() power save setting disabled.");
                return;
            }

            mUpdater.setPowerSave(enabled);
        });
    }

    /**
     * BT Audio設定.
     * <p>
     * BT Audio設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:BT Audio設定有効 {@code false}:BT Audio設定無効
     */
    public void setBtAudio(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().btAudioSettingEnabled) {
                Timber.w("setBtAudio() bt audio setting disabled.");
                return;
            }

            mUpdater.setBtAudio(enabled);
        });
    }

    /**
     * Pandora設定.
     * <p>
     * Pandora設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:Pandora設定有効 {@code false}:Pandora設定無効
     */
    public void setPandora(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().pandoraSettingEnabled) {
                Timber.w("setPandora() pandora setting disabled.");
                return;
            }

            mUpdater.setPandora(enabled);
        });
    }

    /**
     * Spotify設定.
     * <p>
     * Spotify設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:Spotify設定有効 {@code false}:Spotify設定無効
     */
    public void setSpotify(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().spotifySettingEnabled) {
                Timber.w("setSpotify() spotify setting disabled.");
                return;
            }

            mUpdater.setSpotify(enabled);
        });
    }

    /**
     * AUX設定.
     * <p>
     * AUX設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:AUX設定有効 {@code false}:AUX設定無効
     */
    public void setAux(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().auxSettingEnabled) {
                Timber.w("setAux() aux setting disabled.");
                return;
            }

            mUpdater.setAux(enabled);
        });
    }

    /**
     * 99App自動起動設定.
     * <p>
     * 99App自動起動設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:99App自動起動設定有効 {@code false}:99App自動起動設定無効
     */
    public void setAppAutoStart(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().appAutoStartSettingEnabled) {
                Timber.w("setAppAutoStart() 99app auto start setting disabled.");
                return;
            }

            mUpdater.setAppAutoStart(enabled);
        });
    }

    /**
     * USB AUTO設定.
     * <p>
     * USB AUTO設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:USB AUTO設定有効 {@code false}:USB AUTO設定無効
     */
    public void setUsbAuto(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().usbAutoSettingEnabled) {
                Timber.w("setUsbAuto() usb auto setting disabled.");
                return;
            }

            mUpdater.setUsbAuto(enabled);
        });
    }

    /**
     * DISP OFF設定.
     * <p>
     * DISP OFF設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:DISP OFF設定有効 {@code false}:DISP OFF設定無効
     */
    public void setDisplayOff(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().displayOffSettingEnabled) {
                Timber.w("setDisplayOff() display off setting disabled.");
                return;
            }

            mUpdater.setDisplayOff(enabled);
        });
    }

    /**
     * ステアリングリモコン設定.
     * <p>
     * ステアリングリモコン設定が無効な場合何もしない
     *
     * @param type ステアリングリモコン設定種別
     * @throws NullPointerException {@code type}がnull
     */
    public void setSteeringRemoteControl(@NonNull SteeringRemoteControlSettingType type){
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().steeringRemoteControlSettingEnabled) {
                Timber.w("setSteeringRemoteControl() steering remote control setting disabled.");
                return;
            }

            mUpdater.setSteeringRemoteControl(type);
        });
    }

    /**
     * AUTO PI設定.
     * <p>
     * AUTO PI設定が無効な場合何もしない
     *
     * @param enabled 有効か否か {@code true}:AUTO PI設定有効 {@code false}:AUTO PI設定無効
     */
    public void setAutoPi(boolean enabled){
        mHandler.post(() -> {
            if (!mStatusHolder.getSystemSettingStatus().autoPiSettingEnabled) {
                Timber.w("setAutoPi() auto pi setting disabled.");
                return;
            }

            mUpdater.setAutoPi(enabled);
        });
    }
}
