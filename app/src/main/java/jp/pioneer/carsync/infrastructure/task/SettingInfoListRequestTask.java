package jp.pioneer.carsync.infrastructure.task;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Objects;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.SettingListItem;
import jp.pioneer.carsync.domain.model.SettingListTransaction;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機設定リスト要求タスク.
 * <p>
 * 車載機の設定リスト(Bluetooth DeviceList,Bluetooth SearchList)を1つずつ取得する必要があるためタスク化している。
 */
public class SettingInfoListRequestTask implements Runnable, RequestTask.Callback<Boolean> {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    private SettingListType mSettingListType;
    private Callback mCallback;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;
    private Future<?> mTaskFuture;

    /**
     * コンストラクタ
     */
    @Inject
    public SettingInfoListRequestTask() {

    }

    /**
     * パラメータ設定.
     *
     * @param listType 設定リスト種別
     * @param callback コールバック
     * @return 本オブジェクト
     * @throws NullPointerException {@code listType} がnull
     * @throws NullPointerException {@code callback} がnull
     */
    public SettingInfoListRequestTask setParams(@NonNull SettingListType listType,
                                                @NonNull SettingInfoListRequestTask.Callback callback) {
        mSettingListType = checkNotNull(listType);
        mCallback = checkNotNull(callback);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            checkInterrupted();

            if (!requestPhoneSettingStatus()) {
                Timber.e("run() requestPhoneSettingStatus() failed.");
                return;
            }

            if (!mStatusHolder.isSettingListEnabled(mSettingListType)) {
                Timber.e("run() isSettingListEnabled() setting list disabled.");
                return;
            }

            checkInterrupted();
            SettingListTransaction transactionInfo = mStatusHolder.getSettingListInfoMap().getTransaction(mSettingListType);

            // リスト初期情報要求
            if (!requestInitialSettingListInfo()) {
                Timber.e("run() requestInitialSettingListInfo() failed.");
                return;
            }

            while (transactionInfo.hasNext()) {
                checkInterrupted();
                // リスト情報要求
                if (!requestSettingListInfo(transactionInfo)) {
                    if (!requestSettingListInfo(transactionInfo)) {
                        Timber.e("run() requestSettingListInfo() failed.");
                        return;
                    }
                }
                // item取得
                if(transactionInfo.items.get(transactionInfo.listIndex) != null) {
                    mCallback.onGetItem(mSettingListType, transactionInfo.items.get(transactionInfo.listIndex), transactionInfo.listIndex);
                }
            }

            if(transactionInfo.total == 0){
                mCallback.onNoneItem(mSettingListType);
            }

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
        mIsFailed = Objects.equal(result, Boolean.FALSE);
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

    private boolean requestPhoneSettingStatus() throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createPhoneSettingStatusRequest();
        mCountDownLatch = new CountDownLatch(1);
        mTaskFuture = mCarDeviceConnection.sendRequestPacket(packet, this);
        if (mTaskFuture == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private boolean requestInitialSettingListInfo() throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createInitialSettingListInfoRequest(mSettingListType);
        mCountDownLatch = new CountDownLatch(1);
        mTaskFuture = mCarDeviceConnection.sendRequestPacket(packet, this);
        if (mTaskFuture == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private boolean requestSettingListInfo(SettingListTransaction transactionInfo) throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createSettingListInfoRequest(
                transactionInfo.id,
                transactionInfo.listType,
                transactionInfo.listIndex
        );
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
         * SettingListItem無しハンドラ
         * <p>
         * SettingListItemがない場合に呼ばれる
         */
        void onNoneItem(SettingListType listType);

        /**
         * SettingListItem取得ハンドラ.
         * <p>
         * SettingListItemを取得する度に呼ばれる。
         */
        void onGetItem(SettingListType listType, SettingListItem item, int listIndex);
    }
}
