package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.DabFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SessionStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.IsGrantReadNotification;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.interactor.PreferReadNotification;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.SettingEntrance;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.SettingsEntranceView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * 設定入口のPresenter
 */
@PresenterLifeCycle
public class SettingsEntrancePresenter extends Presenter<SettingsEntranceView> {
    private static final String TAG_DIALOG_ALEXA_DENIAL = "alexa_denial";
    @Inject EventBus mEventBus;
    @Inject PreferNaviApp mNaviCase;
    @Inject IsGrantReadNotification mIsGrantCase;
    @Inject CheckAvailableTextToSpeech mCheckTtsCase;
    @Inject PreferReadNotification mMessagingCase;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject YouTubeLinkStatus mYouTubeLinkStatus;
    private ArrayList<Integer> mIconArray;
    private ArrayList<SettingEntrance> mTitleArray;
    private ArrayList<Boolean> mEnableArray;

    @Inject
    public SettingsEntrancePresenter() {
    }

    @Override
    void onTakeView() {
        setArray();
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mIconArray, mTitleArray, mEnableArray));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void setArray() {
        mIconArray = new ArrayList<>();
        mTitleArray = new ArrayList<>();
        mEnableArray = new ArrayList<>();
        StatusHolder holder = mGetStatusHolder.execute();
        CarDeviceStatus status = holder.getCarDeviceStatus();
        CarDeviceSpec spec = holder.getCarDeviceSpec();

        if (spec.systemSettingSupported) {
            mIconArray.add(R.drawable.p0091_icon);
            mTitleArray.add(SettingEntrance.SYSTEM);
            mEnableArray.add(status.systemSettingEnabled);
        }

        mTitleArray.add(SettingEntrance.VOICE);
        mIconArray.add(R.drawable.p0092_icon);
        mEnableArray.add(true);

        mTitleArray.add(SettingEntrance.NAVIGATION);

        mIconArray.add(R.drawable.p0093_icon);
        mEnableArray.add(true);

        mTitleArray.add(SettingEntrance.MESSAGE);
        mIconArray.add(R.drawable.p0094_icon);
        mEnableArray.add(true);

        mTitleArray.add(SettingEntrance.PHONE);
        mIconArray.add(R.drawable.p0095_icon);
        mEnableArray.add(true);

        if(mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN) {
            mTitleArray.add(SettingEntrance.CAR_SAFETY);
            mIconArray.add(R.drawable.p0096_icon);
            mEnableArray.add(true);
        }

        if (spec.illuminationSettingSupported) {
            mTitleArray.add(SettingEntrance.APPEARANCE);
            mIconArray.add(R.drawable.p0097_icon);
            mEnableArray.add(true);
        }

        if (spec.soundFxSettingSupported) {
            mTitleArray.add(SettingEntrance.SOUND_FX);
            mIconArray.add(R.drawable.p0098_icon);
            mEnableArray.add(status.soundFxSettingEnabled);
        }
        if (holder.isAudioSettingSupported()) {
            mTitleArray.add(SettingEntrance.AUDIO);
            mIconArray.add(R.drawable.p0099_icon);
            mEnableArray.add(holder.isAudioSettingEnabled());
        }

        if (spec.dabFunctionSettingSupported) {
            mTitleArray.add(SettingEntrance.DAB);
            mIconArray.add(R.drawable.p1631_dab_settings_icon);
            mEnableArray.add(status.dabFunctionSettingEnabled);
        }

        if (spec.tunerFunctionSettingSupported) {
            mTitleArray.add(SettingEntrance.RADIO);
            mIconArray.add(R.drawable.p0100_icon);
            mEnableArray.add(status.tunerFunctionSettingEnabled);
        }else if(spec.hdRadioFunctionSettingSupported){
            mTitleArray.add(SettingEntrance.HD_RADIO);
            mIconArray.add(R.drawable.p0100_icon);
            mEnableArray.add(status.hdRadioFunctionSettingEnabled);
        }

        mTitleArray.add(SettingEntrance.FUNCTION);
        mIconArray.add(R.drawable.p0972_icon_appsetting);
        mEnableArray.add(true);

        mTitleArray.add(SettingEntrance.INFORMATION);
        mIconArray.add(R.drawable.p0973_icon_information);
        mEnableArray.add(true);

        // TODO Alexaを塞ぐ #5244
        if(mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry) {
            mTitleArray.add(SettingEntrance.AMAZON_ALEXA);
            mIconArray.add(R.drawable.p0167_alexabtn_1nrm);
            mEnableArray.add(true);
        }

        // YouTubeLink設定のタイトル、アイコン、表示/無効表示をセット
        if(mYouTubeLinkStatus.isYouTubeLinkSettingAvailable()) {
            mTitleArray.add(SettingEntrance.YOUTUBE_LINK);
            mIconArray.add(R.drawable.p1632_youtubelink);
            mEnableArray.add(true);
        }
    }

    /**
     * 連絡先のアクセスが必要な設定か否か
     *
     * @param position ポジション
     */
    public boolean isRequireContactAccessSetting(int position){
        return (mTitleArray.get(position) == SettingEntrance.PHONE ||
                mTitleArray.get(position) == SettingEntrance.CAR_SAFETY);
    }

    /**
     * クリック時のアクション
     *
     * @param position ポジション
     */
    public void onClickAction(int position) {
        if(Objects.equals(mEnableArray.get(position), Boolean.FALSE)){
            return;
        }

        switch (mTitleArray.get(position)) {
            case SYSTEM:
                onSystemAction();
                break;
            case VOICE:
                onVoiceAction();
                break;
            case NAVIGATION:
                onNavigationAction();
                break;
            case MESSAGE:
                onMessageAction();
                break;
            case PHONE:
                onPhoneAction();
                break;
            case CAR_SAFETY:
                onCarSafetyAction();
                break;
            case APPEARANCE:
                onThemeAction();
                break;
            case SOUND_FX:
                onFxAction();
                break;
            case AUDIO:
                onAudioAction();
                break;
            case RADIO:
                onRadioAction();
                break;
            case DAB:
                onDabAction();
                break;
            case HD_RADIO:
                onHdRadioAction();
                break;
            case INFORMATION:
                onInformationAction();
                break;
            case FUNCTION:
                onAppSettingAction();
                break;
            case AMAZON_ALEXA:
                onAmazonAlexaAction();
                break;
            case YOUTUBE_LINK:
                onYouTubeLinkSettingAction();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingChangeEvent(AudioSettingChangeEvent event) {
        setArray();
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mIconArray, mTitleArray, mEnableArray));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingChangeEvent(RadioFunctionSettingChangeEvent event) {
        setArray();
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mIconArray, mTitleArray, mEnableArray));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        setArray();
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mIconArray, mTitleArray, mEnableArray));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionStatusChangeEvent(SessionStatusChangeEvent event) {
        setArray();
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mIconArray, mTitleArray, mEnableArray));
    }

    /**
     * Systemボタン押下時の処理
     */
    private void onSystemAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_SYSTEM, createSettingsParams(mContext.getString(R.string.set_215))));
    }

    /**
     * Voiceボタン押下時の処理
     */
    private void onVoiceAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_VOICE, createSettingsParams(mContext.getString(R.string.set_240))));
    }

    /**
     * Navigationボタン押下時の処理
     */
    private void onNavigationAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_NAVIGATION, createSettingsParams(mContext.getString(R.string.set_144))));
    }

    /**
     * Messageボタン押下時の処理
     */
    private void onMessageAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mIsGrantCase.execute()) {
                /*
                 * 当アプリケーションの"通知へのアクセス"が許可されていないため、
                 * 通知情報が取得できない。
                 * "通知へのアクセス"画面へ遷移して設定変更をしてもらう。
                 *
                 * Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS はAPI level 22以降のため、利用不可
                 */
                view.onShowAndroidSettings("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                return;
            }

            mCheckTtsCase.execute(result -> {
                switch (result) {
                    case AVAILABLE:
                    default:
                        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_MESSAGE, createSettingsParams(mContext.getString(R.string.set_133))));
                        break;
                    case LANG_NOT_SUPPORTED:
                    case MAY_NOT_DISABLED:
                        try {
                            view.onShowAndroidSettings("com.android.settings.TTS_SETTINGS");
                            view.onShowMessage(mContext.getString(R.string.err_019));
                        } catch (Exception ex){
                            view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        }
                        break;
                    case LANG_MISSING_DATA:
                        view.onShowAndroidSettings(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        break;
                }
            });
        });
    }

    /**
     * Phoneボタン押下時の処理
     */
    private void onPhoneAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_PHONE, createSettingsParams(mContext.getString(R.string.set_156))));
    }

    /**
     * CarSafetyボタン押下時の処理
     */
    private void onCarSafetyAction() {
        mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(mContext.getString(R.string.set_038))));
    }

    /**
     * Themeボタン押下時の処理
     */
    private void onThemeAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_THEME, createSettingsParams(mContext.getString(R.string.set_017))));
    }

    /**
     * Fxボタン押下時の処理
     */
    private void onFxAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_FX, createSettingsParams(mContext.getString(R.string.set_084))));
    }

    /**
     * Audioボタン押下時の処理
     */
    private void onAudioAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_AUDIO, createSettingsParams(mContext.getString(R.string.set_020))));
    }

    /**
     * Radioボタン押下時の処理
     */
    private void onRadioAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_RADIO, createSettingsParams(mContext.getString(R.string.set_170))));
    }

    /**
     * HD Radioボタン押下時の処理
     */
    private void onHdRadioAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_HD_RADIO, createSettingsParams(mContext.getString(R.string.src_015))));
    }

    /**
     * Dabボタン押下時の処理
     */
    private void onDabAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_DAB, createSettingsParams(mContext.getString(R.string.src_014))));
    }

    /**
     * Informationボタン押下時の処理
     */
    private void onInformationAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_INFORMATION, createSettingsParams(mContext.getString(R.string.set_103))));
    }

    /**
     * AppFunctionボタン押下時の処理
     */
    private void onAppSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_APP, createSettingsParams(mContext.getString(R.string.set_015))));
    }

    /**
     * AmazonAlexaボタン押下時の処理
     */
    private void onAmazonAlexaAction() {
        StatusHolder holder = mGetStatusHolder.execute();
        //holder.getAppStatus().alexaAuthenticated=true;
        if (holder.getAppStatus().alexaAuthenticated) {
            mEventBus.post(new NavigateEvent(ScreenId.ALEXA_SETTING, createSettingsParams(mContext.getString(R.string.set_302))));

        } else {
            if (holder.getSessionStatus() == SessionStatus.STARTED) {
                Bundle bundle = new Bundle();
                bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_ALEXA_DENIAL);
                bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.hom_038));
                bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
            } else {
                mEventBus.post(new NavigateEvent(ScreenId.ALEXA_SPLASH, createSettingsParams(mContext.getString(R.string.set_314))));

            }
        }
    }

    /**
     * YouTubeLinkボタン押下時の処理
     */
    private void onYouTubeLinkSettingAction() {
        mEventBus.post(new NavigateEvent(ScreenId.YOUTUBE_LINK_SETTING, createSettingsParams(mContext.getString(R.string.hom_039))));
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
