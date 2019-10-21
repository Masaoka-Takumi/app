package jp.pioneer.mbg.alexa.AlexaInterface.event.Speaker;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeakerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class MuteChangedItem extends AlexaIfSpeakerItem {
    private long volumed;
    private boolean mute;

    public MuteChangedItem(long volumed, boolean mute){
        super(Constant.EVENT_MUTE_CHANGED);
        this.volumed = volumed;
        this.mute = mute;
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
        this.payLoad.setVolume(volumed);
        this.payLoad.setMuted(mute);
    }
}