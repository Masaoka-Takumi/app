package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.view.AlexaDisplayCardView;
import jp.pioneer.carsync.presentation.view.AlexaView;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import timber.log.Timber;

/**
 * AlexaDisplayCardPresenter
 */
@PresenterLifeCycle
public class AlexaDisplayCardPresenter extends Presenter<AlexaDisplayCardView> {
    @Inject
    GetStatusHolder mGetCase;
    @Inject
    EventBus mEventBus;
    @Inject
    ActionSoftwareShortcutKey mShortcutCase;

    @Inject
    public AlexaDisplayCardPresenter() {
    }

    @Override
    void onResume() {
        Timber.d("onResume");
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isShowAlexaDisplayCardDialog = true;
        RenderTemplateItem item = appStatus.renderTemplateItem;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTemplate(item);
        });
    }

    @Override
    void onPause() {
        Timber.d("onPause");
        mEventBus.unregister(this);
    }

    @Override
    void onDestroy() {
        Timber.d("onDestroy");
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isShowAlexaDisplayCardDialog = false;
    }

    public void startAlexa() {
        mShortcutCase.execute(ShortcutKey.VOICE);
    }

    public void dismissDialog() {
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.renderTemplateItem = null;
        Optional.ofNullable(getView()).ifPresent(AlexaDisplayCardView::callbackClose);
    }

    /**
     * MediaSourceTypeChangeEvent
     *
     * @param event MediaSourceTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent event) {
        MediaSourceType currentSourceType = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (currentSourceType != MediaSourceType.APP_MUSIC) {
            Optional.ofNullable(getView()).ifPresent(AlexaDisplayCardView::callbackClose);
        }
    }
}
