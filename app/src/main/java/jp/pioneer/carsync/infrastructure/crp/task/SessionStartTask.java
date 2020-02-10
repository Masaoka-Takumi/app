package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.AuthenticationException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacketIdType;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.SendTimeoutException;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import jp.pioneer.carsync.infrastructure.crp.handler.SimpleResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * セッション開始タスク.
 * <p>
 * 初期通信～セッション開始までの一連の処理を行う。
 */
public class SessionStartTask extends SendTask {
    @Inject CarRemoteSession mSession;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;
    private AuthenticationException mException;

    /**
     * コンストラクタ.
     */
    public SessionStartTask() {
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
        return SendTaskId.SESSION_START;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTask() throws Exception {
        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        try {
            // 初期通信
            initComm(packetBuilder);
            // セッション開始
            startSession(packetBuilder);
        } catch (AuthenticationException e) {
            // 認証エラー
            post(packetBuilder.createAuthError(e.errorIdType));
            // 直ぐに切断すると通知が届かないかもしれないので少し待機する
            Thread.sleep(mSessionConfig.getErrorCloseWaitTime());
            // CarRemoteSession#stop()はCarRemoteSession#onTaskFailed()で行うので、ここでは行わない
            throw e;
        } catch (SendTimeoutException e) {
            mSession.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        ResponsePacketHandler handler = mHandlerFactory.create(packet.getPacketIdType());
        handler.handle(packet);
        switch (packet.getPacketIdType()) {
            case START_INITIAL_COMM_RESPONSE:               // 初期通信開始応答
            case START_GET_DEVICE_SPEC_RESPONSE:            // 車載機情報取得開始応答
            case END_GET_DEVICE_SPEC_RESPONSE:              // 車載機情報取得終了応答
            case START_SEND_SMART_PHONE_SPEC_RESPONSE:      // SmartPhone情報通知開始応答
            case SMART_PHONE_SPEC_NOTIFICATION_RESPONSE:    // SmartPhoneSpec通知応答
            case SMART_PHONE_STATUS_NOTIFICATION_RESPONSE:  // SmartPhoneステータス情報（初回）通知応答
            case TIME_NOTIFICATION_RESPONSE:                // 時刻情報通知応答
            case END_SEND_SMART_PHONE_SPEC_RESPONSE:        // SmartPhone情報通知終了応答
            case END_INITIAL_COMM_RESPONSE:                 // 初期通信終了応答
            case START_SESSION_RESPONSE:                    // セッション開始応答
                onSimpleResponse(packet.getPacketIdType(), ((SimpleResponsePacketHandler) handler).getResult());
                break;
            case DEVICE_SPEC_RESPONSE:                      // 車載機Spec応答
            case DEVICE_STATUS_RESPONSE:                    // 車載機ステータス情報通知応答
            case DEVICE_MODEL_RESPONSE:                     // 車載機型番応答
            case DEVICE_BD_ADDRESS_RESPONSE:                // 車載機BDアドレス応答
            case DEVICE_FARM_VERSION_RESPONSE:                // 車載機ソフトウェアバージョン応答
                onBooleanResponse(packet.getPacketIdType(), ((DataResponsePacketHandler) handler).getResult());
                break;
            default:
                Timber.w("doResponsePacket() unexpected packet. PacketIdType = " + packet.getPacketIdType());
        }
    }

    private void initComm(OutgoingPacketBuilder packetBuilder) throws InterruptedException, SendTimeoutException {
        ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();

        // 初期通信開始
        doRequest(packetBuilder.createStartInitialComm());
        // 車載機情報取得開始
        doRequest(packetBuilder.createStartGetDeviceSpec());
        // 車載機Spec要求
        doRequest(packetBuilder.createDeviceSpecRequest());
        // 車載機ステータス情報要求
        doRequest(packetBuilder.createDeviceStatusRequest());
        // 車載機型番要求
        doRequest(packetBuilder.createDeviceModelRequest());
        // 車載機BDアドレス要求
        doRequest(packetBuilder.createDeviceBdAddressRequest());
        if(version.isGreaterThanOrEqual(ProtocolVersion.V4_1)) {
            // 車載機ソフトウェアバージョン要求
            doRequest(packetBuilder.createDeviceFarmVersionRequest());
        }
        // 車載機情報取得終了
        doRequest(packetBuilder.createEndGetDeviceSpec());
        // SmartPhone情報通知開始
        doRequest(packetBuilder.createStartSendSmartPhoneSpec());
        // SmartPhoneSpec通知
        doRequest(packetBuilder.createSmartPhoneSpecNotification());
        // SmartPhoneステータス情報の通知
        SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
        doRequest(packetBuilder.createSmartPhoneStatusInitialNotification(version, smartPhoneStatus));
        // 時刻情報通知
        doRequest(packetBuilder.createTimeNotification());
        // SmartPhone情報通知終了
        doRequest(packetBuilder.createEndSendSmartPhoneSpec());
        // 初期通信終了
        doRequest(packetBuilder.createEndInitialComm());
    }

    private void startSession(OutgoingPacketBuilder packetBuilder) throws InterruptedException, SendTimeoutException {
        // セッション開始
        doRequest(packetBuilder.createStartSession());
    }

    private void doRequest(OutgoingPacket packet) throws InterruptedException, SendTimeoutException {
        request(packet);
        if (mException != null) {
            throw mException;
        }
    }

    private void onSimpleResponse(IncomingPacketIdType packetIdType, ResponseCode result) {
        mException = (result != ResponseCode.OK) ? new AuthenticationException(packetIdType) : null;
    }

    private void onBooleanResponse(IncomingPacketIdType packetIdType, Boolean result) {
        mException = !Objects.equal(result, Boolean.TRUE) ? new AuthenticationException(packetIdType) : null;
    }
}
