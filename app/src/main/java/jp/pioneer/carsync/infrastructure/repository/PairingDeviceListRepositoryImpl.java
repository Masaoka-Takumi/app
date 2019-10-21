package jp.pioneer.carsync.infrastructure.repository;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;
import jp.pioneer.carsync.infrastructure.task.PairingDeviceListRequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * PairingDeviceListRepositoryの実装.
 */
public class PairingDeviceListRepositoryImpl implements PairingDeviceListRepository {
    @Inject @ForInfrastructure ExecutorService mTaskExecutor;
    @Inject Provider<PairingDeviceListRequestTask> mTaskProvider;
    private Future mTaskFuture;

    /**
     * コンストラクタ.
     */
    @Inject
    public PairingDeviceListRepositoryImpl(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void get(@NonNull PairingSpecType type, @NonNull Callback callback) {
        Timber.i("get() type = %s, callback = %s", type, callback);
        checkNotNull(type);
        checkNotNull(callback);

        if(isRunningTask(mTaskFuture)){
            stopTask();
        }

        startTask(type, callback);
    }

    private void startTask(PairingSpecType type, Callback callback) {

        PairingDeviceListRequestTask task = mTaskProvider.get().setParam(type, callback);
        mTaskFuture = mTaskExecutor.submit(task);
    }

    private void stopTask() {
        mTaskFuture.cancel(true);
        mTaskFuture = null;
    }

    private boolean isRunningTask(Future<?> future) {
        return (future != null && !future.isDone());
    }
}
