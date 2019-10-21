package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SystemSetting;
import jp.pioneer.carsync.domain.model.SystemSetting.*;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.SystemSettingStatus;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * システム設定情報要求タスク.
 */
public class SystemSettingsRequestTask extends SendTask {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public SystemSettingsRequestTask() {
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
        return SendTaskId.SYSTEM_SETTING_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendSystemSettingRequests()) {
            Timber.d("Can not send system setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        SystemSettingStatus systemSettingStatus = mStatusHolder.getSystemSettingStatus();
        SystemSetting systemSetting = mStatusHolder.getSystemSetting();

        boolean isFirst = systemSetting.requestStatus == RequestStatus.NOT_SENT;
        systemSetting.requestStatus = RequestStatus.SENDING;

        // システム設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createSystemSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // BEEP TONE設定情報要求
        if (systemSettingStatus.beepToneSettingEnabled) {
            request(packetBuilder.createBeepToneSettingInfoRequest(), systemSetting, Tag.BEEP_TONE);
        }
        // ATT/MUTE設定情報要求
        if (systemSettingStatus.attMuteSettingEnabled) {
            request(packetBuilder.createAttMuteSettingInfoRequest(), systemSetting, Tag.ATT_MUTE);
        }
        // DEMO設定情報要求
        if (systemSettingStatus.demoSettingEnabled) {
            request(packetBuilder.createDemoSettingInfoRequest(), systemSetting, Tag.DEMO);
        }
        // POWER SAVE設定情報要求
        if (systemSettingStatus.powerSaveSettingEnabled) {
            request(packetBuilder.createPowerSaveSettingInfoRequest(), systemSetting, Tag.POWER_SAVE);
        }
        // BT Audio設定情報要求
        if (systemSettingStatus.btAudioSettingEnabled) {
            request(packetBuilder.createBtAudioSettingInfoRequest(), systemSetting, Tag.BT_AUDIO);
        }
        // Pandora設定情報要求
        if (systemSettingStatus.pandoraSettingEnabled) {
            request(packetBuilder.createPandoraSettingInfoRequest(), systemSetting, Tag.PANDORA);
        }
        // Spotify設定情報要求
        if (systemSettingStatus.spotifySettingEnabled) {
            request(packetBuilder.createSpotifySettingInfoRequest(), systemSetting, Tag.SPOTIFY);
        }
        // AUX設定情報要求
        if (systemSettingStatus.auxSettingEnabled) {
            request(packetBuilder.createAuxSettingInfoRequest(), systemSetting, Tag.AUX);
        }
        // 99APP自動起動設定情報要求
        if (systemSettingStatus.appAutoStartSettingEnabled) {
            request(packetBuilder.createAppAutoStartSettingInfoRequest(), systemSetting, Tag.APP_AUTO_START);
        }
        // USB AUTO設定情報要求
        if (systemSettingStatus.usbAutoSettingEnabled) {
            request(packetBuilder.createUsbAutoSettingInfoRequest(), systemSetting, Tag.USB_AUTO);
        }
        // ステアリングリモコン設定情報要求
        if (systemSettingStatus.steeringRemoteControlSettingEnabled) {
            request(packetBuilder.createSteeringRemoteControlSettingInfoRequest(), systemSetting, Tag.STEERING_REMOTE_CONTROL);
        }
        // AUTO PI設定情報要求
        if (systemSettingStatus.autoPiSettingEnabled) {
            request(packetBuilder.createAutoPiSettingInfoRequest(), systemSetting, Tag.AUTO_PI);
        }
        // DISP OFF設定情報要求
        if (systemSettingStatus.displayOffSettingEnabled) {
            request(packetBuilder.createDisplayOffSettingInfoRequest(), systemSetting, Tag.DISPLAY_OFF);
        }
        // 距離単位設定情報要求
        if (systemSettingStatus.distanceUnitSettingEnabled) {
            request(packetBuilder.createDistanceUnitSettingInfoRequest(), systemSetting, Tag.DISTANCE_UNIT);
        }
        if (systemSetting.requestStatus == RequestStatus.SENDING) {
            if(systemSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                systemSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                systemSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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
        SystemSettingSpec spec = mStatusHolder.getCarDeviceSpec().systemSettingSpec;
        SystemSetting setting = mStatusHolder.getSystemSetting();
        if(!spec.auxSettingSupported) setting.acquiredSetting(Tag.AUX);
        if(!spec.spotifySettingSupported) setting.acquiredSetting(Tag.SPOTIFY);
        if(!spec.pandoraSettingSupported) setting.acquiredSetting(Tag.PANDORA);
        if(!spec.btAudioSettingSupported) setting.acquiredSetting(Tag.BT_AUDIO);
        if(!spec.powerSaveSettingSupported) setting.acquiredSetting(Tag.POWER_SAVE);
        setting.acquiredSetting(Tag.DEMO); // Demo設定のみ、対応設定で設定不可状態が維持されるため
        if(!spec.attMuteSettingSupported) setting.acquiredSetting(Tag.ATT_MUTE);
        if(!spec.beepToneSettingSupported) setting.acquiredSetting(Tag.BEEP_TONE);
        if(!spec.displayOffSettingSupported) setting.acquiredSetting(Tag.DISPLAY_OFF);
        if(!spec.distanceUnitSettingSupported) setting.acquiredSetting(Tag.DISTANCE_UNIT);
        if(!spec.autoPiSettingSupported) setting.acquiredSetting(Tag.AUTO_PI);
        if(!spec.steeringRemoteControlSettingSupported) setting.acquiredSetting(Tag.STEERING_REMOTE_CONTROL);
        if(!spec.usbAutoSettingSupported) setting.acquiredSetting(Tag.USB_AUTO);
        if(!spec.appAutoStartSettingSupported) setting.acquiredSetting(Tag.APP_AUTO_START);
    }

    private void request(OutgoingPacket packet, SystemSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
