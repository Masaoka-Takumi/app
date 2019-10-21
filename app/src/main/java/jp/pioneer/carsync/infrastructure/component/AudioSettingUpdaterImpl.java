package jp.pioneer.carsync.infrastructure.component;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.google.common.base.Objects;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoadSettingsType;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.entity.LoadSettingResponse;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.interactor.PreferAudio.*;

/**
 * AudioSettingUpdaterの実装.
 */
public class AudioSettingUpdaterImpl implements AudioSettingUpdater {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject Handler mHandler;
    @Inject EventBus mEventBus;
    private WeakReference<LoadSaveCallback> mCallback;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;

    private LoadSettingCallback mLoadSettingCallback = new LoadSettingCallback();
    class LoadSettingCallback implements RequestTask.Callback<LoadSettingResponse>{

        @Override
        public void onResult(LoadSettingResponse result) {
            mIsFailed = Objects.equal(result.result, ResponseCode.NG) ||
                    result.type != LoadSettingsType.SOUND;
            mCountDownLatch.countDown();
        }

        @Override
        public void onError() {
            mIsFailed = true;
            mCountDownLatch.countDown();
        }
    }

    private SaveSettingCallback mSaveSettingCallback = new SaveSettingCallback();
    class SaveSettingCallback implements RequestTask.Callback<ResponseCode>{

        @Override
        public void onResult(ResponseCode result) {
            mIsFailed = Objects.equal(result, ResponseCode.NG);
            mCountDownLatch.countDown();
        }

        @Override
        public void onError() {
            mIsFailed = true;
            mCountDownLatch.countDown();
        }
    }

    /**
     * コンストラクタ.
     */
    @Inject
    public AudioSettingUpdaterImpl(){

    }

    /**
     * 初期化
     */
    public void initialize() {
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeatBlaster(@NonNull BeatBlasterSetting setting) {
        Timber.i("setBeatBlaster() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createBeatBlasterSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoudness(@NonNull LoudnessSetting setting) {
        Timber.i("setLoudness() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createLoudnessSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceLevelAdjuster(int step) {
        Timber.i("setSourceLevelAdjuster() step = %d", step);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSlaSettingNotification(step));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFaderBalance(int fader, int balance) {
        Timber.i("setFaderBalance() fader = %d, balance = %d", fader, balance);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createFaderBalanceSettingNotification(fader, balance));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListeningPosition(@NonNull ListeningPositionSetting setting) {
        Timber.i("setListeningPosition() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createListeningPositionSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAlignmentMode(@NonNull TimeAlignmentSettingMode mode) {
        Timber.i("setTimeAlignmentMode() mode = %s", mode);
        checkNotNull(mode);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createTimeAlignmentPresetSettingNotification(mode));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAlignment(@NonNull MixedSpeakerType type, int step) {
        Timber.i("setTimeAlignment() type = %s, step = %d", type, step);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createTimeAlignmentSettingNotification(type, step));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeakerLevel(@NonNull MixedSpeakerType type, int level) {
        Timber.i("setSpeakerLevel() type = %s, level = %d", type, level);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSpeakerLevelSettingNotification(type, level));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCrossoverHpfLpf(@NonNull SpeakerType type, boolean isOn) {
        Timber.i("setCrossoverHpfLpf() type = %s, isOn = %s", type, isOn);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createCrossoverHpfLpfSettingNotification(type, isOn));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCrossoverCutOff(@NonNull SpeakerType type, @NonNull CutoffSetting setting) {
        Timber.i("setCrossoverCutOff() type = %s, setting = %s", type, setting);
        checkNotNull(type);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createCrossoverCutoffSettingNotification(type, setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCrossoverSlope(@NonNull SpeakerType type, @NonNull SlopeSetting setting) {
        Timber.i("setCrossoverSlope() type = %s, setting = %s", type, setting);
        checkNotNull(type);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createCrossoverSlopeSettingNotification(type, setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubWoofer(@NonNull SubwooferSetting setting) {
        Timber.i("setSubWoofer() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSubwooferSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubWooferPhase(@NonNull SubwooferPhaseSetting setting) {
        Timber.i("setSubWooferPhase() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSubwooferPhaseSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSoundRetriever(@NonNull SoundRetrieverSetting setting) {
        Timber.i("setSoundRetriever() setting = %s", setting);
        checkNotNull(setting);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSoundRetrieverSettingNotification(setting));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAudioSetting(@NonNull LoadSaveCallback callback) {
        Timber.i("saveAudioSetting()");

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("saveAudioSetting() multiple access.");
            return;
        }

        mCallback = new WeakReference<>(callback);

        try {
            if (save()) {
                callbackSuccess();
            } else {
                callbackError();
            }
        } catch (InterruptedException e) {
            Timber.d("request() Interrupted.");
            callbackError();
        }
    }

    private boolean save() throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createSaveSettingNotification();

        mCountDownLatch = new CountDownLatch(1);
        if (mCarDeviceConnection.sendRequestPacket(packet, mSaveSettingCallback) == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAudioSetting(@NonNull LoadSaveCallback callback) {
        Timber.i("loadAudioSetting()");

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("finishRequest() multiple access.");
            return;
        }

        mCallback = new WeakReference<>(callback);
        try {
            if (load()) {
                callbackSuccess();
            } else {
                callbackError();
            }
        } catch (InterruptedException e) {
            Timber.d("request() Interrupted.");
            callbackError();
        }
    }

    private boolean load() throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createLoadSettingNotification(LoadSettingsType.SOUND);

        mCountDownLatch = new CountDownLatch(1);
        if (mCarDeviceConnection.sendRequestPacket(packet, mLoadSettingCallback) == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCustomBand(@NonNull CustomEqType type, @NonNull @Size(13) int[] bands) {
        Timber.i("setCustomBand() type = %s, bands = %s", type, bands);
        checkNotNull(type);
        checkArgument(checkNotNull(bands).length == 13);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createEqualizerCustomAdjustNotification(type, bands));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEqualizer(@NonNull AudioSettingEqualizerType type) {
        Timber.i("setEqualizer() type = %s", type);
        checkNotNull(type);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createEqualizerSettingNotification(type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpecialEqualizer(int typeCode, @NonNull @Size(13) int[] bands) {
        Timber.i("setSpecialEqualizer() type = %d, bands = %s", typeCode, bands);
        checkArgument(checkNotNull(bands).length == 13);

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSpecialEqualizerSettingNotification(typeCode, bands));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initPresetEq() {
        mCarDeviceConnection.sendPacket(mPacketBuilder.createEqualizerPresetInitializationNotification());
    }

    private synchronized void callbackSuccess() {
        LoadSaveCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        mHandler.post(callback::onSuccess);
    }

    private synchronized void callbackError() {
        LoadSaveCallback callback = getCallback();
        if (callback == null) {
            return;
        }

        mHandler.post(callback::onError);
    }

    @Nullable
    private LoadSaveCallback getCallback() {
        if (mCallback == null) {
            Timber.w("getCallback() callback has been cleared.");
            return null;
        }

        LoadSaveCallback callback = mCallback.get();
        mCallback = null;
        if (callback == null) {
            Timber.w("getCallback() callback has been cleared.");
            return null;
        }
        return callback;
    }

    /**
     * セッション停止イベントハンドラ
     * <p>
     * セッション停止した場合はCountDownを強制的に終了し、
     * 失敗したこととする。
     *
     * @param ev セッションストップイベント
     */
    @Subscribe
    public void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        if(mCallback != null) {
            mIsFailed = true;
            mCountDownLatch.countDown();
        }
    }
}
