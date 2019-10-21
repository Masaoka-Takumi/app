package jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfAlertsItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class AlertStartedItem extends AlexaIfAlertsItem {

    private String token;

    public AlertStartedItem(String token){
        super(Constant.EVENT_ALERT_STARTED);
        this.token = token;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(UUID.randomUUID().toString());
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        this.payLoad.setToken(token);
    }
}