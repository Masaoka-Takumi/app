package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.ReadingRequestType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 通知読み上げ準備.
 * <p>
 * 通知を読み上げる前に{@link #start()}を実行する。
 * 通知読み上げが終了したら{@link #finish()}を実行する。
 * <p>
 * 読み上げが有効か否かは本クラスではチェックせず、呼び出し元でチェックする
 */
public class PrepareReadNotification {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ.
     */
    @Inject
    public PrepareReadNotification(){
    }

    /**
     * 開始.
     *
     * 本メソッドを実行するとTTSソース({@link MediaSourceType#TTS})に切り替わるため、
     * 切り替わったら通知読み上げを実施する。
     * ※着信等によりTTSソースに切り替わらないことがあるため、切り替わったことをチェックする必要がある。
     */
    public void start(){
        mHandler.post(() -> {
            if(mStatusHolder.getCarDeviceStatus().sourceType != MediaSourceType.TTS) {
                mCarDevice.requestReadNotification(ReadingRequestType.START);
            }
        });
    }

    /**
     * 終了.
     */
    public void finish(){
        mHandler.post(() -> {
            if(mStatusHolder.getCarDeviceStatus().sourceType == MediaSourceType.TTS) {
                mCarDevice.requestReadNotification(ReadingRequestType.FINISH);
            }
        });
    }
}
