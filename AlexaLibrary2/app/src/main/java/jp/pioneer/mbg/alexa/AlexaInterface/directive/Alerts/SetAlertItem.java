package jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAlertsItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechSynthesizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.List;

public class SetAlertItem extends AlexaIfAlertsItem {
    public long alertId;

//    public String messageId;
//    public String dialogRequestId;
    public String token;
    public String type;
    public String scheduledTime;
    public List<Asset> assets;
    public List<String> assetPlayOrder;
    public String backgroundAlertAsset;
    public Long loopCount;
    public Long loopPauseInMilliseconds;

    public SetAlertItem(){
        super(Constant.DIRECTIVE_SET_ALERT);
    }

    public SetAlertItem(String messageId, String dialogRequestId, String token, String type, String scheduledTime, List<Asset> assets, List<String> assetPlayOrder, String backgroundAlertAsset, Long loopCount, Long loopPauseInMilliseconds){
        super(Constant.DIRECTIVE_SET_ALERT);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.token = token;
        this.type = type;
        this.scheduledTime = scheduledTime;
        this.assets = assets;
        this.assetPlayOrder = assetPlayOrder;
        this.backgroundAlertAsset = backgroundAlertAsset;
        this.loopCount = loopCount;
        this.loopPauseInMilliseconds = loopPauseInMilliseconds;
    }
    public void setAlertId(long alertId){
        this.alertId = alertId;
    }
    public long getAlertId(){
        return alertId;
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
        payLoad.setType(type);
        payLoad.setScheduledTime(scheduledTime);
        payLoad.setAssets(assets);
        payLoad.setAssetPlayOrder(assetPlayOrder);
        payLoad.setBackgroundAlertAsset(backgroundAlertAsset);
        payLoad.setLoopCount(loopCount);
        payLoad.setLoopPauseInMilliseconds(loopPauseInMilliseconds);
    }
}