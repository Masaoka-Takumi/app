package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfSystemItem extends AlexaIfDirectiveItem {
    public AlexaIfSystemItem(String name){
        super(Constant.INTERFACE_SYSTEM);
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