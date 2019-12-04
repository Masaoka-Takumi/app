package jp.pioneer.carsync.presentation.event;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class MainNavigateEvent {
    public final ScreenId screenId;

    public MainNavigateEvent(ScreenId screenId) {
        this.screenId = screenId;
    }
}
