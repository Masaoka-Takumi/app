package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;

/**
 * 衝突検知カウントダウン.
 */
public class ImpactDetectionCountDown {
    private static final int COUNTDOWN_TIME = 30;

    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ
     */
    @Inject
    public ImpactDetectionCountDown(){
    }

    /**
     * 開始.
     */
    public void start(){
        mHandler.post(() -> mCarDevice.impactDetectionCountdown(COUNTDOWN_TIME));
    }

    /**
     * 終了.
     */
    public void finish(){
        mHandler.post(() -> mCarDevice.impactDetectionCountdown(0));
    }
}
