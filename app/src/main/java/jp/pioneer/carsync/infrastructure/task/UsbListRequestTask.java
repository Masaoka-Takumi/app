package jp.pioneer.carsync.infrastructure.task;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Objects;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * USBリスト要求タスク.
 * <p>
 *
 * <p>
 * {@link SendTask}の派生クラスで行うとタスク実行中は他の通知や要求を行えないので、使用するのを諦めた。
 */
public class UsbListRequestTask implements Runnable, RequestTask.Callback<Boolean> {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    private Callback mCallback;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;
    private RequestType mType;
    private Future<?> mTaskFuture;

    public enum RequestType{
        LIST_INFO,
        ITEM_INFO
    }

    /**
     * コンストラクタ.
     * <p>
     * タスク実行前に{@link #setParams(Callback, RequestType)}でパラメータを設定すること。
     * （フィールドインジェクションを使用したいため、パラメータが別渡しになっている）
     */
    @Inject
    public UsbListRequestTask() {
    }

    /**
     * パラメータ設定.
     *
     * @param callback コールバック
     * @return 本オブジェクト
     * @throws NullPointerException {@code callback}がnull
     */
    public UsbListRequestTask setParams(@NonNull Callback callback, @NonNull RequestType type) {
        mCallback = checkNotNull(callback);
        mType = checkNotNull(type);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Timber.i("run()");

        try {
            checkInterrupted();

            if(mType == RequestType.LIST_INFO) {
                if (!requestInitialListInfo()) {
                    Timber.e("run() requestInitialListInfo() failed.");
                    return;
                }
            } else {
                ListInfo.TransactionInfo transactionInfo = mStatusHolder.getListInfo().transactionInfo;
                if (!requestListInfo(transactionInfo)) {
                    Timber.e("run() requestListInfo() failed.");
                    return;
                }
            }

            mCallback.onFinish(mType);
        } catch (InterruptedException e) {
            Timber.d("run() Interrupted.");
            if(mTaskFuture != null) {
                if (!mTaskFuture.isDone()) {
                    mTaskFuture.cancel(true);
                }
            }
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

    private boolean requestInitialListInfo() throws InterruptedException {
        ListType listType = ListType.valueOf((byte) ListType.LIST.code, MediaSourceType.USB);
        OutgoingPacket packet = mPacketBuilder.createInitialListInfoRequest(MediaSourceType.USB, listType);
        mCountDownLatch = new CountDownLatch(1);
        mTaskFuture = mCarDeviceConnection.sendRequestPacket(packet, this);
        if (mTaskFuture == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private boolean requestListInfo(ListInfo.TransactionInfo transactionInfo) throws InterruptedException {
        Timber.d("run() requestListInfo() transactionInfo = %s.", transactionInfo);
        if(transactionInfo.listIndex > transactionInfo.total){
            return true;
        }

        OutgoingPacket packet = mPacketBuilder.createListInfoRequest(
                transactionInfo.id,
                transactionInfo.sourceType,
                transactionInfo.listType,
                transactionInfo.listIndex,
                1);
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
         */
        void onFinish(RequestType requestType);
    }
}
