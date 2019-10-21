package jp.pioneer.carsync.presentation.event;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * ダイアログクローズイベント.
 * <p>
 * 表示されているダイアログを閉じるためのイベント
 */
public class CloseDialogEvent {
    public final ScreenId screenId;

    /**
     * コンストラクタ.
     *
     * @param screenId ScreenId
     */
    public CloseDialogEvent(ScreenId screenId) {
        this.screenId = screenId;
    }
}
