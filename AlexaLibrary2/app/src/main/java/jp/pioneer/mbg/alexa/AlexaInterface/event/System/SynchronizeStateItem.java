package jp.pioneer.mbg.alexa.AlexaInterface.event.System;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class SynchronizeStateItem extends AlexaIfSystemItem {
    public SynchronizeStateItem(){
        super(Constant.EVENT_SYNCHRONIZE_STATE);
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(UUID.randomUUID().toString());
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();
        this.payLoad = new PayLoad();
    }
}