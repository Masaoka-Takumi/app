package jp.pioneer.carsync.application.di.module;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import dagger.Module;
import dagger.Provides;
import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;
import jp.pioneer.carsync.application.di.ForCarRemoteSession;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.SessionLogger;
import jp.pioneer.carsync.infrastructure.crp.SessionLoggerImpl;
import jp.pioneer.carsync.infrastructure.crp.task.TaskStatusMonitor;
import jp.pioneer.carsync.infrastructure.crp.transport.Transport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CarRemoteSessionComponent用のDaggerモジュール.
 */
@Module
public class CarRemoteSessionModule {
    private CarRemoteSession mSession;

    /**
     * コンストラクタ.
     *
     * @param session セッション
     * @throws NullPointerException {@code transport}がnull
     */
    public CarRemoteSessionModule(CarRemoteSession session) {
        mSession = checkNotNull(session);
    }

    @CarRemoteSessionLifeCycle
    @Provides
    public CarRemoteSession provideCarRemoteSession() {
        return mSession;
    }


    @CarRemoteSessionLifeCycle
    @Provides
    public Transport provideTransport() {
        return mSession.getTransport();
    }

    @CarRemoteSessionLifeCycle
    @Provides
    public StatusHolder provideStatusHolder() {
        return mSession.getStatusHolder();
    }

    @CarRemoteSessionLifeCycle
    @Provides
    @Nullable
    public SessionLogger provideSessionLogger(SessionLoggerImpl impl) {
        return impl;
    }

    @ForCarRemoteSession
    @CarRemoteSessionLifeCycle
    @Provides
    public ExecutorService provideExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    public ReentrantLock provideReentrantLock() {
        return new ReentrantLock();
    }

    @CarRemoteSessionLifeCycle
    @Provides
    public TaskStatusMonitor provideTaskStatusMonitor() {
        return mSession;
    }

    @Provides
    public Date provideDate() {
        return new Date();
    }
}
