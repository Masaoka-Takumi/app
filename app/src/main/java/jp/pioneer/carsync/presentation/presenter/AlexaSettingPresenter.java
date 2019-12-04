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
import jp.pioneer.carsync.domain.model.AlexaLanguageType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlexaSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.mbg.alexa.manager.AlexaDirectiveManager;
import jp.pioneer.mbg.alexa.util.SettingsUpdatedUtil;

/**
 * Alexa設定のPresenter
 */
@PresenterLifeCycle
public class AlexaSettingPresenter extends Presenter<AlexaSettingView>{
    public static final String TAG_DIALOG_ALEXA_SIGN_OUT = "alexa_sign_out";
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject
    public AlexaSettingPresenter() {
    }

    @Override
    void onTakeView() {

    }

    @Override
    void onResume(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAlexaLanguage(mPreference.getAlexaLanguage().label);
        });
    }

    @Override
    void onPause() {
    }

    public void showSignOutDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ALEXA_SIGN_OUT);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.set_306));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    public void onAlexaExampleUsage(){
        mEventBus.post(new NavigateEvent(ScreenId.ALEXA_EXAMPLE_USAGE, Bundle.EMPTY));
    }

    public void showLanguageSelectDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(SingleChoiceDialogFragment.TITLE, mContext.getResources().getString(R.string.set_307));
        bundle.putStringArray(SingleChoiceDialogFragment.DATA, mContext.getResources().getStringArray(R.array.alexa_language));     // Require ArrayList
        bundle.putInt(SingleChoiceDialogFragment.SELECTED, mPreference.getAlexaLanguage().code);
        mEventBus.post(new NavigateEvent(ScreenId.SELECT_DIALOG, bundle));
    }

    public void setAlexaLanguage(int position){
        AlexaLanguageType type = AlexaLanguageType.valueOf((byte)position);
        mPreference.setAlexaLanguage(type);
        SettingsUpdatedUtil.setLocale(mContext.getString(mPreference.getAlexaLanguage().locale));
        AlexaDirectiveManager.sendSettingsUpdated(mContext);
    }

    public void onNavigateAlexaUsage() {
//        CarDeviceClassId lastCarDeviceClassId = mPreference.getLastConnectedCarDeviceClassId();
//        if(lastCarDeviceClassId == CarDeviceClassId.SPH) {
            mEventBus.post(new NavigateEvent(ScreenId.ALEXA_EXAMPLE_USAGE, createSettingsParams(mContext.getString(R.string.set_318))));
//        } else {
//            mEventBus.post(new NavigateEvent(ScreenId.ALEXA_GUIDANCE_OF_PUTTING_SMARTPHONE, createSettingsParams(mContext.getString(R.string.set_403))));
//            mEventBus.post(new NavigateEvent(ScreenId.ALEXA_GUIDANCE_OF_PUTTING_SMARTPHONE, createSettingsParams("Amazon Alexa")));
//        }
    }

    public void onLogout(){
        StatusHolder holder = mGetStatusHolder.execute();
        holder.getAppStatus().alexaAuthenticated = false;
        //ログアウト時にCapabilitiesSend状態クリア
        mPreference.setAlexaCapabilitiesSend(false);
        mEventBus.post(new GoBackEvent());
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
