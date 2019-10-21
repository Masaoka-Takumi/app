package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_008316 on 2017/03/23.
 */

public interface SettingsContainerView extends OnNavigateListener {

    void setPass(String pass);

    void updateCloseButton();

    void updateBackButton();

    void updateNextButton();

    void updateCaution();

    void updateNavigateBar();

    void updateOtherButton();

    ScreenId getCurrentScreenId();
}
