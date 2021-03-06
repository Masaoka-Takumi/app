package jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfPlaybackControllerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSettingsItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class PlayCommandIssuedItem extends AlexaIfPlaybackControllerItem {

    public PlayCommandIssuedItem(){
        super(Constant.EVENT_PLAY_COMMAND_ISSUED);
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
    }
}