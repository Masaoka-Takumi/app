package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;

/**
 * SoundFxItem
 */

public class SoundFxItem {
    public ItemType type;//0:OFF, 1:LiveSimulation, 2:Todoroki
    public SoundFieldControlSettingType soundFieldControlSetting;
    public SuperTodorokiSetting superTodorokiSetting;

    public enum ItemType{
        OFF,
        LIVE_SIMULATION,
        TODOROKI
    }

    public SoundFxItem() {
        this.type = ItemType.OFF;
        this.soundFieldControlSetting = SoundFieldControlSettingType.OFF;
        this.superTodorokiSetting = SuperTodorokiSetting.OFF;
    }

    public SoundFxItem(ItemType type, SoundFieldControlSettingType soundFieldControlSetting, SuperTodorokiSetting superTodorokiSetting) {
        this.type = type;
        this.soundFieldControlSetting = soundFieldControlSetting;
        this.superTodorokiSetting = superTodorokiSetting;
    }
}
