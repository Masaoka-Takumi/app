package jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class ProgressReportDelayElapsedItem extends AlexaIfAudioPlayerItem {

    private String token;
    private long offsetInMilliseconds;

    public ProgressReportDelayElapsedItem(String token, long offsetInMilliseconds){
        super(Constant.EVENT_PROGRESS_REPORT_DELAY_ELAPSED);
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