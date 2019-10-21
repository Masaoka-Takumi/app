package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.view.SessionStoppedDialogView;

/**
 * 車載機切断ダイアログのpresenter.
 */
public class SessionStoppedDialogPresenter extends Presenter<SessionStoppedDialogView> {
    /**
     * コンストラクタ.
     */
    @Inject
    public SessionStoppedDialogPresenter() {
    }

    /**
     * 確認ボタン押下イベント.
     */
    public void onConfirmAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.onScreenOff();
            view.callbackClose();
        });
    }
}
