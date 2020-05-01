package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.DabBandType;

public class DabPresetItem extends AbstractPresetItem{
    public DabBandType bandType;

    public DabPresetItem() {
        this.bandType = DabBandType.BAND1;
        this.presetNumber = 0;
        this.channelName = "";
        this.frequencyText = "";
        this.selected = false;
    }

    public DabPresetItem(DabBandType bandType, int presetNumber, String channelName, String frequencyText, boolean selected) {
        this.bandType = bandType;
        this.presetNumber = presetNumber;
        this.channelName = channelName;
        this.frequencyText = frequencyText;
        this.selected = selected;
    }
}
