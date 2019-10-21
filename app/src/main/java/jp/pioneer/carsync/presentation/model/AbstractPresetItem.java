package jp.pioneer.carsync.presentation.model;

/**
 * Created by NSW00_007906 on 2018/01/30.
 */

public abstract class AbstractPresetItem {
    public int presetNumber;
    public String channelName;
    public String frequencyText;
    public boolean selected;

    public void reset() {
        this.presetNumber = 0;
        this.channelName = "";
        this.frequencyText = "";
        this.selected = false;
    }

}
