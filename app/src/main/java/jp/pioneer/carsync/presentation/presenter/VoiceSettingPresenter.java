package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeMicType;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.VoiceSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;

/**
 * Voice設定画面のPresenter.
 */
@PresenterLifeCycle
public class VoiceSettingPresenter extends Presenter<VoiceSettingView> {

    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusCase;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AnalyticsEventManager mAnalytics;
    public final static boolean mIsDebug = BuildConfig.DEBUG;
    /**
     * コンストラクタ
     */
    @Inject
    public VoiceSettingPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void updateView(){
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        Optional.ofNullable(getView()).ifPresent(view ->
        {
            //TODO:Alexaを塞ぐ
            view.setVoiceRecognitionVisible(!(mIsDebug && appStatus.isAlexaAvailableCountry));
            view.setVoiceRecognitionTypeVisible(mIsDebug && appStatus.isAlexaAvailableCountry);
            view.setVoiceRecognitionEnabled(mPreference.isVoiceRecognitionEnabled());
            view.setVoiceRecognitionType(mPreference.getVoiceRecognitionType());
            view.setVoiceRecognitionTypeEnabled(true);
			view.setVoiceRecognitionMicTypeVisible(mStatusCase.execute().getPhoneSettingStatus().hfDevicesCountStatus != ConnectedDevicesCountStatus.NONE);
            view.setVoiceRecognitionMicTypeEnabled(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.PIONEER_SMART_SYNC);
            if(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA){
                view.setVoiceRecognitionMicType(VoiceRecognizeMicType.PHONE);
            }else{
                view.setVoiceRecognitionMicType(mPreference.getVoiceRecognitionMicType());
            }
        });
    }
    /**
     * VoiceRecognition押下時の処理
     *
     * @param isValue 有効/無効
     */
    public void onVoiceRecognitionChange(boolean isValue) {
        mPreference.setVoiceRecognitionEnabled(isValue);
    }

    /**
     * VoiceRecognition押下時の処理
     *
     */
    public void onVoiceRecognitionTypeChange() {
        Bundle bundle = new Bundle();
        bundle.putString(SingleChoiceDialogFragment.TITLE, mContext.getResources().getString(R.string.set_323));
        String[] strArray = {mContext.getResources().getString(R.string.set_324), mContext.getResources().getString(R.string.set_325)};
        bundle.putStringArray(SingleChoiceDialogFragment.DATA, strArray);     // Require ArrayList
        bundle.putInt(SingleChoiceDialogFragment.SELECTED, mPreference.getVoiceRecognitionType().code);
        mEventBus.post(new NavigateEvent(ScreenId.SELECT_DIALOG, bundle));
    }

    public void setVoiceRecognizeType(int position){
        VoiceRecognizeType nextType = VoiceRecognizeType.valueOf(position);
        mPreference.setVoiceRecognitionType(nextType);
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setVoiceRecognitionType(nextType);
            view.setVoiceRecognitionMicTypeEnabled(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.PIONEER_SMART_SYNC);
            if(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA){
                view.setVoiceRecognitionMicType(VoiceRecognizeMicType.PHONE);
            }else{
                view.setVoiceRecognitionMicType(mPreference.getVoiceRecognitionMicType());
            }
        });
        if(nextType==VoiceRecognizeType.PIONEER_SMART_SYNC) {
            AppStatus appStatus = mStatusCase.execute().getAppStatus();
            if (appStatus.appMusicAudioMode == AudioMode.ALEXA) {
                appStatus.appMusicAudioMode = AudioMode.MEDIA;
                appStatus.playerInfoItem = null;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                if (audioManager != null) {
                    audioManager.doStop();
                }
            }
        }
    }

    /**
     * VoiceRecognition押下時の処理
     *
     */
    public void onVoiceRecognitionMicTypeChange() {
        VoiceRecognizeMicType nextType = mPreference.getVoiceRecognitionMicType().toggle();
        mPreference.setVoiceRecognitionMicType(nextType);
        Optional.ofNullable(getView()).ifPresent(view -> view.setVoiceRecognitionMicType(nextType));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneSettingStatusChangeEvent(PhoneSettingStatusChangeEvent event) {
        updateView();
    }
}
