package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.domain.model.SxmBandType;

/**
 * Created by NSW00_007906 on 2018/01/30.
 */

public class SxmPresetItem extends AbstractPresetItem{
    public SxmBandType bandType;

    public SxmPresetItem() {
        this.bandType = SxmBandType.SXM1;
        this.presetNumber = 0;
        this.channelName = "";
        this.frequencyText = "";
        this.selected = false;
    }

    public SxmPresetItem(SxmBandType bandType, int presetNumber, String channelName, String frequencyText, boolean selected) {
        this.bandType = bandType;
        this.presetNumber = presetNumber;
        this.channelName = channelName;
        this.frequencyText = frequencyText;
        this.selected = selected;
    }
}
