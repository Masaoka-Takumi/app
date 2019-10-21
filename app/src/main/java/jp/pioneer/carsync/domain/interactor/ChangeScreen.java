package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.TransitionDirection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機画面切替.
 */
public class ChangeScreen {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ
     */
    @Inject
    public ChangeScreen(){
    }

    /**
     * 実行.
     *
     * @param screen 車載機画面種別
     * @param direction 遷移方向
     * @throws NullPointerException {@code screen}がnull
     * @throws NullPointerException {@code direction}がnull
     */
    public void execute(@NonNull CarDeviceScreen screen, @NonNull TransitionDirection direction){
        checkNotNull(screen);
        checkNotNull(direction);

        mHandler.post(() -> mCarDevice.changeScreen(screen, direction));
    }
}
