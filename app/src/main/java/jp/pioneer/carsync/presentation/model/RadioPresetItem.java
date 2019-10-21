package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.RadioBandType;

/**
 * Created by NSW00_007906 on 2017/08/21.
 */

public class RadioPresetItem extends AbstractPresetItem{
    public RadioBandType bandType;

    public RadioPresetItem() {
        this.bandType = RadioBandType.AM;
        this.presetNumber = 0;
        this.channelName = "";
        this.frequencyText = "";
        this.selected = false;
    }

    public RadioPresetItem(RadioBandType bandType, int presetNumber, String channelName, String frequencyText, boolean selected) {
        this.bandType = bandType;
        this.presetNumber = presetNumber;
        this.channelName = channelName;
        this.frequencyText = frequencyText;
        this.selected = selected;
    }
}
