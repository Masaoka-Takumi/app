package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;

import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ADAS警告イベント群更新.
 * <p>
 * デバッグ設定用
 */
public class UpdateWarningEvents {
    @Inject CarDevice mCarDevice;

    /**
     * コンストラクタ.
     */
    @Inject
    public UpdateWarningEvents(){

    }

    /**
     * 実行.
     *
     * @param warningEvents イベント群
     * @throws NullPointerException {@code warningEvents}がnull
     */
    public void execute(@NonNull Set<AdasWarningEvent> warningEvents){
        Timber.i("execute() warningEvents = %s", warningEvents);
        checkNotNull(warningEvents);

        mCarDevice.updateAdasWarningStatus(warningEvents);
    }
}
