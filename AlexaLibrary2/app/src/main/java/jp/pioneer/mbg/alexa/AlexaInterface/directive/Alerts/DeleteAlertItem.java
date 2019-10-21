package jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAlertsItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.List;

public class DeleteAlertItem extends AlexaIfAlertsItem {

//    public String messageId;
//    public String dialogRequestId;
    public String token;

    public DeleteAlertItem(){
        super(Constant.DIRECTIVE_DELETE_ALERT);
    }

    public DeleteAlertItem(String messageId, String dialogRequestId, String token){
        super(Constant.DIRECTIVE_DELETE_ALERT);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
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
        payLoad.setToken(token);
    }
}