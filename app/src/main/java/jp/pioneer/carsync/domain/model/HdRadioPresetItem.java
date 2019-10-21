package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.presentation.model.AbstractPresetItem;

public class HdRadioPresetItem extends AbstractPresetItem {
    public HdRadioBandType bandType;

    public HdRadioPresetItem() {
        this.bandType = HdRadioBandType.FM1;
        this.presetNumber = 0;
        this.channelName = "";
        this.frequencyText = "";
        this.selected = false;
    }

    public HdRadioPresetItem(HdRadioBandType bandType, int presetNumber, String channelName, String frequencyText, boolean selected) {
        this.bandType = bandType;
        this.presetNumber = presetNumber;
        this.channelName = channelName;
        this.frequencyText = frequencyText;
        this.selected = selected;
    }
}