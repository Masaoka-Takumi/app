package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;

/**
 * 車載機音声認識実行通知.
 */
public class DeviceVoiceRecognition {
    @Inject
    @ForInfrastructure
    Handler mHandler;
    @Inject
    CarDevice mCarDevice;

    /**
     * コンストラクタ
     */
    @Inject
    public DeviceVoiceRecognition(){
    }

    /**
     * 開始.
     *
     */
    public void start(){
        mHandler.post(() -> mCarDevice.startDeviceVoiceRecognition(true));
    }
}
