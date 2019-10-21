package jp.pioneer.mbg.alexa.AlexaInterface.directive;

import java.io.FileDescriptor;

/**
 * Created by esft-sakamori on 2017/08/24.
 */

public interface IAlexaAudioInterface {

    /**
     * 音声データを取得
     * @return
     */
    public byte[] getAudioContent();

    /**
     * 音声データを設定
     * @param audioContent
     */
    public void setAudioContent(byte[] audioContent);

}
