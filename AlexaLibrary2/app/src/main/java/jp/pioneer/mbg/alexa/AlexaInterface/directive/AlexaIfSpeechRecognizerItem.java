package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class AlexaIfSpeechRecognizerItem extends AlexaIfDirectiveItem {
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