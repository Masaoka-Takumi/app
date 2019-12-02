package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AlexaNotificationChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.presentation.event.AlexaVoiceRecognizeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlexaView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;

/**
 * AlexaPresenter
 */
@PresenterLifeCycle
public class AlexaPresenter extends Presenter<AlexaView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject ControlSource mControlSource;
    @Inject AnalyticsEventManager mAnalytics;
    @Inject
    public AlexaPresenter() {
    }

    @Override
    void onResume() {
        super.onResume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        AppStatus appStatus  = mGetCase.execute().getAppStatus();
        appStatus.isShowAlexaDialog = true;
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setNotificationQueuedState(mGetCase.execute().getAppStatus().alexaNotification);
        });
    }

    @Override
    void onPause() {
        super.onPause();
        mEventBus.unregister(this);
        AppStatus appStatus  = mGetCase.execute().getAppStatus();
        appStatus.isShowAlexaDialog = false;
    }

    public void onAudioPlay(){
        AppStatus appStatus  = mGetCase.execute().getAppStatus();
        appStatus.appMusicAudioMode = AudioMode.ALEXA;
        mEventBus.post(new AppMusicAudioModeChangeEvent());
        mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER, Bundle.EMPTY));
        Optional.ofNullable(getView()).ifPresent(AlexaView::callbackClose);
    }

    public void setPlayInfo(RenderPlayerInfoItem playerInfoItem){
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.playerInfoItem = playerInfoItem;
    }

    public void changePreviousSource(){
        AppStatus appStatus = mGetCase.execute().getAppStatus();
       if(appStatus.alexaPreviousSourceType != MediaSourceType.APP_MUSIC){
           mControlSource.selectSource(appStatus.alexaPreviousSourceType);
           mAnalytics.setSourceSelectReason(Analytics.SourceChangeReason.alexaEnd);
       }
    }

    /**
     * AlexaNotificationChangeEvent
     * @param event AlexaNotificationChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlexaNotificationChangeEvent(AlexaNotificationChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setNotificationQueuedState(mGetCase.execute().getAppStatus().alexaNotification);
        });
    }

    /**
     * MediaSourceTypeChangeEvent
     * @param event MediaSourceTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent event) {
        MediaSourceType currentSourceType = mGetCase.execute().getCarDeviceStatus().sourceType;
        if(currentSourceType!=MediaSourceType.APP_MUSIC) {
            Optional.ofNullable(getView()).ifPresent(AlexaView::callbackClose);
        }
    }

    /**
     * AlexaVoiceRecognizeEvent
     * @param event AlexaVoiceRecognizeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlexaVoiceRecognizeEvent(AlexaVoiceRecognizeEvent event) {
        Optional.ofNullable(getView()).ifPresent(AlexaView::setVoiceCommand);
    }
}
