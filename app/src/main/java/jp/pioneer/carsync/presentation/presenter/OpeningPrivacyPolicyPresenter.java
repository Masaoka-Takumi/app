package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.DeviceConnectionSuppressEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.ShowCautionEvent;
import jp.pioneer.carsync.presentation.view.OpeningPrivacyPolicyView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;


/**
 *起動時PrivacyPolicy画面のPresenter
 */
@PresenterLifeCycle
public class OpeningPrivacyPolicyPresenter extends Presenter<OpeningPrivacyPolicyView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject AppSharedPreference mPreference;
    @Inject Context mContext;
    @Inject ControlSource mControlSource;
    private boolean mIsScrollBottom;

    /**
     * コンストラクタ
     */
    @Inject
    public OpeningPrivacyPolicyPresenter() {
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("agree", mIsScrollBottom);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        setEnabledAgreeBtn(savedInstanceState.getBoolean("agree"));
    }

    public void onScrollBottomAction(){
        mIsScrollBottom = true;
        setEnabledAgreeBtn(true);
    }

    /**
     * 同意ボタン押下時の処理
     */
    public void onAcceptAction(){
        mPreference.setAgreedEulaPrivacyPolicy(true);
        AnalyticsEventManager.startSession(mContext);
        StatusHolder holder = mGetStatusHolder.execute();
        //購入情報チェックが不要かつオーバーレイ権限がOKの場合連携抑制解除
        if(holder.getAppStatus().adasBillingCheck&&!(MainPresenter.sIsVersionQ&&!Settings.canDrawOverlays(mContext))) {
            holder.getAppStatus().deviceConnectionSuppress = false;
            mEventBus.post(new DeviceConnectionSuppressEvent());
        }
        if(mGetStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED) {
            if (mPreference.isFirstInitialSettingCompletion()) {
                mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
            } else {
                if (holder.getProtocolSpec().isSphCarDevice()) {
                    //mControlSource.selectSource(MediaSourceType.OFF);
                    transitionFirstInitialSetting();
                } else {
                    mPreference.setFirstInitialSettingCompletion(true);
                    mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
                }
            }
            mEventBus.post(new ShowCautionEvent());
        } else {
            mEventBus.post(new NavigateEvent(ScreenId.UNCONNECTED_CONTAINER, Bundle.EMPTY));
        }
    }

    private void transitionFirstInitialSetting() {
        SettingsParams params = new SettingsParams();
        params.pass = mContext.getString(R.string.set_104);
        params.mScreenId = ScreenId.SETTINGS_SYSTEM_INITIAL;
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, params.toBundle()));
    }

    private void setEnabledAgreeBtn(boolean isEnabled){
        Optional.ofNullable(getView()).ifPresent(view -> view.setEnabledAgreeBtn(isEnabled));
    }
}
