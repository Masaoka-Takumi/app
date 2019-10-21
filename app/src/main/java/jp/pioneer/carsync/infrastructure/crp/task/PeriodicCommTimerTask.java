package jp.pioneer.carsync.infrastructure.crp.task;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.SessionConfig;
import timber.log.Timber;

/**
 * 定期通信タイマータスク.
 * <p>
 * セッション開始後、SmartPhoneステータス情報を{@link SessionConfig#getPeriodicCommInterval()}毎に
 * 最新のステータス情報を通知する必要がある。
 * 本タスクを{@link Timer}で定期的に実行することにより、定期通信を実現する。
 */
public class PeriodicCommTimerTask extends TimerTask {
    @Inject CarRemoteSession mSession;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    /**
     * コンストラクタ.
     */
    @Inject
    public PeriodicCommTimerTask() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
        SmartPhoneStatus smartPhoneStatus = mStatusHolder.getSmartPhoneStatus();
        OutgoingPacket packet = mPacketBuilder.createSmartPhoneStatusNotification(version, smartPhoneStatus);
        mSession.sendPacketDirect(packet);
    }
}
