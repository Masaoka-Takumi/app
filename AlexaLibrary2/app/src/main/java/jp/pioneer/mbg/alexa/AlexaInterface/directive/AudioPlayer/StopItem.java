package jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class StopItem extends AlexaIfAudioPlayerItem {

//    public String messageId;
//    public String dialogRequestId;

    public StopItem(){
        super(Constant.DIRECTIVE_STOP);
    }

    public StopItem(String messageId, String dialogRequestId){
        super(Constant.DIRECTIVE_STOP);
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