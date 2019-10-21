package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ダイレクトコール.
 */
public class DirectCall {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ.
     */
    @Inject
    public DirectCall(){
    }

    /**
     * 実行.
     *
     * @param number 電話番号
     * @throws NullPointerException {@code number}がnull
     */
    public void execute(@NonNull String number){
        checkNotNull(number);

        mHandler.post(() -> mCarDevice.phoneCall(number.replace("-", "").replace(" ", "")));
    }
}
