package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSetting.*;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * イルミ設定情報要求タスク.
 */
public class IlluminationSettingsRequestTask extends SendTask {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;

    /**
     * コンストラクタ.
     */
    public IlluminationSettingsRequestTask() {
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
        return SendTaskId.ILLUMINATION_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        if (!mStatusHolder.shouldSendIlluminationSettingRequests()) {
            Timber.d("Can not send illumination setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        IlluminationSettingStatus illuminationSettingStatus = mStatusHolder.getIlluminationSettingStatus();
        IlluminationSetting illuminationSetting = mStatusHolder.getIlluminationSetting();

        boolean isFirst = illuminationSetting.requestStatus == RequestStatus.NOT_SENT;
        illuminationSetting.requestStatus = RequestStatus.SENDING;

        // イルミ設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createIlluminationSettingStatusRequest());
            setSettingTagIfUnsupported();
        }

        // KEY COLOR設定情報要求
        if (illuminationSettingStatus.keyColorSettingEnabled) {
            request(packetBuilder.createKeyColorSettingInfoRequest(), illuminationSetting, Tag.KEY_COLOR);
            request(packetBuilder.createKeyColorBulkSettingInfoRequest(), illuminationSetting, Tag.KEY_COLOR_SPEC);
        }
        // DISP COLOR設定情報要求
        if (illuminationSettingStatus.dispColorSettingEnabled) {
            request(packetBuilder.createDispColorSettingInfoRequest(), illuminationSetting, Tag.DISP_COLOR);
            request(packetBuilder.createDispColorBulkSettingInfoRequest(), illuminationSetting, Tag.DISP_COLOR_SPEC);
        }
        // DIMMER設定情報要求
        if (illuminationSettingStatus.dimmerSettingEnabled) {
            request(packetBuilder.createDimmerSettingInfoRequest(), illuminationSetting, Tag.DIMMER);
        }
        // BRIGHTNESS設定情報要求（共通設定モデル用）
        if (illuminationSettingStatus.brightnessSettingEnabled) {
            request(packetBuilder.createBrightnessSettingInfoRequest(), illuminationSetting, Tag.BRIGHTNESS);
        }
        // KEY BRIGHTNESS設定情報要求（個別設定モデル用）
        if (illuminationSettingStatus.keyBrightnessSettingEnabled) {
            request(packetBuilder.createKeyBrightnessSettingInfoRequest(), illuminationSetting, Tag.KEY_BRIGHTNESS);
        }
        // DISP BRIGHTNESS設定情報要求（個別設定モデル用）
        if (illuminationSettingStatus.dispBrightnessSettingEnabled) {
            request(packetBuilder.createDisplayBrightnessSettingInfoRequest(), illuminationSetting, Tag.DISP_BRIGHTNESS);
        }
        // BT PHONE COLOR情報要求
        if (illuminationSettingStatus.btPhoneColorSettingEnabled) {
            request(packetBuilder.createBtPhoneColorSettingInfoRequest(), illuminationSetting, Tag.BT_PHONE_COLOR);
        }
        // 蛍の光風設定情報要求
        if (illuminationSettingStatus.hotaruNoHikariLikeSettingEnabled) {
            request(packetBuilder.createIlluminationSettingInfoRequest(), illuminationSetting, Tag.ILLUMINATION_EFFECT);
        }
        // オーディオレベルメータ連動設定要求
        if (illuminationSettingStatus.audioLevelMeterLinkedSettingEnabled) {
            request(packetBuilder.createAudioLevelMeterLinkedSettingInfoRequest(), illuminationSetting, Tag.AUDIO_LEVEL_MATER);
        }
        // [SPH] BT PHONE COLOR設定情報要求
        if (illuminationSettingStatus.sphBtPhoneColorSettingEnabled) {
            request(packetBuilder.createSphBtPhoneColorSettingInfoRequest(), illuminationSetting, Tag.SPH_BT_PHONE_COLOR);
        }
        // COLOR設定情報要求  (共通設定モデル用)
        if (illuminationSettingStatus.commonColorSettingEnabled) {
            request(packetBuilder.createCommonColorSettingInfoRequest(), illuminationSetting, Tag.COMMON_COLOR);
            request(packetBuilder.createCommonColorBulkSettingInfoRequest(), illuminationSetting, Tag.COMMON_COLOR_SPEC);
        }
        // メッセージ受信通知COLOR設定情報要求
        if (illuminationSettingStatus.incomingMessageColorSettingEnabled) {
            request(packetBuilder.createIncomingMessageColorSettingInfoRequest(), illuminationSetting, Tag.INCOMING_MESSAGE_COLOR);
        }

        // 設定取得中にLOAD SETTINGを行った場合は再度取得しにいくのでNOT_SENTだったら更新しない
        if (illuminationSetting.requestStatus == RequestStatus.SENDING) {
            if(illuminationSetting.isAllSettingAcquisition(Tag.getAllTags())) {
                illuminationSetting.requestStatus = RequestStatus.SENT_COMPLETE;
            } else {
                illuminationSetting.requestStatus = RequestStatus.SENT_INCOMPLETE;
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
        IlluminationSettingSpec spec = mStatusHolder.getCarDeviceSpec().illuminationSettingSpec;
        IlluminationSetting setting = mStatusHolder.getIlluminationSetting();
        if(!spec.hotaruNoHikariLikeSettingSupported) setting.acquiredSetting(Tag.ILLUMINATION_EFFECT);
        if(!spec.btPhoneColorSettingSupported) setting.acquiredSetting(Tag.BT_PHONE_COLOR);
        if(!spec.brightnessSettingSupported) setting.acquiredSetting(Tag.BRIGHTNESS);
        if(!spec.dimmerSettingSupported) setting.acquiredSetting(Tag.DIMMER);
        if(!spec.dispColorSettingSupported) setting.acquiredSetting(Tag.DISP_COLOR);
        if(!spec.dispColorSettingSupported) setting.acquiredSetting(Tag.DISP_COLOR_SPEC);
        if(!spec.keyColorSettingSupported) setting.acquiredSetting(Tag.KEY_COLOR);
        if(!spec.keyColorSettingSupported) setting.acquiredSetting(Tag.KEY_COLOR_SPEC);
        if(!spec.incomingMessageColorSettingSupported) setting.acquiredSetting(Tag.INCOMING_MESSAGE_COLOR);
        if(!spec.commonColorSettingSupported) setting.acquiredSetting(Tag.COMMON_COLOR);
        if(!spec.commonColorCustomSettingSupported) setting.acquiredSetting(Tag.COMMON_COLOR_SPEC);
        if(!spec.sphBtPhoneColorSettingSupported) setting.acquiredSetting(Tag.SPH_BT_PHONE_COLOR);
        if(!spec.audioLevelMeterLinkedSettingSupported) setting.acquiredSetting(Tag.AUDIO_LEVEL_MATER);
        if(!spec.dispBrightnessSettingSupported) setting.acquiredSetting(Tag.DISP_BRIGHTNESS);
        if(!spec.keyBrightnessSettingSupported) setting.acquiredSetting(Tag.KEY_BRIGHTNESS);
    }

    private void request(OutgoingPacket packet, IlluminationSetting setting, String tag) throws Exception {
        if(!setting.isSettingAcquisition(tag)) {
            if (requestNoSendTimeout(packet)) {
                setting.acquiredSetting(tag);
            }
        }
    }
}
