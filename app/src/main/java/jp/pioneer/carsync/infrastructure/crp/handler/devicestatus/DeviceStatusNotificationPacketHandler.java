package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.task.AudioSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.FunctionSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.IlluminationSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.InitialSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.NaviGuideVoiceSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.ParkingSensorSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.PhoneSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import jp.pioneer.carsync.infrastructure.crp.task.SoundFxSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SystemSettingsRequestTask;
import timber.log.Timber;

/**
 * 車載機ステータス情報通知パケットハンドラ.
 */
public class DeviceStatusNotificationPacketHandler extends AbstractPacketHandler {
    private DeviceStatusPacketProcessor mPacketProcessor;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceStatusNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = session.getStatusHolder();
        mPacketProcessor = new DeviceStatusPacketProcessor(mStatusHolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        if (!Objects.equal(mPacketProcessor.process(packet), Boolean.TRUE)) {
            throw new BadPacketException("Bad packet.");
        }

        if (!mPacketProcessor.isUpdated()) {
            sendSettingRequestsIfNecessary();
            return null;
        }

        Timber.d("doHandle() Updated.");
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (status.sourceStatus == MediaSourceStatus.CHANGING) {
            // ソース切替中になったら昔のデータが表示されないようにステータスをリセット
            refreshMediaInfo(status.sourceType);
        }

        sendSettingRequestsIfNecessary();
        getSession().publishStatusUpdateEvent(packet.getPacketIdType());
        return null;
    }

    private void refreshMediaInfo(MediaSourceType sourceType) {
        CarDeviceMediaInfoHolder infoHolder = mStatusHolder.getCarDeviceMediaInfoHolder();
        switch (sourceType) {
            case RADIO:
                infoHolder.radioInfo.reset();
                break;
            case DAB:
                infoHolder.dabInfo.reset();
                break;
            case HD_RADIO:
                infoHolder.hdRadioInfo.reset();
                break;
            case SIRIUS_XM:
                infoHolder.sxmMediaInfo.reset();
                break;
            case CD:
                infoHolder.cdInfo.reset();
                break;
            case PANDORA:
                infoHolder.pandoraMediaInfo.reset();
                break;
            case USB:
                infoHolder.usbMediaInfo.reset();
                break;
            case SPOTIFY:
                infoHolder.spotifyMediaInfo.reset();
                break;
            default:
                // nothing to do
        }
    }

    private void sendSettingRequestsIfNecessary() {
        if (mStatusHolder.shouldSendSystemSettingRequests()) {
            getSession().executeSendTask(createSystemSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendAudioSettingRequests()) {
            getSession().executeSendTask(createAudioSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendIlluminationSettingRequests()) {
            getSession().executeSendTask(createIlluminationSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendFunctionSettingRequests()) {
            getSession().executeSendTask(createFunctionSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendParkingSensorSettingRequests()) {
            getSession().executeSendTask(createParkingSensorSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendNaviGuideVoiceSettingRequests()) {
            getSession().executeSendTask(createNaviGuideVoiceSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendInitialSettingRequests()) {
            getSession().executeSendTask(createInitialSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendPhoneSettingRequests()) {
            getSession().executeSendTask(createPhoneSettingsRequestTask());
        }

        if (mStatusHolder.shouldSendSoundFxSettingRequests()) {
            getSession().executeSendTask(createSoundFxSettingsRequestTask());
        }
    }

    private SendTask createSystemSettingsRequestTask() {
        SystemSettingsRequestTask task = new SystemSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createAudioSettingsRequestTask() {
        AudioSettingsRequestTask task = new AudioSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createIlluminationSettingsRequestTask() {
        IlluminationSettingsRequestTask task = new IlluminationSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createFunctionSettingsRequestTask() {
        FunctionSettingsRequestTask task = new FunctionSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createParkingSensorSettingsRequestTask() {
        ParkingSensorSettingsRequestTask task = new ParkingSensorSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createNaviGuideVoiceSettingsRequestTask() {
        NaviGuideVoiceSettingsRequestTask task = new NaviGuideVoiceSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createInitialSettingsRequestTask() {
        InitialSettingsRequestTask task = new InitialSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createPhoneSettingsRequestTask() {
        PhoneSettingsRequestTask task = new PhoneSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }

    private SendTask createSoundFxSettingsRequestTask() {
        SoundFxSettingsRequestTask task = new SoundFxSettingsRequestTask();
        return task.inject(getSession().getSessionComponent());
    }
}
