package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 非接続時画面のContainerView.
 */
public interface UnconnectedContainerView extends OnNavigateListener {
    void setBillingHelper();
    void showAlexaAvailableConfirmDialog();
    ScreenId getScreenIdInContainer();
}
