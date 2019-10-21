package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.SmartPhoneInterruptionController;
import jp.pioneer.carsync.domain.model.InterruptFlashPatternDirecting;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruptType;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.task.SetCustomFlashPatternTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SmartPhoneInterruptionControllerの実装.
 */
public class SmartPhoneInterruptionControllerImpl implements SmartPhoneInterruptionController {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject @ForInfrastructure ExecutorService mTaskExecutor;
    @Inject Provider<SetCustomFlashPatternTask> mSetCustomFlashPatternTaskProvider;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject EventBus mEventBus;
    private Future<?> mTaskFuture;

    /**
     * コンストラクタ.
     */
    @Inject
    public SmartPhoneInterruptionControllerImpl(){

    }

    /**
     * 初期化.
     */
    public void initialize(){
        Timber.i("initialize()");

        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt(@NonNull SmartPhoneInterruption interruption) {
        Timber.i("interrupt() interruption = %s", interruption);
        checkNotNull(interruption);

        sendInterruption(interruption);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseInterrupt() {
        Timber.i("releaseInterrupt()");

        postReleaseInterruption();
    }

    /**
     * セッション停止イベントハンドラ.
     *
     * @param ev セッション停止イベント.
     */
    @Subscribe
    public void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev){
        stopTask();
    }

    private void sendInterruption(SmartPhoneInterruption interruption){
        if(mStatusHolder.getProtocolSpec().isSphCarDevice()) {
            startCustomFlashPatternTask(interruption);
        } else {
            postInterruption(interruption, false);
        }
    }

    private void startCustomFlashPatternTask(SmartPhoneInterruption interruption) {
        stopTask();
        mTaskFuture = mTaskExecutor.submit(mSetCustomFlashPatternTaskProvider.get().setParamsForInterruption(interruption));
    }

    private void stopTask() {
        if (mTaskFuture != null && !mTaskFuture.isDone()) {
            mTaskFuture.cancel(true);
            mTaskFuture = null;
        }
    }

    private void postReleaseInterruption(){
        notificationInterruptInfo(SmartPhoneInterruptType.RELEASE, "", InterruptFlashPatternDirecting.OFF);
    }

    private void postInterruption(SmartPhoneInterruption interruption, boolean isLighting){
        notificationInterruptInfo(interruption.type, interruption.message, isLighting ? InterruptFlashPatternDirecting.ON : InterruptFlashPatternDirecting.OFF);
    }

    private void notificationInterruptInfo(SmartPhoneInterruptType interruptType, String displayText, InterruptFlashPatternDirecting directing) {
        mCarDeviceConnection.sendPacket(mPacketBuilder.createSmartPhoneInterruptNotification(
                interruptType,
                displayText,
                directing
        ));
    }
}
