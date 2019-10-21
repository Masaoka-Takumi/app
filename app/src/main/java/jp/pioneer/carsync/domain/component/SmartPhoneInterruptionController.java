package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;

/**
 * SmartPhone割り込みコントローラー.
 * <p>
 * 車載機に割り込みの発光パターン登録と割り込み情報を通知する
 */
public interface SmartPhoneInterruptionController {

    /**
     * 割り込み.
     *
     * @param interruption 割り込み
     * @throws NullPointerException {@code interruption}がnull
     */
    void interrupt(@NonNull SmartPhoneInterruption interruption);

    /**
     * 割り込み解除.
     */
    void releaseInterrupt();
}
