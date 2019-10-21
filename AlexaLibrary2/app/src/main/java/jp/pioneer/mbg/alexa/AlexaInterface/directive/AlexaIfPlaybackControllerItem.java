package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfPlaybackControllerItem extends AlexaIfDirectiveItem {
    public AlexaIfPlaybackControllerItem(String name){
        super(Constant.INTERFACE_PLAYBACK_CONTROLLER);
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