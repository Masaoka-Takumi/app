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
 * パケットハンドラファクトリ.
 * <p>
 * 応答パケット以外を扱う。
 */
@CarRemoteSessionLifeCycle
public class PacketHandlerFactory {
    @Inject CarRemoteSession mSession;
    private Map<Class<? extends PacketHandler>, PacketHandler> mHandlers = new HashMap<>();

    /**
     * コンストラクタ.
     */
    @Inject
    public PacketHandlerFactory() {
    }

    /**
     * 生成.
     *
     * @param packetIdType 受信パケットIDタイプ.
     * @return パケットハンドラ
     * @throws Exception 何らかの例外発生
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public PacketHandler create(@NonNull IncomingPacketIdType packetIdType) throws Exception {
        Class<? extends PacketHandler> clazz = checkNotNull(packetIdType).packetHandlerClass;
        PacketHandler handler = mHandlers.get(clazz);
        if (handler == null) {
            handler = createInstance(clazz);
            mHandlers.put(clazz, handler);
        }

        return handler;
    }

    private PacketHandler createInstance(Class<? extends PacketHandler> clazz) throws Exception {
        Constructor<? extends PacketHandler> constructor = clazz.getConstructor(CarRemoteSession.class);
        return constructor.newInstance(mSession);
    }
}
