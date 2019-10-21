package jp.pioneer.carsync.presentation.view;

/**
 * Created by NSW00_007906 on 2018/09/28.
 */

public interface AlexaView {

    /**
     * ダイアログ終了
     */
    void callbackClose();

    void setNotificationQueuedState(boolean notification);

    void setVoiceCommand();
}
