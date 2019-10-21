package jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfAudioPlayerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.IAlexaAudioInterface;
import jp.pioneer.mbg.alexa.util.Constant;

import java.io.FileDescriptor;
import java.util.List;

public class PlayItem extends AlexaIfAudioPlayerItem implements IAlexaAudioInterface {

    public String playBehavior;
    public AlexaIfDirectiveItem.AudioItem audioItem;
    private byte[] audioContent = null;

    public PlayItem(){
        super(Constant.DIRECTIVE_PLAY);
    }

    public PlayItem(String messageId, String dialogRequestId, String playBehavior, AlexaIfDirectiveItem.AudioItem audioItem){
        super(Constant.DIRECTIVE_PLAY);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.playBehavior = playBehavior;
        this.audioItem = audioItem;
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
        payLoad.setPlayBehavior(playBehavior);
        payLoad.setAudioItem(audioItem);
    }

    /**
     * 音声データを取得
     * @return audioContent
     */
    @Override
    public byte[] getAudioContent() {
        return audioContent;
    }

    /**
     * 音声データを設定
     * @param audioContent
     */
    @Override
    public void setAudioContent(byte[] audioContent) {
        this.audioContent = audioContent;
    }

}