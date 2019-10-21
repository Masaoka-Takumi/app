package jp.pioneer.mbg.alexa.AlexaInterface.directive.System;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class SetEndpointItem extends AlexaIfSystemItem {

//    private String messageId;
    public String endpoint;

    public SetEndpointItem(){
        super(Constant.DIRECTIVE_SET_ENDPOINT);
    }

    public SetEndpointItem(String messageId, String endpoint){
        super(Constant.DIRECTIVE_SET_ENDPOINT);
        this.messageId = messageId;
        this.endpoint = endpoint;
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
        payLoad.setEndpoint(endpoint);
    }
}