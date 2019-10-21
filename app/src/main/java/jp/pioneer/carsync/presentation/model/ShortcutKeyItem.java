package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.ShortcutKey;

/**
 * ShortcutKeyItem
 */

public class ShortcutKeyItem {

    public ShortcutKey key;

    public int imageResource;

    public int optionImageResource;

    public boolean enabled;
    public ShortcutKeyItem() {

        key = null;
        imageResource = 0;
        optionImageResource = 0;
        enabled = true;
    }

    public ShortcutKeyItem(ShortcutKey shortCutKey, int imageResource, int optionImageResource, boolean enabled) {
        this.key = shortCutKey;
        this.imageResource = imageResource;
        this.optionImageResource = optionImageResource;
        this.enabled = enabled;
    }
}
