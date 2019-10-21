package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Function設定情報要求タスク.
 */
public class FunctionSettingsRequestTask extends SendTask {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;

    /**
     * コンストラクタ.
     */
    public FunctionSettingsRequestTask() {
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
        return SendTaskId.FUNCTION_SETTINGS_REQUEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        if (!mStatusHolder.shouldSendFunctionSettingRequests()) {
            Timber.d("Can not send function setting requests.");
            return;
        }

        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;

        boolean isFirst = getRequestStatus(sourceType) == RequestStatus.NOT_SENT;
        setRequestStatus(sourceType, RequestStatus.SENDING);

        // Function設定ステータス情報要求
        if(isFirst) {
            requestNoSendTimeout(packetBuilder.createFunctionSettingStatusRequest());
        }

        switch (sourceType) {
            case RADIO:
            case HD_RADIO:
            case DAB:
                // Function設定情報要求
                requestNoSendTimeout(packetBuilder.createFunctionSettingInfoRequest());
                break;
            case BT_AUDIO:
                // Function設定が無い
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        // 要求前とソースが異なる場合、未送信に戻す（再度要求することになるが、そうそう発生するものではない）
        MediaSourceType current = mStatusHolder.getCarDeviceStatus().sourceType;
        if (sourceType == current) {
            setRequestStatus(sourceType, RequestStatus.SENT_COMPLETE);
        } else {
            Timber.w("doTask() Source changed. (before, after) = %s, %s", sourceType, current);
            setRequestStatus(sourceType, RequestStatus.NOT_SENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        mHandlerFactory.create(packet.getPacketIdType()).handle(packet);
    }

    private void setRequestStatus(MediaSourceType sourceType, RequestStatus requestStatus) {
        switch (sourceType) {
            case RADIO:
                mStatusHolder.getTunerFunctionSetting().requestStatus = requestStatus;
                break;
            case DAB:
                mStatusHolder.getDabFunctionSetting().requestStatus = requestStatus;
                break;
            case HD_RADIO:
                mStatusHolder.getHdRadioFunctionSetting().requestStatus = requestStatus;
                break;
            case BT_AUDIO:
                // Function設定が無い
                break;
            default:
                throw new AssertionError("can't happen.");
        }
    }

    private RequestStatus getRequestStatus(MediaSourceType sourceType) {
        switch (sourceType) {
            case RADIO:
                return mStatusHolder.getTunerFunctionSetting().requestStatus;
            case DAB:
                return mStatusHolder.getDabFunctionSetting().requestStatus;
            case HD_RADIO:
                return mStatusHolder.getHdRadioFunctionSetting().requestStatus;
            case BT_AUDIO:
                // Function設定が無い
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        return RequestStatus.NOT_SENT;
    }
}
