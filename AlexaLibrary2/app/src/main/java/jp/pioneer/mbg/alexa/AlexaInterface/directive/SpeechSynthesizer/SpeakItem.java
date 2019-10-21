package jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AlexaIfSpeechSynthesizerItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.IAlexaAudioInterface;
import jp.pioneer.mbg.alexa.util.Constant;

import java.io.File;
import java.io.FileDescriptor;

public class SpeakItem extends AlexaIfSpeechSynthesizerItem implements IAlexaAudioInterface {

    public String url;
    public String format;
    public String token;
    private byte[] audioContent = null;

    public SpeakItem(){
        super(Constant.DIRECTIVE_SPEAK);
    }

    public SpeakItem(String messageId, String dialogRequestId, String url, String format, String token){
        super(Constant.DIRECTIVE_SPEAK);
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
        this.url = url;
        this.format = format;
        this.token = token;
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
        payLoad.setUrl(url);
        payLoad.setFormat(format);
        payLoad.setToken(token);
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