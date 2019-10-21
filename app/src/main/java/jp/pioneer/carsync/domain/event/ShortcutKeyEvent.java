package jp.pioneer.carsync.domain.event;

import jp.pioneer.carsync.domain.model.ShortcutKey;

/**
 * ショートカットキーイベント.
 * <p>
 * 車載機のハードキー、または、アプリのソフトウェアショートカットキーが押下された場合に発生する。
 */
public class ShortcutKeyEvent {
    /** ショートカットキー. */
    public ShortcutKey shortcutKey;

    /**
     * コンストラクタ.
     *
     * @param shortcutKey ショートカットキー
     */
    public ShortcutKeyEvent(ShortcutKey shortcutKey) {
        this.shortcutKey = shortcutKey;
    }
}
