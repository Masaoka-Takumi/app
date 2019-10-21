package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;

/**
 * メニュー表示解除通知.
 * <p>
 * アプリの設定画面を開く時に使用する
 */
public class ExitMenu {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ.
     */
    @Inject
    public ExitMenu(){
    }

    /**
     * 実行.
     */
    public void execute(){
        mHandler.post(() -> mCarDevice.exitMenu());
    }
}
