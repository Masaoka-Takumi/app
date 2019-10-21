package jp.pioneer.carsync.presentation.model;

import android.support.annotation.DrawableRes;

import jp.pioneer.carsync.domain.model.SoundEffectType;

/**
 * Created by NSW00_008316 on 2017/10/10.
 */

public class VisualEffectItem {
    public SoundEffectType type;
    @DrawableRes public int resourceId;

    public VisualEffectItem() {
        this.type = SoundEffectType.OFF;
        this.resourceId = 0;
    }

    public VisualEffectItem(SoundEffectType type, int resourceId) {
        this.type = type;
        this.resourceId = resourceId;
    }
}
