package jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class ClearQueueItem extends AlexaIfAudioPlayerItem {

//    public String messageId;
//    public String dialogRequestId;
    public String clearBehavior;

    public ClearQueueItem(){
        super(Constant.DIRECTIVE_CLEAR_QUEUE);
    }

    public ClearQueueItem(String messageId, String dialogRequestId, String clearBehavior){
        super(Constant.DIRECTIVE_CLEAR_QUEUE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.clearBehavior = clearBehavior;
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
        payLoad.setClearBehavior(clearBehavior);
    }
}