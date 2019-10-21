package jp.pioneer.carsync.presentation.view;

import android.os.Bundle;

import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by BP06566 on 2017/03/03.
 */

public interface PlayerContainerView extends OnNavigateListener {
    void navigate(ScreenId screenId, Bundle args);

    /**
     * SRC更新時メッセージの表示
     */
    void displaySrcMessage(String str);
    void displayVoiceMessage(String str);
}
