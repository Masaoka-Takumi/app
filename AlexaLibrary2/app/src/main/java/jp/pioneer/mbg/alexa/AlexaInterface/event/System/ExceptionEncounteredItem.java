package jp.pioneer.mbg.alexa.AlexaInterface.event.System;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSystemItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class ExceptionEncounteredItem extends AlexaIfSystemItem {

    private String unparsedDirective;
    private Error error;

    public ExceptionEncounteredItem(String unparsedDirective, Error error){
        super(Constant.EVENT_EXCEPTION_ENCOUNTERED);
        this.unparsedDirective = unparsedDirective;
        this.error = error;
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
        this.payLoad.setUnparsedDirective(unparsedDirective);
        this.payLoad.setError(error);
    }
}