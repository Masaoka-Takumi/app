package jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechRecognizer;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class ExpectSpeechTimedOutItem extends AlexaIfSpeechRecognizerItem {
    public ExpectSpeechTimedOutItem(){
        super(Constant.EVENT_EXPECT_SPEECH_TIMEOUT);
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