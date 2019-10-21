package jp.pioneer.mbg.alexa.AlexaInterface.directive.Speaker;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfNotificationsItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class SetVolumeItem extends AlexaIfSpeakerItem {

//    public String messageId;
//    public String dialogRequestId;
    public Long volume;

    public SetVolumeItem(){
        super(Constant.DIRECTIVE_SET_VOLUME);
    }

    public SetVolumeItem(String messageId, String dialogRequestId, Long volume){
        super(Constant.DIRECTIVE_SET_VOLUME);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.volume = volume;
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
        payLoad.setVolume(volume);
    }
}