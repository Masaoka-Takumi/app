package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
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
import static jp.pioneer.carsync.infrastructure.crp.IncomingPacketIdType.PROTOCOL_VERSION_RESPONSE;

/**
 * 初期認証タスク.
 */
public class InitialAuthTask extends SendTask {
    @Inject CarRemoteSession mSession;
    @Inject @ForInfrastructure
    StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;
    private AuthenticationException mException;

    /**
     * コンストラクタ.
     */
    public InitialAuthTask() {
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
        return SendTaskId.INITIAL_AUTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        try {
            // 初期認証
            initAuth(packetBuilder);
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
            case START_INITIAL_AUTH_RESPONSE:               // 認証開始応答
            case PROTOCOL_VERSION_NOTIFICATION_RESPONSE:    // Protocol Version通知応答
            case END_INITIAL_AUTH_RESPONSE:                 // 認証終了応答
                onSimpleResponse(packet.getPacketIdType(), ((SimpleResponsePacketHandler) handler).getResult());
                break;
            case CLASS_ID_REQUEST_RESPONSE:                 // Class ID応答
            case PROTOCOL_VERSION_RESPONSE:                 // Protocol Version応答
                onBooleanResponse(packet.getPacketIdType(), ((DataResponsePacketHandler) handler).getResult());
                break;
            default:
                Timber.w("doResponsePacket() unexpected packet. PacketIdType = " + packet.getPacketIdType());
        }
    }

    private void initAuth(OutgoingPacketBuilder packetBuilder) throws InterruptedException, SendTimeoutException {
        // 最初のパケットを送信するまで2秒待つ（ARCのコードより）
        Thread.sleep(2000);
        // 認証開始
        doRequest(packetBuilder.createStartInitialAuth());
        // ClassID要求
        doRequest(packetBuilder.createClassIdRequest());
        // Protocol version要求
        doRequest(packetBuilder.createProtocolVersionRequest());
        // Protocol versionの決定とProtocol version通知
        ProtocolSpec protocolSpec = mStatusHolder.getProtocolSpec();
        ProtocolVersion version = protocolSpec.getSuitableProtocolVersion();
        if (version == null) {
            throw new AuthenticationException(PROTOCOL_VERSION_RESPONSE);
        }
        protocolSpec.setConnectingProtocolVersion(version);
        doRequest(packetBuilder.createProtocolVersionNotification(version));
        // 認証終了
        doRequest(packetBuilder.createEndInitialAuth());
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
