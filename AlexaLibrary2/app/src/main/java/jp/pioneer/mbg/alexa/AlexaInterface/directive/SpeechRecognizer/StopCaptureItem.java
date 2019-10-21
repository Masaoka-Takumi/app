package jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class StopCaptureItem extends AlexaIfSpeechRecognizerItem {

//    public String messageId;
//    public String dialogRequestId;

    public StopCaptureItem(){
        super(Constant.DIRECTIVE_STOP_CAPTURE);
    }

    public StopCaptureItem(String messageId, String dialogRequestId){
        super(Constant.DIRECTIVE_STOP_CAPTURE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(messageId);
        this.header.setDialogRequestId(dialogRequestId);
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
    }
}