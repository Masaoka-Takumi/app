package jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechRecognizer;

import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class ExpectSpeechItem extends AlexaIfSpeechRecognizerItem {

//    public String messageId;
//    public String dialogRequestId;
    public Long timeoutInMilliseconds;
//    public String initiator;          // 2018.03.30 API仕様変更対応
    public Initiator initiator;

    public ExpectSpeechItem(){
        super(Constant.DIRECTIVE_EXPECT_SPEECH);
    }

//    public ExpectSpeechItem(String messageId, String dialogRequestId, Long timeoutInMilliseconds, String initiator){
//        super(Constant.DIRECTIVE_EXPECT_SPEECH);
//        this.messageId = messageId;
//        this.dialogRequestId = dialogRequestId;
//        this.timeoutInMilliseconds = timeoutInMilliseconds;
//        this.initiator = initiator;
//    }
    public ExpectSpeechItem(String messageId, String dialogRequestId, Long timeoutInMilliseconds, Initiator initiator){
        super(Constant.DIRECTIVE_EXPECT_SPEECH);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.initiator = initiator;
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
        payLoad.setTimeoutInMilliseconds(timeoutInMilliseconds);
        // TODO:使用ケースが無いので、現時点では未実装
//        payLoad.setInitiator(initiator);
    }

}