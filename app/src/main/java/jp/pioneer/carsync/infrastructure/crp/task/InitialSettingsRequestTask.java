package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.InitialSetting;
import jp.pioneer.carsync.domain.model.InitialSetting.*;
import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.InitialSettingStatus;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 初期設定情報要求タスク.
 */
public class InitialSettingsRequestTask extends SendTask {
    @Inject
    @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public InitialSettingsRequestTask() {
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
        return SendTaskId.INITIAL_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendInitialSettingRequests()) {
            Timber.d("Can not send initial setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        InitialSettingStatus initialSettingStatus = mStatusHolder.getInitialSettingStatus();
        InitialSetting initialSetting = mStatusHolder.getInitialSetting();

        boolean isFirst = initialSetting.requestStatus == RequestStatus.NOT_SENT;
        initialSetting.requestStatus = RequestStatus.SENDING;

        // 初期設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createInitialSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // FM STEP設定情報要求
        if (initialSettingStatus.fmStepSettingEnabled) {
            request(packetBuilder.createFmStepSettingInfoRequest(), initialSetting, Tag.FM_STEP);
        }
        // AM STEP設定情報要求
        if (initialSettingStatus.amStepSettingEnabled) {
            request(packetBuilder.createAmStepSettingInfoRequest(), initialSetting, Tag.AM_STEP);
        }
        // REAR出力設定/PREOUT出力設定情報要求
        if (initialSettingStatus.rearOutputPreoutOutputSettingEnabled) {
            request(packetBuilder.createRearOutputPreoutOutputSettingInfoRequest(), initialSetting, Tag.REAR_OUTPUT_PREOUT_OUTPUT);
        }
        // REAR出力設定情報要求
        if (initialSettingStatus.rearOutputSettingEnabled) {
            request(packetBuilder.createRearOutputSettingInfoRequest(), initialSetting, Tag.REAR_OUTPUT);
        }
        // MENU表示言語設定情報要求
        if (initialSettingStatus.menuDisplayLanguageSettingEnabled) {
            request(packetBuilder.createMenuDisplayLanguageSettingInfoRequest(), initialSetting, Tag.MENU_DISPLAY_LANGUAGE);
        }
        // DAB ANT PW設定情報要求
        if (initialSettingStatus.dabAntennaPowerEnabled) {
            request(packetBuilder.createDabAntennaPowerSettingInfoRequest(), initialSetting, Tag.DAB_ANTENNA_POWER);
        }
        if (initialSetting.requestStatus == RequestStatus.SENDING) {
            if(initialSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                initialSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                initialSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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
        InitialSettingSpec spec = mStatusHolder.getCarDeviceSpec().initialSettingSpec;
        InitialSetting setting = mStatusHolder.getInitialSetting();
        if(!spec.menuDisplayLanguageSettingSupported) setting.acquiredSetting(Tag.MENU_DISPLAY_LANGUAGE);
        if(!spec.rearOutputSettingSupported) setting.acquiredSetting(Tag.REAR_OUTPUT);
        if(!spec.rearOutputPreoutOutputSettingSupported) setting.acquiredSetting(Tag.REAR_OUTPUT_PREOUT_OUTPUT);
        if(!spec.amStepSettingSupported) setting.acquiredSetting(Tag.AM_STEP);
        if(!spec.fmStepSettingSupported) setting.acquiredSetting(Tag.FM_STEP);
    }

    private void request(OutgoingPacket packet, InitialSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
