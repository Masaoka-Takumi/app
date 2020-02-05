package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;

/**
 * 車載機ボリューム指定通知.
 */
public class ChangeVolume {
    @Inject
    @ForInfrastructure
    Handler mHandler;
    @Inject
    CarDevice mCarDevice;

    /**
     * コンストラクタ
     */
    @Inject
    public ChangeVolume(){
    }

    /**
     * 実行.
     *
     * @param volume ボリューム値

     */
    public void execute(int volume){
        mHandler.post(() -> mCarDevice.changeDeviceVolume(volume));
    }
}
