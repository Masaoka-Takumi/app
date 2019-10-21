package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.model.ShortcutKey;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ソフトウェアショートカットキーアクション.
 */
public class ActionSoftwareShortcutKey {
    @Inject EventBus mEventBus;

    /**
     * コンストラクタ.
     */
    @Inject
    public ActionSoftwareShortcutKey() {
    }

    /**
     * 実行.
     *
     * @param key ショートカットキー
     * @throws NullPointerException {@code key}がnull
     */
    public void execute(@NonNull ShortcutKey key) {
        checkNotNull(key);

        mEventBus.post(new ShortcutKeyEvent(key));
    }
}
