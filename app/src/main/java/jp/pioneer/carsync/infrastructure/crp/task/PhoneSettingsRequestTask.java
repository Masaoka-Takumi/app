package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.PhoneSetting;
import jp.pioneer.carsync.domain.model.PhoneSetting.*;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Phone設定情報要求タスク.
 */
public class PhoneSettingsRequestTask extends SendTask {
    @Inject
    @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public PhoneSettingsRequestTask() {
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
        return SendTaskId.PHONE_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendPhoneSettingRequests()) {
            Timber.d("Can not send phone setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        PhoneSettingStatus phoneSettingStatus = mStatusHolder.getPhoneSettingStatus();
        PhoneSetting phoneSetting = mStatusHolder.getPhoneSetting();

        boolean isFirst = phoneSetting.requestStatus == RequestStatus.NOT_SENT;
        phoneSetting.requestStatus = RequestStatus.SENDING;

        // Phone設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createPhoneSettingStatusRequest());
        }

        // AUTO ANSWER設定情報要求
        if (phoneSettingStatus.autoAnswerSettingEnabled) {
            request(packetBuilder.createAutoAnswerSettingInfoRequest(), phoneSetting, Tag.AUTO_ANSWER);
        }
        // AUTO PAIRING設定情報要求
        if (phoneSettingStatus.autoPairingSettingEnabled) {
            request(packetBuilder.createAutoPairingSettingInfoRequest(), phoneSetting, Tag.AUTO_PAIRING);
        }

        if (phoneSetting.requestStatus == RequestStatus.SENDING) {
            if(phoneSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                phoneSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                phoneSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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

    private void request(OutgoingPacket packet, PhoneSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
