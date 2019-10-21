package jp.pioneer.mbg.alexa.manager.callback;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;

/**
 * Created by esft-sakamori on 2017/10/04.
 */

/**
 * AlexaSpeakManager、及び、AlexaAudioManagerから音楽再生の状態を通知するためのコールバック
 */
public interface IAudioCallback {

    public void onPrepare(AlexaIfItem item);
    public void onPrepared(AlexaIfItem item);
    public void onPlay(AlexaIfItem item);
    public void onPause(AlexaIfItem item);
    public void onResume(AlexaIfItem item);
    public void onStop(AlexaIfItem item);
    public void onComplete(AlexaIfItem item);
    public void onError(AlexaIfItem item);
    public void onUpdateProgress(AlexaIfItem item, long position);
    public void onAdjustVolume(float volume);
    public void onSetVolume(float volume);
    public void onSetMute(boolean isMute);
    public boolean isAlexaPlayable();
    public void onNoResponse();
    public void onNoDirectiveAtSendEventResponse();
    public void onDecodeStart();
    public void onDecodeFinish();
    public void onWLAudioFocusLoss();
}
