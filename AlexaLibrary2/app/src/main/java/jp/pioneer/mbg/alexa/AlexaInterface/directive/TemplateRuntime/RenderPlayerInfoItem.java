package jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfTemplateRuntimeItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.List;

public class RenderPlayerInfoItem extends AlexaIfTemplateRuntimeItem {

//    public String messageId;
//    public String dialogRequestId;
    public String audioItemId;
    public Content content;
    public List<Control> controls;

    public RenderPlayerInfoItem(){
        super(Constant.DIRECTIVE_RENDER_PLAYER_INFO);
    }

    public RenderPlayerInfoItem(String messageId, String endpoint, String dialogRequestId, String audioItemId, Content content, List<Control> controls){
        super(Constant.DIRECTIVE_RENDER_PLAYER_INFO);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.audioItemId = audioItemId;
        this.content = content;
        this.controls = controls;
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
        payLoad.setAudioItemId(audioItemId);
        payLoad.setContent(content);
        payLoad.setControls(controls);
    }
}