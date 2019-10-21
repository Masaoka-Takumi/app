package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.util.Constant;

/**
 * Created by NSW00_007906 on 2018/12/11.
 */

public class AlexaIfNavigationItem extends AlexaIfDirectiveItem {
    public AlexaIfNavigationItem(String name){
        super(Constant.INTERFACE_NAVIGATION);
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