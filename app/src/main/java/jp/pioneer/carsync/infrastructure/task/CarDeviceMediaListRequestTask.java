package jp.pioneer.carsync.infrastructure.task;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Objects;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AppStatusChangeEvent;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機メディアリスト要求タスク.
 * <p>
 * 車載機メディアのリストは取得可能数制限により複数回に分けて取得する必要があるためタスク化している。
 * {@link SendTask}の派生クラスで行うとタスク実行中は他の通知や要求を行えないので、使用するのを諦めた。
 */
public class CarDeviceMediaListRequestTask implements Runnable, RequestTask.Callback<Boolean> {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject EventBus mEventBus;
    private MediaSourceType mSourceType;
    private ListType mListType;
    private Callback mCallback;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;
    private Future<?> mTaskFuture;

    /**
     * コンストラクタ.
     * <p>
     * タスク実行前に{@link #setParams(MediaSourceType, ListType, Callback)}でパラメータを設定すること。
     * （フィールドインジェクションを使用したいため、パラメータが別渡しになっている）
     */
    @Inject
    public CarDeviceMediaListRequestTask() {
    }

    /**
     * パラメータ設定.
     *
     * @param sourceType ソース種別
     * @param listType リスト種別
     * @param callback コールバック
     * @return 本オブジェクト
     * @throws NullPointerException {@code sourceType}、{@code listType}、{@code callback}のいずれかがnull
     */
    public CarDeviceMediaListRequestTask setParams(@NonNull MediaSourceType sourceType,
                                                   @NonNull ListType listType,
                                                   @NonNull Callback callback) {
        mSourceType = checkNotNull(sourceType);
        mListType = checkNotNull(listType);
        mCallback = checkNotNull(callback);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");
        try {
            updateRunningTaskStatus(true);

            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            if(info.isSearchStatus()){
                Timber.e("run() is search status.");
                return;
            }

            if(info.rdsInterruptionType != null && info.rdsInterruptionType != RdsInterruptionType.NORMAL){
                Timber.e("run() is rds interrupted.");
                return;
            }

            checkInterrupted();
            ListInfo.TransactionInfo transactionInfo = mStatusHolder.getListInfo().transactionInfo;
            // リスト初期情報要求
            if (!requestInitialListInfo()) {
                Timber.e("run() requestInitialListInfo() failed.");
                return;
            }

            while (transactionInfo.hasNext()) {
                checkInterrupted();
                // リスト情報要求
                if (!requestListInfo(transactionInfo)) {
                    Timber.e("run() requestListInfo() failed.");
                    return;
                }
            }

            mCallback.onFinish();
        } catch (InterruptedException e) {
            Timber.d("run() Interrupted.");
            if(mTaskFuture != null) {
                if (!mTaskFuture.isDone()) {
                    mTaskFuture.cancel(true);
                }
            }
        } finally {
            updateRunningTaskStatus(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(Boolean result) {
        mIsFailed = !Objects.equal(result, Boolean.TRUE);
        mCountDownLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError() {
        mIsFailed = true;
        mCountDownLatch.countDown();
    }

    /**
     * 割り込みチェック.
     *
     * @throws InterruptedException 割り込みが発生した
     */
    @VisibleForTesting
    void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void updateRunningTaskStatus(boolean isRunning){
        AppStatus status = mStatusHolder.getAppStatus();
        status.isRunningListTask = isRunning;

        mEventBus.post(new AppStatusChangeEvent());
    }

    private boolean requestInitialListInfo() throws InterruptedException {
        ListType listType = ListType.valueOf((byte) mListType.code, mSourceType);
        OutgoingPacket packet = mPacketBuilder.createInitialListInfoRequest(mSourceType, listType);
        mCountDownLatch = new CountDownLatch(1);
        mTaskFuture = mCarDeviceConnection.sendRequestPacket(packet, this);
        if (mTaskFuture == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private boolean requestListInfo(ListInfo.TransactionInfo transactionInfo) throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createListInfoRequest(
                transactionInfo.id,
                transactionInfo.sourceType,
                transactionInfo.listType,
                transactionInfo.listIndex,
                transactionInfo.limit);
        mCountDownLatch = new CountDownLatch(1);
        mTaskFuture = mCarDeviceConnection.sendRequestPacket(packet, this);
        if (mTaskFuture == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    /**
     * コールバック.
     */
    public interface Callback {
        /**
         * 終了ハンドラ.
         * <p>
         * リスト情報を全て取得出来た場合に呼ばれる。
         * 取得した情報はトランザクション情報に格納されている。
         * <pre>{@code
         *  mStatusHolder.getCarDeviceStatus().listInfo.transactionInfo
         * }</pre>
         *
         * リスト情報が取得出来なかった場合何もしないので、取得済みのプリセットを
         * そのまま使用することになる。取得出来ない場合はクリアした方が良いと思うが、
         * ARCがクリアしていなかったのでクリアしないようにしている。
         */
        void onFinish();
    }
}
