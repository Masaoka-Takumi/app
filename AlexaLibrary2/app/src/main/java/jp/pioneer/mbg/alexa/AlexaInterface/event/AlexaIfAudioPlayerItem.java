package jp.pioneer.mbg.alexa.AlexaInterface.event;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfAudioPlayerItem extends AlexaIfEventItem {
    public AlexaIfAudioPlayerItem(String name){
        super(Constant.INTERFACE_AUDIO_PLAYER);
        this.name = name;
    }

    @Override
    protected void setHeader() {
        super.setHeader();
    }

    @Override
    protected void setPayLoad() {
        super.setPayLoad();
    }
}