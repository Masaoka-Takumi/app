package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.AudioOutputMode;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSetting.*;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.HpfLpfFilterType;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * オーディオ設定情報要求タスク.
 */
public class AudioSettingsRequestTask extends SendTask {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;

    /**
     * コンストラクタ.
     */
    public AudioSettingsRequestTask() {
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
        return SendTaskId.AUDIO_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        if (!mStatusHolder.shouldSendAudioSettingRequests()) {
            Timber.d("Can not send audio setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        CarDeviceStatus carDeviceStatus = mStatusHolder.getCarDeviceStatus();
        AudioSettingStatus audioSettingStatus = mStatusHolder.getAudioSettingStatus();
        AudioSetting audioSetting = mStatusHolder.getAudioSetting();

        boolean isFirst = audioSetting.requestStatus == RequestStatus.NOT_SENT;
        audioSetting.requestStatus = RequestStatus.SENDING;

        // オーディオ設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createAudioSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // EQ設定情報要求
        if (audioSettingStatus.equalizerSettingEnabled) {
            if (carDeviceStatus.audioSettingEnabled) {
                request(packetBuilder.createEqualizerSettingInfoRequest(), audioSetting, Tag.EQUALIZER);
            } else if (carDeviceStatus.jasperAudioSettingEnabled) {
                request(packetBuilder.createJasperEqualizerSettingInfoRequest(), audioSetting, Tag.EQUALIZER);
            } else if (carDeviceStatus.ac2AudioSettingEnabled) {
                CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
                if (spec.carModelSpecializedSetting.audioSettingSupported) {
                    request(packetBuilder.createAc2EqualizerSettingInfoRequest(), audioSetting, Tag.EQUALIZER);
                } else {
                    request(packetBuilder.createEqualizerSettingInfoRequest(), audioSetting, Tag.EQUALIZER);
                }
            } else {
                Timber.w("doTask() Unexpected audio menu status.");
            }
        }
        // FADER/BALANCE設定情報要求
        if (audioSettingStatus.faderSettingEnabled || audioSettingStatus.balanceSettingEnabled) {
            request(packetBuilder.createFaderBalanceSettingInfoRequest(), audioSetting, Tag.FADER_BALANCE);
        }
        // SUBWOOFER設定情報要求
        if (audioSettingStatus.subwooferSettingEnabled) {
            request(packetBuilder.createSubwooferSettingInfoRequest(), audioSetting, Tag.SUBWOOFER);
        }
        // SUBWOOFER位相設定情報要求
        if (audioSettingStatus.subwooferPhaseSettingEnabled) {
            request(packetBuilder.createSubwooferPhaseSettingInfoRequest(), audioSetting, Tag.SUBWOOFER_PHASE);
        }
        // SPEAKER LEVEL情報要求
        if (audioSettingStatus.speakerLevelSettingEnabled) {
            request(packetBuilder.createSpeakerLevelSettingInfoRequest(), audioSetting, Tag.SPEAKER_LEVEL);
        }
        // CROSSOVER設定情報要求
        if (audioSettingStatus.crossoverSettingEnabled) {
            if (carDeviceStatus.audioSettingEnabled || carDeviceStatus.ac2AudioSettingEnabled) {
                AudioOutputMode audioOutputMode = mStatusHolder.getCarDeviceSpec().audioSettingSpec.audioOutputMode;
                for (SpeakerType speakerType : SpeakerType.values()) {
                    if (speakerType.mode == audioOutputMode) {
                        request(packetBuilder.createCrossoverSettingRequest(speakerType), audioSetting, Tag.CROSSOVER);
                    }
                }
            } else if (carDeviceStatus.jasperAudioSettingEnabled) {
                for (HpfLpfFilterType lpfFilterType : HpfLpfFilterType.values()) {
                    request(packetBuilder.createJasperCrossoverSettingRequest(lpfFilterType), audioSetting, Tag.CROSSOVER);
                }
            } else {
                Timber.w("doTask() Unexpected audio menu status.");
            }
        }
        // LISTENING POSITION設定情報要求
        if (mStatusHolder.isListeningPositionSettingRequestEnabled()) {
            request(packetBuilder.createListeningPositionSettingInfoRequest(), audioSetting, Tag.LISTENING_POSITION);
        }
        // TIME ALIGNMENT設定情報要求
        if (audioSettingStatus.timeAlignmentSettingEnabled) {
            request(packetBuilder.createTimeAlignmentSettingInfoRequest(), audioSetting, Tag.TIME_ALIGNMENT);
        }
        // Auto EQ(AEQ)補正設定情報要求
        if (audioSettingStatus.aeqSettingEnabled) {
            request(packetBuilder.createAutoEqCorrectionSettingInfoRequest(), audioSetting, Tag.AUTO_EQ_CORRECTION);
        }
        // BASS BOOSTERレベル設定情報要求
        if (audioSettingStatus.bassBoosterSettingEnabled) {
            request(packetBuilder.createBassBoosterLevelSettingInfoRequest(), audioSetting, Tag.BASS_BOOSTER);
        }
        // LOUDNESS設定情報要求
        if (audioSettingStatus.loudnessSettingEnabled) {
            request(packetBuilder.createLoudnessSettingInfoRequest(), audioSetting, Tag.LOUDNESS);
        }
        // ALC設定情報要求
        if (audioSettingStatus.alcSettingEnabled) {
            request(packetBuilder.createAlcSettingInfoRequest(), audioSetting, Tag.ALC);
        }
        // SLA設定情報要求
        if (audioSettingStatus.slaSettingEnabled) {
            request(packetBuilder.createSlaSettingInfoRequest(), audioSetting, Tag.SLA);
        }
        // [AC2] Beat Blasterレベル設定情報要求
        if (audioSettingStatus.beatBlasterSettingEnabled) {
            request(packetBuilder.createBeatBlasterLevelSettingInfoRequest(), audioSetting, Tag.BEAT_BLASTER);
        }
        // [AC2] LEVEL設定情報要求
        if (audioSettingStatus.levelSettingEnabled) {
            request(packetBuilder.createLevelSettingInfoRequest(), audioSetting, Tag.LEVEL);
        }
        // SOUND RETRIEVER設定情報要求
        if (audioSettingStatus.soundRetrieverSettingEnabled) {
            request(packetBuilder.createSoundRetrieverSettingInfoRequest(), audioSetting, Tag.SOUND_RETRIEVER);
        }

        // 設定取得中にLOAD SETTINGを行った場合は再度取得しにいくのでNOT_SENTだったら更新しない
        if (audioSetting.requestStatus == RequestStatus.SENDING) {
            if(audioSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                audioSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                audioSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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
        CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
        AudioSettingSpec spec = mStatusHolder.getCarDeviceSpec().audioSettingSpec;
        AudioSetting setting = mStatusHolder.getAudioSetting();
        if(!spec.listeningPositionSettingSupported && !carDeviceSpec.carModelSpecializedSetting.audioSettingSupported) setting.acquiredSetting(Tag.LISTENING_POSITION);
        if(!spec.crossoverSettingSupported) setting.acquiredSetting(Tag.CROSSOVER);
        if(!spec.speakerLevelSettingSupported) setting.acquiredSetting(Tag.SPEAKER_LEVEL);
        if(!spec.subwooferPhaseSettingSupported) setting.acquiredSetting(Tag.SUBWOOFER_PHASE);
        if(!spec.subwooferSettingSupported) setting.acquiredSetting(Tag.SUBWOOFER);
        if(!spec.balanceSettingSupported && !spec.faderSettingSupported) setting.acquiredSetting(Tag.FADER_BALANCE);
        if(!spec.equalizerSettingSupported) setting.acquiredSetting(Tag.EQUALIZER);
        if(!spec.slaSettingSupported) setting.acquiredSetting(Tag.SLA);
        if(!spec.alcSettingSupported) setting.acquiredSetting(Tag.ALC);
        if(!spec.loudnessSettingSupported) setting.acquiredSetting(Tag.LOUDNESS);
        if(!spec.bassBoosterSettingSupported) setting.acquiredSetting(Tag.BASS_BOOSTER);
        if(!spec.aeqSettingSupported) setting.acquiredSetting(Tag.AUTO_EQ_CORRECTION);
        if(!spec.timeAlignmentSettingSupported) setting.acquiredSetting(Tag.TIME_ALIGNMENT);
        if(!spec.levelSettingSupported) setting.acquiredSetting(Tag.LEVEL);
        if(!spec.beatBlasterSettingSupported) setting.acquiredSetting(Tag.BEAT_BLASTER);
        if(!spec.soundRetrieverSettingSupported) setting.acquiredSetting(Tag.SOUND_RETRIEVER);
    }

    private void request(OutgoingPacket packet, AudioSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
