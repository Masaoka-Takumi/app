package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import java.util.TimerTask;

import javax.inject.Inject;

import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.SessionConfig;
import jp.pioneer.carsync.infrastructure.crp.SessionLogger;
import timber.log.Timber;

/**
 * 通信監視タイマータスク.
 * <p>
 * 車載機から{@link SessionConfig#getIdleReceiveCommTimeout()}間何も受信しない場合、切断（セッション停止）する。
 * 本タスクは、「切断（セッション停止）する」部分を実装しているので、車載機から何か受信する度に、
 * {@link SessionConfig#getIdleReceiveCommTimeout()}後に発火するように本タスクをリスタートすれば良い。
 */
public class IdleReceiveCommTimerTask extends TimerTask {
    @Inject CarRemoteSession mSession;
    @Inject @Nullable SessionLogger mLogger;

    /**
     * コンストラクタ.
     */
    @Inject
    public IdleReceiveCommTimerTask() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        Optional.ofNullable(mLogger)
                .ifPresent(SessionLogger::receiveTimeout);
        mSession.stop();
    }
}
