package jp.pioneer.carsync.presentation.event;

import android.os.Bundle;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by BP06565 on 2017/02/15.
 */

public class NavigateEvent {
    public final ScreenId screenId;
    public final Bundle args;

    public NavigateEvent(ScreenId screenId) {
        this(screenId, Bundle.EMPTY);
    }

    public NavigateEvent(ScreenId screenId, Bundle args) {
        this.screenId = screenId;
        this.args = args;
    }
}
