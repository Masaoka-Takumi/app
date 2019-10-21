package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfTemplateRuntimeItem extends AlexaIfDirectiveItem {
    public AlexaIfTemplateRuntimeItem(String name){
        super(Constant.INTERFACE_TEMPLATE_RUNTIME);
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