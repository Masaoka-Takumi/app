package jp.pioneer.mbg.alexa.AlexaInterface.event.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class StreamMetadataExtractedItem extends AlexaIfAudioPlayerItem {

    private String token;
    private MetaData metadata;

    public StreamMetadataExtractedItem(String token, MetaData metaData){
        super(Constant.EVENT_STREAM_METADATA_EXTRACTED);
        this.token = token;
        this.metadata = metaData;
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
        this.payLoad.setMetadata(metadata);
    }
}