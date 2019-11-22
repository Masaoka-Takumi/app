package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlexaSplashView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * AlexaSplashPresenter
 */
@PresenterLifeCycle
public class AlexaSplashPresenter extends Presenter<AlexaSplashView> {
    @Inject Context mContext;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject
    public AlexaSplashPresenter() {
    }

    public void onLoginSuccess(){
        StatusHolder holder = mGetStatusHolder.execute();
        holder.getAppStatus().alexaAuthenticated = true;
        int vCode = mPreference.getAlexaCapabilitiesVersionCode();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (vCode < MainPresenter.ALEXA_CAPABILITIES_NEW_VERSION) {
                mPreference.setAlexaCapabilitiesSend(false);
                view.setAlexaCapabilities();
                mPreference.setAlexaCapabilitiesVersionCode(MainPresenter.ALEXA_CAPABILITIES_NEW_VERSION);
            }else{
                if (!mPreference.isAlexaCapabilitiesSend()) {
                    view.setAlexaCapabilities();
                }
            }
        });
        mPreference.setVoiceRecognitionType(VoiceRecognizeType.ALEXA);
        mEventBus.post(new NavigateEvent(ScreenId.ALEXA_EXAMPLE_USAGE, createSettingsParams(mContext.getString(R.string.set_318))));
    }

    public void onCapabilitiesSendSuccess(){
        mPreference.setAlexaCapabilitiesSend(true);
    }

    /**
     * Back押下アクション
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    /**
     * Close押下アクション
     */
    public void onCloseAction() {
        if (mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED) {
            mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER));
        } else {
            mEventBus.post(new NavigateEvent(ScreenId.UNCONNECTED_CONTAINER));
        }
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
