package jp.pioneer.mbg.alexa.AlexaInterface.event.PlaybackController;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfPlaybackControllerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class NextCommandIssuedItem extends AlexaIfPlaybackControllerItem {

    public NextCommandIssuedItem(){
        super(Constant.EVENT_NEXT_COMMAND_ISSUED);
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