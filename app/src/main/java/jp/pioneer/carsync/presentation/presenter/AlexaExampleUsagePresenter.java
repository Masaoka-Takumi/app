package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AlexaLanguageType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlexaExampleUsageView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.mbg.alexa.manager.AlexaDirectiveManager;
import jp.pioneer.mbg.alexa.util.SettingsUpdatedUtil;


/**
 * AlexaExampleUsagePresenter
 */
@PresenterLifeCycle
public class AlexaExampleUsagePresenter  extends Presenter<AlexaExampleUsageView> {
    public static final String TAG_DIALOG_ALEXA_SIGN_OUT = "alexa_sign_out";
    public static final String TAG_DIALOG_ALEXA_MIC_PROMPT = "alexa_mic_prompt";
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject
    public AlexaExampleUsagePresenter() {
    }
    /**
     * Back押下アクション
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    /**
     * Next押下アクション
     */
    public void onNextAction() {
        showAlexaMicPromptDialog();
    }

    private void showAlexaMicPromptDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ALEXA_MIC_PROMPT);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.set_396));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    public void showLanguageSelectDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(SingleChoiceDialogFragment.TITLE, mContext.getResources().getString(R.string.set_307));
        String[] strArray = new String[AlexaLanguageType.getValues().size()];
        for (int i = 0; i < strArray.length; i++) {
            strArray[i] = mContext.getString(AlexaLanguageType.getValues().get(i).label);
        }
        bundle.putStringArray(SingleChoiceDialogFragment.DATA, strArray);     // Require ArrayList
        mEventBus.post(new NavigateEvent(ScreenId.SELECT_DIALOG, bundle));
    }

    public void setAlexaLanguage(int position){
        AlexaLanguageType type = AlexaLanguageType.getValues().get(position);
        mPreference.setAlexaLanguage(type);
        mEventBus.post(new NavigateEvent(ScreenId.ALEXA_SETTING, createSettingsParams(mContext.getString(R.string.set_302))));
    }

    public void showSignOutDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ALEXA_SIGN_OUT);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.set_306));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    public void onLogout(){
        StatusHolder holder = mGetStatusHolder.execute();
        holder.getAppStatus().alexaAuthenticated = false;
        //ログアウト時にCapabilitiesSend状態クリア
        mPreference.setAlexaCapabilitiesSend(false);
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_ENTRANCE, createSettingsParams(mContext.getString(R.string.hom_015))));
    }

    public ScreenId getBeforeScreenId(Bundle arguments) {
        SettingsParams params = SettingsParams.from(arguments);
        return params.mScreenId;
    }

    public CarDeviceClassId getLastConnectedCarDeviceClassId() {
        return mPreference.getLastConnectedCarDeviceClassId();
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
