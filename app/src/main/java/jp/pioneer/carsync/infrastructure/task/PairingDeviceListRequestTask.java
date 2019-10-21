package jp.pioneer.carsync.infrastructure.task;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.PairingDeviceList;
import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ペアリングデバイスリスト要求タスク.
 */
public class PairingDeviceListRequestTask implements Runnable, RequestTask.Callback<Boolean> {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject EventBus mEventBus;
    private PairingSpecType mType;
    private CountDownLatch mCountDownLatch;
    private PairingDeviceListRepository.Callback mCallback;
    private boolean mIsFailed;

    /**
     * コンストラクタ.
     */
    @Inject
    public PairingDeviceListRequestTask(){

    }

    /**
     * パラメータ設定.
     *
     * @param type ペアリング規格種別
     * @return 本オブジェクト
     * @throws NullPointerException {@code type}がnull
     */
    public PairingDeviceListRequestTask setParam(@NonNull PairingSpecType type,
                                                 @NonNull PairingDeviceListRepository.Callback callback){
        mType = checkNotNull(type);
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

            // デバイスリスト情報取得
            if(!requestPairingDeviceListInfo()){
                Timber.e("run() requestPairingDeviceListInfo() failed.");
                return;
            }

            PairingDeviceList deviceList = mStatusHolder.getDebugInfo().getDeviceList(mType);
            for(String address : deviceList.getAddressList()){
                checkInterrupted();

                // デバイス情報取得
                if (!requestPairingDeviceInfo(address)) {
                    Timber.e("run() requestPairingDeviceInfo() failed.");
                    return;
                }
            }



        } catch (InterruptedException e) {
            Timber.d("run() Interrupted.");
        } finally {
            mCallback.onComplete(mType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(Boolean result) {
        mIsFailed = Objects.equals(result, Boolean.FALSE);
        mCountDownLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError() {
        mIsFailed = false;
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

    private boolean requestPairingDeviceListInfo() throws InterruptedException {
        OutgoingPacket packet = mPacketBuilder.createPairingDeviceListInfoRequest(mType);
        mCountDownLatch = new CountDownLatch(1);
        if(mCarDeviceConnection.sendRequestPacket(packet, this) == null){
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private boolean requestPairingDeviceInfo(String bdAddress) throws InterruptedException{
        OutgoingPacket packet = mPacketBuilder.createPairingDeviceInfoRequest(
                bdAddress,
                mType
        );
        mCountDownLatch = new CountDownLatch(1);
        if(mCarDeviceConnection.sendRequestPacket(packet, this) == null){
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }
}
