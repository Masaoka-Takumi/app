package jp.pioneer.carsync.presentation.util;

import android.os.Bundle;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

public class YouTubeLinkActionHandler {

    @Inject AppSharedPreference mPreference;
    @Inject EventBus mEventBus;

    @Inject
    public YouTubeLinkActionHandler() {
    }

    public void execute(){
        mEventBus.post(new NavigateEvent(ScreenId.YOUTUBE_LINK_CONTAINER, Bundle.EMPTY));
    }
}
