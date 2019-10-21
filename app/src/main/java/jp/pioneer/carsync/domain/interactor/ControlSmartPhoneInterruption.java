package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.SmartPhoneInterruptionController;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * スマートフォン割り込み操作.
 */
public class ControlSmartPhoneInterruption {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject SmartPhoneInterruptionController mController;

    /**
     * コンストラクタ.
     */
    @Inject
    public ControlSmartPhoneInterruption(){

    }

    /**
     * 割り込み追加.
     *
     * @param interruption 割り込み
     * @throws NullPointerException {@code interruption}がnull
     */
    public void addInterruption(@NonNull SmartPhoneInterruption interruption){
        checkNotNull(interruption);

        mHandler.post(() -> mController.interrupt(interruption));

    }

    /**
     * 割り込み解除.
     */
    public void releaseInterruption(){
        mHandler.post(() -> mController.releaseInterrupt());
    }
}
