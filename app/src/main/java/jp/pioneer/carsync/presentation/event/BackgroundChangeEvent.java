package jp.pioneer.carsync.presentation.event;

/**
 * 背景変更通知イベント
 * <p>
 * ぼかし処理を変更する際に発行するイベント
 */

public class BackgroundChangeEvent {
    public boolean isBlur = false;

    public BackgroundChangeEvent(boolean isBlur) {
        this.isBlur = isBlur;
    }
}
