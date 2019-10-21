package jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class SetMuteItem extends AlexaIfSpeakerItem {

//    public String messageId;
//    public String dialogRequestId;
    public Boolean mute;

    public SetMuteItem(){
        super(Constant.DIRECTIVE_SET_MUTE);
    }

    public SetMuteItem(String messageId, String dialogRequestId, Boolean mute){
        super(Constant.DIRECTIVE_SET_MUTE);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.mute = mute;
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
        payLoad.setMute(mute);
    }
}