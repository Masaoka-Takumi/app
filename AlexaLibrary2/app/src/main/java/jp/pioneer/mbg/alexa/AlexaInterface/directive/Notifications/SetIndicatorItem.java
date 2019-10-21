package jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfNotificationsItem;
import jp.pioneer.mbg.alexa.util.Constant;

public class SetIndicatorItem extends AlexaIfNotificationsItem {

//    public String messageId;
    public Boolean persistVisualIndicator;
    public Boolean playAudioIndicator;
    public Asset asset;

    public SetIndicatorItem(){
        super(Constant.DIRECTIVE_SET_INDICATOR);
    }

    public SetIndicatorItem(String messageId, Asset asset, Boolean persistVisualIndicator, Boolean playAudioIndicator){
        super(Constant.DIRECTIVE_SET_INDICATOR);
        this.messageId = messageId;
        this.persistVisualIndicator = persistVisualIndicator;
        this.playAudioIndicator = playAudioIndicator;
        this.asset = asset;
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(messageId);
    }
    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        payLoad.setAsset(asset);
        payLoad.setPersistVisualIndicator(persistVisualIndicator);
        payLoad.setPlayAudioIndicator(playAudioIndicator);
    }
}