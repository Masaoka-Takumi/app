package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.view.AlexaDisplayCardView;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;

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
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        RenderTemplateItem item = appStatus.renderTemplateItem;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTemplate(item);
        });
    }

    public void startAlexa() {
        //mEventBus.post(new NavigateEvent(ScreenId.ALEXA, Bundle.EMPTY));
        mShortcutCase.execute(ShortcutKey.VOICE);
        Optional.ofNullable(getView()).ifPresent(AlexaDisplayCardView::callbackClose);
    }

}
