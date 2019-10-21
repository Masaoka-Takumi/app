package jp.pioneer.carsync.infrastructure.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.CustomFlashPatternTransaction;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.FlashPattern;
import jp.pioneer.carsync.domain.model.FlashPatternRegistrationType;
import jp.pioneer.carsync.domain.model.InterruptFlashPatternDirecting;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CUSTOM発光パターン設定用タスク.
 * <p>
 * 車載機にCUSTOM発光パターンを設定する
 * タスク完了やエラー発生時も通知はしない
 * <p>
 * CUSTOM発光パターン設定が無効な場合と、
 * SPH(専用機)以外の場合は何もしない
 */
public class SetCustomFlashPatternTask implements Runnable, RequestTask.Callback<Integer> {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject AppSharedPreference mPreference;
    @Inject Context mContext;
    private FlashPatternRegistrationType mRegistrationType;
    private FlashPattern mFlashPattern;
    private SmartPhoneInterruption mInterruption;
    private CustomFlashPatternTransaction mTransaction;
    private CountDownLatch mCountDownLatch;
    private boolean mIsFailed;

    /**
     * コンストラクタ
     */
    @Inject
    public SetCustomFlashPatternTask() {

    }

    /**
     * パラメータ設定.
     * <p>
     * カスタム発光パターン用
     *
     * @param flashPattern 発光パターン
     * @return 本オブジェクト
     * @throws NullPointerException {@code flashPattern} がnull
     */
    public SetCustomFlashPatternTask setParamsForCustomFlashPattern(@NonNull FlashPattern flashPattern) {
        mRegistrationType = FlashPatternRegistrationType.NORMAL;
        mFlashPattern = checkNotNull(flashPattern);
        return this;
    }

    /**
     * パラメータ設定.
     * <p>
     * 割り込み用
     *
     * @param interruption 割り込み
     * @return 本オブジェクト
     * @throws NullPointerException {@code interruption} がnull
     */
    public SetCustomFlashPatternTask setParamsForInterruption(@NonNull SmartPhoneInterruption interruption) {
        checkNotNull(interruption);

        mRegistrationType = FlashPatternRegistrationType.INTERRUPT;
        mFlashPattern = interruption.pattern;
        mInterruption = interruption;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            checkInterrupted();

            if (!mStatusHolder.getProtocolSpec().isSphCarDevice()) {
                Timber.e("run() is not sph car device.");
                return;
            }

            if(mRegistrationType == FlashPatternRegistrationType.NORMAL &&
                    !mStatusHolder.getIlluminationSettingStatus().customFlashPatternSettingEnabled){
                Timber.e("run() custom flash pattern disabled.");
                return;
            }

            checkInterrupted();

            // 発光パターン取得
            mTransaction = createTransaction();

            if (mRegistrationType == FlashPatternRegistrationType.NORMAL) {
                mStatusHolder.getIlluminationSetting().customFlashPatternRequestStatus = RequestStatus.SENDING;
            }

            while (mTransaction.hasNext()) {
                checkInterrupted();
                // リスト情報要求
                if (!setCustomFlashPattern(mTransaction)) {
                    Timber.e("run() setCustomFlashPattern() failed.");
                    break;
                }
            }

            if (mRegistrationType == FlashPatternRegistrationType.NORMAL) {
                mStatusHolder.getIlluminationSetting().customFlashPatternRequestStatus = RequestStatus.SENT_COMPLETE;

                startCustomFlashPattern();
            } else {
                notificationInterruptInfo();
            }
        } catch (InterruptedException e) {
            Timber.d("run() Interrupted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(Integer result) {
        mIsFailed = result.compareTo(mTransaction.id) != 0;
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
     * トランザクション生成.
     * <p>
     * UnitTest用
     *
     * @return トランザクション
     */
    @VisibleForTesting
    CustomFlashPatternTransaction createTransaction() {
        return new CustomFlashPatternTransaction(mFlashPattern.get(mContext));
    }

    /**
     * 割り込みチェック.
     * <p>
     * UnitTest用
     *
     * @throws InterruptedException 割り込みが発生した
     */
    @VisibleForTesting
    void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private boolean setCustomFlashPattern(CustomFlashPatternTransaction transaction) throws InterruptedException {

        OutgoingPacket packet = mPacketBuilder.createCustomFlashPatternSettingNotification(
                mRegistrationType,
                transaction.id,
                transaction.total,
                transaction.index,
                transaction.getZoneFrameInfo());

        mCountDownLatch = new CountDownLatch(1);
        if (mCarDeviceConnection.sendRequestPacket(packet, this) == null) {
            return false;
        }

        mCountDownLatch.await();
        return !mIsFailed;
    }

    private void startCustomFlashPattern() {
        if (!mPreference.isLightingEffectEnabled()) {
            Timber.e("startCustomFlashPattern() lighting effect disabled.");
            return;
        }

        OutgoingPacket packet = mPacketBuilder.createCustomFlashCommand(CustomFlashRequestType.START);
        mCarDeviceConnection.sendPacket(packet);
    }

    private void notificationInterruptInfo() {
        OutgoingPacket packet = mPacketBuilder.createSmartPhoneInterruptNotification(
                mInterruption.type,
                mInterruption.message,
                InterruptFlashPatternDirecting.ON
        );
        mCarDeviceConnection.sendPacket(packet);
    }
}
