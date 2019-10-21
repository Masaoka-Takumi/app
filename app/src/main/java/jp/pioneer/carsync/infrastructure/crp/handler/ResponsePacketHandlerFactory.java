package jp.pioneer.carsync.infrastructure.crp.handler;

import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacketIdType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 応答パケットハンドラファクトリ.
 */
@CarRemoteSessionLifeCycle
public class ResponsePacketHandlerFactory {
    @Inject CarRemoteSession mSession;
    private Map<Class<? extends ResponsePacketHandler>, ResponsePacketHandler> mHandlers = new HashMap<>();

    /**
     * コンストラクタ.
     */
    @Inject
    public ResponsePacketHandlerFactory() {
    }

    /**
     * 生成.
     *
     * @param packetIdType 受信パケットIDタイプ.
     * @return 応答パケットハンドラ
     * @throws Exception 何らかの例外発生
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <T> ResponsePacketHandler<T> create(@NonNull IncomingPacketIdType packetIdType) throws Exception {
        Class<? extends ResponsePacketHandler> clazz =
                (Class<? extends ResponsePacketHandler>) checkNotNull(packetIdType).packetHandlerClass;
        ResponsePacketHandler handler = mHandlers.get(clazz);
        if (handler == null) {
            handler = createInstance(clazz);
            mHandlers.put(clazz, handler);
        }

        return handler;
    }

    private ResponsePacketHandler createInstance(Class<? extends ResponsePacketHandler> clazz) throws Exception {
        Constructor<? extends ResponsePacketHandler> constructor = clazz.getConstructor(CarRemoteSession.class);
        return constructor.newInstance(mSession);
    }
}
