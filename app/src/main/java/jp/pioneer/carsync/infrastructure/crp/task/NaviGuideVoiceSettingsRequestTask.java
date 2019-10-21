package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceSetting;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceSetting.*;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ナビガイド音声設定情報要求タスク.
 */
public class NaviGuideVoiceSettingsRequestTask extends SendTask {
    @Inject
    @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;


    /**
     * コンストラクタ.
     */
    public NaviGuideVoiceSettingsRequestTask() {
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
        return SendTaskId.NAVI_GUIDE_VOICE_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {

        if (!mStatusHolder.shouldSendNaviGuideVoiceSettingRequests()) {
            Timber.d("Can not send navi guide voice setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        NaviGuideVoiceSetting naviGuideVoiceSetting = mStatusHolder.getNaviGuideVoiceSetting();
        naviGuideVoiceSetting.requestStatus = RequestStatus.SENDING;

        // ナビガイド音声設定情報要求
        request(packetBuilder.createNaviGuideVoiceSettingInfoRequest(), naviGuideVoiceSetting, Tag.NAVI_GUIDE_VOICE);

        // ナビガイド音声ボリューム設定情報要求
        request(packetBuilder.createNaviGuideVoiceVolumeSettingInfoRequest(), naviGuideVoiceSetting, Tag.NAVI_GUIDE_VOICE_VOLUME);

        if (naviGuideVoiceSetting.requestStatus == RequestStatus.SENDING) {
            if(naviGuideVoiceSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                naviGuideVoiceSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                naviGuideVoiceSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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

    private void request(OutgoingPacket packet, NaviGuideVoiceSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
