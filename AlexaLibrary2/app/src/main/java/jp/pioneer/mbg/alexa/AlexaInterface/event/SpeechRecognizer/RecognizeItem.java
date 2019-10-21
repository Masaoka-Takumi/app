package jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechRecognizer;

import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;
import jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfSpeechRecognizerItem;
import jp.pioneer.mbg.alexa.util.Constant;

import java.util.UUID;

public class RecognizeItem extends AlexaIfSpeechRecognizerItem {

    public Initiator initiator;

    public RecognizeItem(Initiator initiator){
        super(Constant.EVENT_RECOGNIZE);
        this.initiator = initiator;
        this.dialogRequestId = UUID.randomUUID().toString();
    }

    @Override
    protected void setHeader() {
        super.setHeader();

        this.header = new Header();
        this.header.setNamespace(namespace);
        this.header.setName(name);
        this.header.setMessageId(UUID.randomUUID().toString());
//        this.header.setDialogRequestId("dialogRequestId-321");
        this.header.setDialogRequestId(this.dialogRequestId);
    }

    @Override
    protected void setPayLoad() {
        super.setPayLoad();

        this.payLoad = new PayLoad();
        // 距離の設定."CLOSE_TALK", "NEAR_FIELD", "FAR_FIELD".から選択.
//                this.payLoad.put(PayLoad.PROFILE, PayLoad.CLOSE_TALK);
        this.payLoad.setProfile("NEAR_FIELD");
        // AUDIO_FORMATはおそらく固定.
        this.payLoad.setFormat(Constant.AUDIO_FORMAT);
        this.payLoad.setInitiator(this.initiator);
    }

}