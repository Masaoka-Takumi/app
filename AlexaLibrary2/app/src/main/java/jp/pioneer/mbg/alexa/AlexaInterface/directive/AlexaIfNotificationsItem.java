package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfNotificationsItem extends AlexaIfDirectiveItem {
    public AlexaIfNotificationsItem(String name){
        super(Constant.INTERFACE_NOTIFICATIONS);
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