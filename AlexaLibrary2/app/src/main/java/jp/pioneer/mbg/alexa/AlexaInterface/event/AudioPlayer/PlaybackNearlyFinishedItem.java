package jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class PlaybackNearlyFinishedItem extends AlexaIfAudioPlayerItem {

    private String token;
    private long offsetInMilliseconds;

    public PlaybackNearlyFinishedItem(String token, long offsetInMilliseconds){
        super(Constant.EVENT_PLAYBACK_NEARLY_FINISHED);
        this.token = token;
        this.offsetInMilliseconds = offsetInMilliseconds;
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
        this.payLoad.setOffsetInMilliseconds(offsetInMilliseconds);
    }
}