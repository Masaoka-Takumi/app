package jp.pioneer.mbg.alexa.AlexaInterface.directive.System;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class ResetUserInactivityItem extends AlexaIfSystemItem {

//    public String messageId;

    public ResetUserInactivityItem(){
        super(Constant.DIRECTIVE_RESET_USER_INACTIVITY);
    }

    public ResetUserInactivityItem(String messageId){
        super(Constant.DIRECTIVE_RESET_USER_INACTIVITY);
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