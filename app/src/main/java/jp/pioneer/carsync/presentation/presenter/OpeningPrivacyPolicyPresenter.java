package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
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
    @Inject AnalyticsEventManager mAnalytics;
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
        //再表示し同意したら保存するバージョン（13言語）全て上書き
        String language = mContext.getResources().getString(R.string.url_001);
        int newVersionCodeEula = 0;
        Integer newVersionCodeEuraInteger = App.EULA_PRIVACY_NEW_VERSION.get(language);
        if (newVersionCodeEuraInteger != null) {
            newVersionCodeEula = newVersionCodeEuraInteger;
        }
        for (Map.Entry<String,Integer> entry : App.EULA_PRIVACY_NEW_VERSION.entrySet()) {
            mPreference.setEulaPrivacyVersionCode(entry.getKey(), entry.getValue());
        }
        mAnalytics.startSession(mContext);
        StatusHolder holder = mGetStatusHolder.execute();
        //購入情報チェックが不要かつオーバーレイ権限がOKの場合連携抑制解除
        if(holder.getAppStatus().adasBillingCheck&&!(MainPresenter.sIsVersionQ&&!Settings.canDrawOverlays(mContext))) {
            // Alexa利用可能ダイアログを表示する必要がない場合は連携抑制解除
            if(!isAlexaAvailableConfirmNeeded()) {
                holder.getAppStatus().deviceConnectionSuppress = false;
                mEventBus.post(new DeviceConnectionSuppressEvent());
            }
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

    /**
     * Alexa機能利用可能ダイアログを出すべきかどうかの判定
     * TODO #5244 MainPresenterの同名メソッドと共通化したい
     * @return
     * {@code true}:Alexa機能が利用可能かつAlexa機能利用可能ダイアログを未表示
     * {@code false}:それ以外(Alexa機能が利用不可能またはAlexa機能利用可能ダイアログを表示済み)
     */
    private boolean isAlexaAvailableConfirmNeeded() {
        return mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry && !mPreference.isAlexaAvailableConfirmShowed();
    }
}
