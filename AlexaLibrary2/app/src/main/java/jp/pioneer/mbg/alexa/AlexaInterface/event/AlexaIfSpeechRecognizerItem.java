package jp.pioneer.mbg.alexa.AlexaInterface.event;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfSpeechRecognizerItem extends AlexaIfEventItem {
    public AlexaIfSpeechRecognizerItem(String name){
        super(Constant.INTERFACE_SPEECH_RECOGNIZER);
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