package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting.*;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingSpec;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * パーキングセンサー設定情報要求タスク.
 */
public class ParkingSensorSettingsRequestTask extends SendTask {
    @Inject
    @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public ParkingSensorSettingsRequestTask() {
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
        return SendTaskId.PARKING_SENSOR_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendParkingSensorSettingRequests()) {
            Timber.d("Can not send parking sensor setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        ParkingSensorSettingStatus parkingSensorSettingStatus = mStatusHolder.getParkingSensorSettingStatus();
        ParkingSensorSetting parkingSensorSetting = mStatusHolder.getParkingSensorSetting();

        boolean isFirst = parkingSensorSetting.requestStatus == RequestStatus.NOT_SENT;
        parkingSensorSetting.requestStatus = RequestStatus.SENDING;

        // パーキングセンサー設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createParkingSensorSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // パーキングセンサー設定情報要求
        // ステータスに存在しないため、パーキングセンサー設定自体がEnableであるかを判断して要求を実行する。
        // shouldSendParkingSensorSettingRequests()がtrueの場合はEnableのため、ここでは判定をせずに要求を実行する。
        request(packetBuilder.createParkingSensorSettingInfoRequest(), parkingSensorSetting, Tag.PARKING_SENSOR);
        // 警告音出力先設定情報要求
        if (parkingSensorSettingStatus.alarmOutputDestinationSettingEnabled) {
            request(packetBuilder.createAlarmOutputDestinationSettingInfoRequest(), parkingSensorSetting, Tag.ALARM_OUTPUT_DESTINATION);
        }
        // 警告音量設定情報要求
        if (parkingSensorSettingStatus.alarmVolumeSettingEnabled) {
            request(packetBuilder.createAlarmVolumeSettingInfoRequest(), parkingSensorSetting, Tag.ALARM_VOLUME);
        }
        // バック信号極性設定情報要求
        if (parkingSensorSettingStatus.backPolaritySettingEnabled) {
            request(packetBuilder.createBackPolaritySettingInfoRequest(), parkingSensorSetting, Tag.BACK_POLARITY);
        }

        if (parkingSensorSetting.requestStatus == RequestStatus.SENDING) {
            if(parkingSensorSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                parkingSensorSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                parkingSensorSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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
        ParkingSensorSettingSpec spec = mStatusHolder.getCarDeviceSpec().parkingSensorSettingSpec;
        ParkingSensorSetting setting = mStatusHolder.getParkingSensorSetting();
        if(!spec.alarmOutputDestinationSettingSupported) setting.acquiredSetting(Tag.ALARM_OUTPUT_DESTINATION);
        if(!spec.alarmVolumeSettingSupported) setting.acquiredSetting(Tag.ALARM_VOLUME);
        if(!spec.backPolaritySettingSupported) setting.acquiredSetting(Tag.BACK_POLARITY);
    }

    private void request(OutgoingPacket packet, ParkingSensorSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
