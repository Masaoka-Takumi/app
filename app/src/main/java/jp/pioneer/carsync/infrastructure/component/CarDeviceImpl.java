package jp.pioneer.carsync.infrastructure.component;

import android.os.Handler;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SourceController;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerTypeChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.EqualizerSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.ReadingRequestType;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSendTaskFinishedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.task.SetCustomFlashPatternTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.component.BroadcastReceiverImpl.ACTION_USB_ACCESSORY_PERMISSION;

/**
 * CarDeviceの実装.
 */
public class CarDeviceImpl implements CarDevice, AppSharedPreference.OnAppSharedPreferenceChangeListener {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject @ForInfrastructure ExecutorService mTaskExecutor;
    @Inject Map<MediaSourceType, SourceController> mSourceControllers;
    @Inject NullSourceController mNullSourceController;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject Provider<SetCustomFlashPatternTask> mSetCustomFlashPatternTaskProvider;
    private MediaSourceType mCurrentSourceType;
    private AdasWarningStatus mAdasWarningStatus;
    private Future<?> mTaskFuture;

    /**
     * コンストラクタ.
     */
    @Inject
    public CarDeviceImpl() {
    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        mCarDeviceConnection.initialize(ACTION_USB_ACCESSORY_PERMISSION, BroadcastReceiverImpl.class);
        mCurrentSourceType = mStatusHolder.getCarDeviceStatus().sourceType;
        mHandler.post(() -> getSourceController().active());
        mPreference.registerOnAppSharedPreferenceChangeListener(this);
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SourceController getSourceController(@NonNull MediaSourceType sourceType) {
        Timber.i("getSourceController() sourceType = " + sourceType);
        checkNotNull(sourceType);

        SourceController controller = mSourceControllers.get(sourceType);
        return (controller == null) ? mNullSourceController : controller;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectSource(@NonNull MediaSourceType sourceType) {
        Timber.i("selectSource() sourceType = " + sourceType);
        checkNotNull(sourceType);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSourceSwitchCommand(sourceType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeNextSource() {
        Timber.i("changeNextSource()");
        mCarDeviceConnection.sendPacket(mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.SOURCE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeScreen(@NonNull CarDeviceScreen screen, @NonNull TransitionDirection direction) {
        Timber.i("changeScreen() screen = %s, direction = %s", screen, direction);
        checkNotNull(screen);
        checkNotNull(direction);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createScreenChangeCommand(screen, direction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestReadNotification(@NonNull ReadingRequestType type) {
        Timber.i("requestReadNotification() type = %s", type);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createReadingCommand(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestCustomFlash(@NonNull CustomFlashRequestType type) {
        Timber.i("requestCustomFlash() type = %s", type);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createCustomFlashCommand(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void impactDetectionCountdown(int timerSecond) {
        Timber.i("impactDetectionCountdown() timerSecond = %d", timerSecond);

        mCarDeviceConnection.sendPacketDirect(mPacketBuilder.createImpactDetectionCommand(timerSecond));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void phoneCall(@NonNull String number) {
        Timber.i("phoneCall() number = %s", number);
        checkNotNull(number);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createPhoneCallCommand(number));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMenu() {
        Timber.i("exitMenu()");

        mCarDeviceConnection.sendPacket(mPacketBuilder.createExitMenuCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAdasWarningStatus(@NonNull Set<AdasWarningEvent> warningEvents){
        Timber.i("updateAdasWarningStatus() warningEvents = %s", warningEvents);
        checkNotNull(warningEvents);

        SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
        smartPhoneStatus.adasWarningEvents = warningEvents;
        if (smartPhoneStatus.getAdasWarningStatus() != mAdasWarningStatus) {
            mAdasWarningStatus = smartPhoneStatus.getAdasWarningStatus();

            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneStatusNotification(version, smartPhoneStatus));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAppSharedPreferenceChanged(@NonNull AppSharedPreference preferences, @NonNull String key) {
        Timber.i("onAppSharedPreferenceChanged() preferences = %s, key = %s", preferences, key);

        switch (key) {
            case AppSharedPreference.KEY_THEME_TYPE:
                if (mStatusHolder.getProtocolSpec().isSphCarDevice() &&
                        mStatusHolder.getIlluminationSettingStatus().customFlashPatternSettingEnabled) {
                    // テーマ変更のため、CUSTOM発光パターン更新
                    startCustomFlashPatternTask();
                }
                break;
            case AppSharedPreference.KEY_LIGHTING_EFFECT_ENABLED:
                if (mStatusHolder.getProtocolSpec().isSphCarDevice()) {
                    if (preferences.isLightingEffectEnabled()) {
                        requestCustomFlash(CustomFlashRequestType.START);
                    } else {
                        requestCustomFlash(CustomFlashRequestType.FINISH);
                    }
                }
                break;
            case AppSharedPreference.KEY_ADAS_ALARM_ENABLED:
                onAdasWarningUpdateEvent(null);
                break;
            case AppSharedPreference.KEY_DEBUG_SPECIAL_EQ_ENABLED:
                if(!preferences.isDebugSpecialEqEnabled()){
                    SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
                    if(soundFxSetting.soundFxSettingEqualizerType.code >= (1 << 8)) {
                        mCarDeviceConnection.sendPacket(mPacketBuilder.createEqualizerPresetInitializationNotification());
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * StatusHolder更新イベントハンドラ.
     *
     * @param ev StatusHolder更新イベント
     */
    @Subscribe
    public synchronized void onCrpStatusUpdateEvent(CrpStatusUpdateEvent ev) {
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (status.sourceType != mCurrentSourceType) {
            onSourceChanged(status.sourceType);
        }
    }

    /**
     * ADAS警告状態更新イベントハンドラ.
     * <p>
     * 新たに取得した警報状態と保持している警報状態が異なる場合に車載機へ通知を送る。
     *
     * @param ev ADAS警告状態更新イベント
     */
    @Subscribe
    public synchronized void onAdasWarningUpdateEvent(AdasWarningUpdateEvent ev) {
		//警告ダイアログ表示と同時に警告音を鳴らすためここでは何もしない
/*        SmartPhoneStatus status = mStatusHolder.getSmartPhoneStatus();
        if (status.getAdasWarningStatus() != mAdasWarningStatus) {
            mAdasWarningStatus = status.getAdasWarningStatus();

            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneStatusNotification(version, status));
        }*/
    }

    /**
     * ReadNotificationPostedEventハンドラ
     * <p>
     * 新規通知を受信した場合に動作する。
     *
     * @param ev ReadNotificationPostedEvent
     */
    @Subscribe
    public synchronized void onReadNotificationPostedEvent(ReadNotificationPostedEvent ev) {
        mCarDeviceConnection.sendPacket(mPacketBuilder.createNewMessageCommand());
    }

    /**
     * SmartPhoneControlCommandEventハンドラ
     * <p>
     * スマートフォン操作コマンドを受信した場合に動作する。
     *
     * @param ev SmartPhoneControlCommandEvent
     */
    @Subscribe
    public synchronized void onSmartPhoneControlCommandEvent(SmartPhoneControlCommandEvent ev) {
        AppStatus appStatus = mStatusHolder.getAppStatus();
        if(mStatusHolder.getAppStatus().isAlexaAvailableCountry && mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA
                && ev.command == SmartPhoneControlCommand.VR && !mStatusHolder.getProtocolSpec().isSphCarDevice()) {
            return;
        }
        if(appStatus.isTransitionedHomeScreen && appStatus.isAgreedCaution) {
            ShortcutKey key;
            switch (ev.command) {
                case MUSIC:
                    key = ShortcutKey.SOURCE;
                    break;
                case NAVI:
                    key = ShortcutKey.NAVI;
                    break;
                case PHONE:
                    key = ShortcutKey.PHONE;
                    break;
                case MAIL:
                    key = ShortcutKey.MESSAGE;
                    break;
                case VR:
                    key = ShortcutKey.VOICE;
                    break;
                case APP:
                    key = ShortcutKey.APP;
                    break;
                default:
                    return;
            }

            mEventBus.post(new ShortcutKeyEvent(key));
        }
    }

    /**
     * SendTask終了イベントハンドラ..
     *
     * @param ev SendTask終了イベント
     */
    @Subscribe
    public synchronized void onCrpSendTaskFinishedEvent(CrpSendTaskFinishedEvent ev) {
        switch(ev.id){
            case ILLUMINATION_SETTINGS_REQUEST:
                if((mStatusHolder.getIlluminationSetting().requestStatus == RequestStatus.SENT_COMPLETE ||
                        mStatusHolder.getIlluminationSetting().requestStatus == RequestStatus.SENT_INCOMPLETE) &&
                        mStatusHolder.shouldSendCustomFlashPatternRequests()){
                    startCustomFlashPatternTask();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Equalizer種別変更イベントハンドラ.
     *
     * @param ev Equalizer種別変更イベント
     */
    @Subscribe
    public void onEqualizerTypeChangeEvent(EqualizerTypeChangeEvent ev) {
        EqualizerSetting setting = mStatusHolder.getAudioSetting().equalizerSetting;

        SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();
        SoundFxSettingEqualizerType currentType;
        if(setting.audioSettingEqualizerType == AudioSettingEqualizerType.SPECIAL){
            if(mPreference.isDebugSpecialEqEnabled()) {
                currentType = SoundFxSettingEqualizerType.valueOfSpecialEq(setting.specialEqType);
            } else {
                currentType = SoundFxSettingEqualizerType.UNKNOWN;
            }
        } else {
            currentType = SoundFxSettingEqualizerType.valueOfEq((byte) setting.audioSettingEqualizerType.code);
        }

        if(currentType == SoundFxSettingEqualizerType.UNKNOWN){
            mCarDeviceConnection.sendPacket(mPacketBuilder.createEqualizerPresetInitializationNotification());
        } else if(soundFxSetting.soundFxSettingEqualizerType != currentType ||
                soundFxSetting.soundFxSettingEqualizerType.isCustomEq()){
            soundFxSetting.soundFxSettingEqualizerType = currentType;
            mEventBus.post(new EqualizerSettingChangeEvent());
        }

    }

    /**
     * AppMusic再生状態変更イベント.
     *
     * @param ev AppMusicPlaybackModeChangeEvent
     */
    @Subscribe
    public void onAppMusicPlaybackModeChangeEvent(AppMusicPlaybackModeChangeEvent ev) {
        if(mStatusHolder.getAppStatus().isFormatRead &&
                mStatusHolder.getSmartPhoneStatus().playbackMode == PlaybackMode.PLAY){
            mStatusHolder.getAppStatus().isFormatRead = false;
        }
    }

    private void onSourceChanged(MediaSourceType newSourceType) {
        Timber.i("onSourceChanged()");

        getSourceController().inactive();
        mCurrentSourceType = newSourceType;
        getSourceController().active();

        if(newSourceType != MediaSourceType.APP_MUSIC){
            mStatusHolder.getAppStatus().isFormatRead =
                    mStatusHolder.getSmartPhoneStatus().playbackMode != PlaybackMode.PLAY;
        }
    }

    private SourceControllerImpl getSourceController() {
        return (SourceControllerImpl) getSourceController(mCurrentSourceType);
    }

    private void startCustomFlashPatternTask() {
        stopTask();
        mTaskFuture = mTaskExecutor.submit(mSetCustomFlashPatternTaskProvider.get()
                .setParamsForCustomFlashPattern(mPreference.getThemeType().getFlashPattern()));
    }

    private void stopTask() {
        if (mTaskFuture != null && !mTaskFuture.isDone()) {
            mTaskFuture.cancel(true);
            mTaskFuture = null;
        }
    }
}
