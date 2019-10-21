package jp.pioneer.mbg.alexa.AlexaInterface.event;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfAlertsItem extends AlexaIfEventItem {
    public AlexaIfAlertsItem(String name){
        super(Constant.INTERFACE_ALERTS);
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