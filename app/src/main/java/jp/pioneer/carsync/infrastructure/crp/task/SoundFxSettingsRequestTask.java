package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSetting.*;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Sound FX設定情報要求タスク.
 */
public class SoundFxSettingsRequestTask extends SendTask {
    @Inject
    @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public SoundFxSettingsRequestTask() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("taskId", getSendTaskId())
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendTask inject(@NonNull CarRemoteSessionComponent component) {
        checkNotNull(component).inject(this);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SendTaskId getSendTaskId() {
        return SendTaskId.SOUND_FX_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendSoundFxSettingRequests()) {
            Timber.d("Can not send sound fx setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        SoundFxSettingStatus soundFxSettingStatus = mStatusHolder.getSoundFxSettingStatus();
        SoundFxSetting soundFxSetting = mStatusHolder.getSoundFxSetting();

        boolean isFirst = soundFxSetting.requestStatus == RequestStatus.NOT_SENT;
        soundFxSetting.requestStatus = RequestStatus.SENDING;

        // Sound FX設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createSoundFxSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // Super轟設定情報要求
        if (soundFxSettingStatus.superTodorokiSettingEnabled) {
            request(packetBuilder.createSuperTodorokiSettingInfoRequest(), soundFxSetting, Tag.SUPER_TODOROKI);
        }
        // Small Car TA設定情報要求
        if (soundFxSettingStatus.smallCarTaSettingEnabled) {
            request(packetBuilder.createSmallCarTaSettingInfoRequest(), soundFxSetting, Tag.SMALL_CAR_TA);
        }
        // ライブシミュレーション設定情報要求
        if (soundFxSettingStatus.liveSimulationSettingEnabled) {
            request(packetBuilder.createLiveSimulationSettingInfoRequest(), soundFxSetting, Tag.LIVE_SIMULATION);
        }
        // カラオケ設定系情報要求
        if (soundFxSettingStatus.karaokeSettingEnabled) {
            // カラオケ設定情報要求
            request(packetBuilder.createKaraokeSettingInfoRequest(), soundFxSetting, Tag.KARAOKE);
            // マイク音量設定情報要求
            request(packetBuilder.createMicVolumeSettingInfoRequest(), soundFxSetting, Tag.MIC_VOLUME);
            // Vocal Cancel設定情報要求
            request(packetBuilder.createVocalCancelSettingInfoRequest(), soundFxSetting, Tag.VOCAL_CANCEL);
        }

        if (soundFxSetting.requestStatus == RequestStatus.SENDING) {
            if(soundFxSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                soundFxSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                soundFxSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        mHandlerFactory.create(packet.getPacketIdType()).handle(packet);
    }

    private void setSettingTagIfUnsupported(){
        SoundFxSettingSpec spec = mStatusHolder.getCarDeviceSpec().soundFxSettingSpec;
        SoundFxSetting setting = mStatusHolder.getSoundFxSetting();
        if(!spec.karaokeSettingSupported) setting.acquiredSetting(Tag.KARAOKE);
        if(!spec.karaokeSettingSupported) setting.acquiredSetting(Tag.VOCAL_CANCEL);
        if(!spec.karaokeSettingSupported) setting.acquiredSetting(Tag.MIC_VOLUME);
        if(!spec.liveSimulationSettingSupported) setting.acquiredSetting(Tag.LIVE_SIMULATION);
        if(!spec.smallCarTaSettingSupported) setting.acquiredSetting(Tag.SMALL_CAR_TA);
        if(!spec.superTodorokiSettingSupported) setting.acquiredSetting(Tag.SUPER_TODOROKI);
    }

    private void request(OutgoingPacket packet, SoundFxSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
