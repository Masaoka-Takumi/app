package jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechSynthesizer;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechSynthesizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class SpeechStartedItem extends AlexaIfSpeechSynthesizerItem {
    private String token;

    public SpeechStartedItem(String token){
        super(Constant.EVENT_SPEECH_STARTED);
        this.token = token;
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
        this.payLoad.setToken(token);
    }
}