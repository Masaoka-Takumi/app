package jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfNotificationsItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class ClearIndicatorItem extends AlexaIfNotificationsItem {

//    public String messageId;

    public ClearIndicatorItem(){
        super(Constant.DIRECTIVE_CLEAR_INDICATOR);
    }

    public ClearIndicatorItem(String messageId){
        super(Constant.DIRECTIVE_CLEAR_INDICATOR);
        this.messageId = messageId;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(messageId);
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
    }
}