package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.AlexaNotificationChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicAudioModeChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.event.LiveSimulationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.NotificationListenerServiceConnectedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.event.VoiceRecognitionTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetReadNotificationList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AdasErrorType;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.model.SoundFxItem;
import jp.pioneer.carsync.presentation.util.AlexaAvailableStatus;
import jp.pioneer.carsync.presentation.util.CustomKeyActionHandler;
import jp.pioneer.carsync.presentation.util.YouTubeLinkActionHandler;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import timber.log.Timber;

/**
 * 再生画面の共通Presenter.
 */
public class PlayerPresenter<T> extends Presenter<T> {
    private static final String EMPTY = "";

    @Inject Context mContext;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;
    @Inject EventBus mEventBus;
    @Inject PreferSoundFx mFxCase;
    @Inject ExitMenu mExitMenu;
    @Inject AppSharedPreference mPreference;
    @Inject ActionSoftwareShortcutKey mShortcutCase;
    @Inject ControlSource mControlSource;
    @Inject GetReadNotificationList mNotificationCase;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject PreferMusicApp mPreferMusicApp;
    @Inject CustomKeyActionHandler mCustomKeyActionHandler;
    @Inject YouTubeLinkActionHandler mYouTubeLinkActionHandler;
    @Inject YouTubeLinkStatus mYouTubeLinkStatus;
    @Inject AnalyticsEventManager mAnalytics;
    @Inject AlexaAvailableStatus mAlexaAvailableStatus;
    private final Handler mHandler = new Handler();
    List<SoundFxSettingEqualizerType> mEqArray = new ArrayList<>();
    List<SoundFxItem> mSoundFxArray = new ArrayList<SoundFxItem>(){{
        add(new SoundFxItem(SoundFxItem.ItemType.OFF,               SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.OFF));
        add(new SoundFxItem(SoundFxItem.ItemType.TODOROKI,          SoundFieldControlSettingType.OFF,       SuperTodorokiSetting.LAST));
    }};
    private MediaSourceType mSourceType;
    private String mCurrentFxText;
    private String mCurrentEqText;
    boolean mIsFxClick;
    boolean mIsEqClick;
    private List<Notification> mNotifications;
    private static final ShortcutKey[] KEY_INDEX = new ShortcutKey[]{
            ShortcutKey.SOURCE,
            ShortcutKey.VOICE,
            ShortcutKey.NAVI,
            ShortcutKey.MESSAGE,
            ShortcutKey.PHONE,
    };
    private static final int[][] KEY_IMAGES = new int[][]{
            {R.drawable.p2002_customkey_btn_1nrm, 0},//Source
            {R.drawable.p0162_vrbtn_1nrm, 0},//Voice
            {R.drawable.p0163_navibtn_1nrm, 0},//Navi
            {R.drawable.p0164_messagebtn_1nrm, R.drawable.p0171_notification},//Message
            {R.drawable.p0165_phonebtn_1nrm, 0},//Phone
    };
    protected ArrayList<ShortcutKeyItem> mShortCutKeyList = new ArrayList<>();
    private final static int YOUTUBE_LINK_ICON = R.drawable.p2001_youtubelink_btn_1nrm;
    private final static int CUSTOM_KEY_ICON = R.drawable.p2002_customkey_btn_1nrm;

    /**
     * コンストラクタ.
     */
    @Inject
    public PlayerPresenter() {
        for (int i = 0; i < KEY_INDEX.length; i++) {
            ShortcutKeyItem key = new ShortcutKeyItem(KEY_INDEX[i], KEY_IMAGES[i][0], KEY_IMAGES[i][1],true);
            mShortCutKeyList.add(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        setShortcutIconResources();
        CarDeviceSpec spec = mStatusHolder.execute().getCarDeviceSpec();
        mEqArray = spec.soundFxSettingSpec.supportedEqualizers;
        updateShortcutButton();

        // カスタムキー[ソース一覧表示]動作のセット
        mCustomKeyActionHandler.setSourceListAction(new Runnable() {
            @Override
            public void run() {
                mEventBus.post(new BackgroundChangeEvent(true));
                mEventBus.post(new NavigateEvent(ScreenId.SOURCE_SELECT, Bundle.EMPTY));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        onUpdateSoundFxButton();
        updateNotification();
        updateAlexaNotification();
        mHandler.postDelayed(this::showListIfListDisplayable, 500);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
        mEventBus.unregister(this);
    }


    /**
     * リストボタン押下処理.
     * <p>
     * 特別な処理が必要な場合はオーバーライドする。
     */
    public void onSelectListAction() {
        CarDeviceStatus status = mStatusHolder.execute().getCarDeviceStatus();
        if (status.sourceType.isPchListSupported()) {
            if(status.listType.canEnter()) {
                mMediaCase.enterList(ListType.PCH_LIST);
            } else {
                showListIfListDisplayable();
            }
        } else if (status.sourceType.isListSupported()) {
            if(status.sourceType == MediaSourceType.DAB){
                mMediaCase.enterList(ListType.SERVICE_LIST);
            } else if(status.listType.canEnter()) {
                mMediaCase.enterList(ListType.LIST);
            } else {
                showListIfListDisplayable();
            }
        }
    }

    /**
     * SoundFx切り換えアクション
     */
    public void onSelectFxAction() {
        SoundFxSetting fxSetting = getFxSetting();

        mIsEqClick = false;
        mIsFxClick = true;

        int soundFxStr;
        SuperTodorokiSetting superTodorokiSetting = fxSetting.superTodorokiSetting;
        LiveSimulationSetting liveSimulationSetting = fxSetting.liveSimulationSetting;
        int index = 0;
        if(superTodorokiSetting == SuperTodorokiSetting.OFF){
            index = 0;
        }else{
            for(SoundFxItem item : mSoundFxArray){
                if(item.type == SoundFxItem.ItemType.TODOROKI){
                    index = mSoundFxArray.indexOf(item);
                    break;
                }
            }
        }
        if(index >= mSoundFxArray.size() - 1){
            index = 0;
        }else{
            index = index + 1 ;
        }
        SoundFxItem nextItem = mSoundFxArray.get(index);

        mExitMenu.execute();
        if(nextItem.type== SoundFxItem.ItemType.LIVE_SIMULATION) {
            SoundEffectType effectType = liveSimulationSetting.soundEffectSettingType.type;
            mFxCase.setLiveSimulation(nextItem.soundFieldControlSetting, effectType);
        }else {
            mFxCase.setSuperTodoroki(nextItem.superTodorokiSetting);
        }
    }

    /**
     * EQ切り換えアクション
     */
    public void onSelectVisualAction() {
        mExitMenu.execute();

        mIsEqClick = true;
        mIsFxClick = false;

        SoundFxSetting fxSetting = getFxSetting();
        if(fxSetting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF &&
                mStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC){
            mFxCase.setLiveSimulation(SoundFieldControlSettingType.OFF, fxSetting.liveSimulationSetting.soundEffectSettingType.type);
        } else if (fxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF){
            mFxCase.setSuperTodoroki(SuperTodorokiSetting.OFF);
        } else {
            SoundFxSettingEqualizerType equalizerType = fxSetting.soundFxSettingEqualizerType;
            int index = mEqArray.indexOf(equalizerType);
            if (index < 0) {
                return;
            } else {
                if (index >= mEqArray.size() - 1) {
                    index = 0;
                } else {
                    for(SoundFxSettingEqualizerType eq : mEqArray) {
                        index++;
                        if (!(!mPreference.isDebugSpecialEqEnabled() && mEqArray.get(index).code >= (1 << 8))) {
                            break;
                        }
                    }
                }
            }
            mFxCase.setEqualizer(mEqArray.get(index));
        }
    }

    /**
     * Home画面遷移
     */
    public void onHomeAction() {
        mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
    }

    /**
     * Settingへの遷移アクション
     */
    public void onSettingsAction(Bundle bundle) {
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, bundle));
    }

    /**
     * ソース選択画面への遷移アクション
     */
    public void onSelectSourceAction() {
        mEventBus.post(new BackgroundChangeEvent(true));
        mEventBus.post(new NavigateEvent(ScreenId.SOURCE_SELECT, Bundle.EMPTY));
    }

    /**
     * SoundFXボタンの情報取得
     *
     * @return SoundFXボタンに表示する情報
     */
    public SoundFxButtonInfo getSoundFxButtonInfo(){
        StatusHolder holder = mStatusHolder.execute();
        SoundFxSettingStatus settingStatus = holder.getSoundFxSettingStatus();
        AudioSettingStatus audioSettingStatus = holder.getAudioSettingStatus();
        String eqStr, soundFxStr;

        SuperTodorokiSetting superTodorokiSetting = getFxSetting().superTodorokiSetting;
        LiveSimulationSetting liveSimulationSetting = getFxSetting().liveSimulationSetting;
        SoundFxSettingEqualizerType equalizerType = getFxSetting().soundFxSettingEqualizerType;

        if(superTodorokiSetting != SuperTodorokiSetting.OFF){
            soundFxStr = mContext.getString(R.string.ply_053);
        }else{
            if(holder.getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC&&holder.getAppStatus().appMusicAudioMode== AudioMode.MEDIA){
                soundFxStr = mContext.getString(liveSimulationSetting.soundFieldControlSettingType.getLabel());
            } else {
                soundFxStr = mContext.getString(SoundFieldControlSettingType.OFF.getLabel());
            }
        }

        if(mEqArray.size() > 0){
            if(holder.getCarDeviceStatus().sourceType != MediaSourceType.APP_MUSIC ) {
                if(superTodorokiSetting != SuperTodorokiSetting.OFF){
                    eqStr = mContext.getString(SoundFxSettingEqualizerType.FLAT.getLabel());
                } else {
                    eqStr = mContext.getString(equalizerType.getLabel());
                }
            } else {
                if (liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF) {
                    eqStr = mContext.getString(SoundFxSettingEqualizerType.FLAT.getLabel());
                } else if (superTodorokiSetting != SuperTodorokiSetting.OFF) {
                    eqStr = mContext.getString(SoundFxSettingEqualizerType.FLAT.getLabel());
                }else if(holder.getAppStatus().appMusicAudioMode== AudioMode.ALEXA){
                    eqStr = mContext.getString(SoundFxSettingEqualizerType.FLAT.getLabel());
                } else {
                    eqStr = mContext.getString(equalizerType.getLabel());
                }
            }
        }else{
            eqStr = EMPTY;
        }

        boolean isEnabled = (holder.getCarDeviceStatus().sourceType != MediaSourceType.OFF &&
                        holder.getCarDeviceStatus().sourceType != MediaSourceType.TI) &&
                        holder.getCarDeviceSpec().supportedSources.contains(holder.getCarDeviceStatus().sourceType);

        SoundFxButtonInfo info = new SoundFxButtonInfo(mIsFxClick, mIsEqClick, isEnabled,isEnabled,soundFxStr, eqStr);

        if(!eqStr.equals(mCurrentEqText) || !soundFxStr.equals(mCurrentFxText)){
            mIsFxClick = false;
            mIsEqClick = false;
        }

        mCurrentEqText = eqStr;
        mCurrentFxText = soundFxStr;
        return info;
    }

    private SoundFxSetting getFxSetting() {
        StatusHolder holder = mStatusHolder.execute();
        return holder.getSoundFxSetting();
    }


    /**
     * リスト表示判定.
     */
    private void showListIfListDisplayable() {
        StatusHolder holder = mStatusHolder.execute();
        ListType listType = holder.getCarDeviceStatus().listType;
        MediaSourceType sourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
        if(sourceType==MediaSourceType.APP_MUSIC){
            if(listType==ListType.LIST||listType==ListType.ABC_SEARCH_LIST){
                if(mStatusHolder.execute().getAppStatus().appMusicAudioMode==AudioMode.ALEXA){
                    //LIST非対応ダイアログ表示
                    Bundle bundle = new Bundle();
                    String title="";
                    title = mContext.getString(R.string.ply_084);
                    bundle.putString(StatusPopupDialogFragment.TAG, title);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, title);
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mEventBus.post(new NavigateEvent(ScreenId.LIST_STATUS_DIALOG, bundle));
                    return;
                }
            }
        }
        if (listType.types.contains(sourceType)) {
            onShowList();
        }
        onListTypeChange();
    }

    //UnitTest用
    @VisibleForTesting
    public void setUp(GetStatusHolder statusHolder, EventBus eventBus, ControlMediaList controlMediaList) {
        mStatusHolder = statusHolder;
        mEventBus = eventBus;
        mMediaCase = controlMediaList;
    }
    @VisibleForTesting
    public void stopHandler(){
        mHandler.removeCallbacks(this::showListIfListDisplayable);
    }


    // MARK - subscribe

    /**
     * ソース種別変更イベント.
     *
     * @param event MediaSourceTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent event) {
        MediaSourceType sourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
        if(mSourceType != sourceType) {
            mIsFxClick = false;
            mIsEqClick = false;

            mSourceType = sourceType;
        }
    }

    /**
     * リスト種別変更イベント.
     *
     * @param event ListTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListTypeChangeEvent(ListTypeChangeEvent event) {
        showListIfListDisplayable();
    }

    /**
     * Equalizer設定変更イベント.
     *
     * @param event EqualizerSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEqualizerSettingChangeEvent(EqualizerSettingChangeEvent event) {
        onUpdateSoundFxButton();
    }

    /**
     *  Live Simulation設定変更イベント.
     *
     * @param event LiveSimulationSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveSimulationSettingChangeEvent(LiveSimulationSettingChangeEvent event) {
        onUpdateSoundFxButton();
    }

    /**
     * Super轟設定変更イベント.
     *
     * @param event SuperTodorokiSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuperTodorokiSettingChangeEvent(SuperTodorokiSettingChangeEvent event) {
        onUpdateSoundFxButton();
    }

    /**
     * Sound FX設定ステータス変更イベント.
     *
     * @param event SoundFxSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxSettingStatusChangeEvent(SoundFxSettingStatusChangeEvent event) {
        onUpdateSoundFxButton();
    }

    // MARK - override method

    /**
     * リスト遷移.
     * <p>
     * ソースにより表示するリストが異なるため、
     * 遷移先はオーバーライドして指定する。
     */
    void onShowList() {
    }

    /**
     * リスト状態変更.
     */
    void onListTypeChange(){
    }

    /**
     * SoundFxボタン更新.
     * <p>
     * FX,EQボタン更新に使用する
     */
    void onUpdateSoundFxButton(){
    }

    // MARK - class

    /**
     * SoundFxボタン情報
     */
    class SoundFxButtonInfo{
        /** FX更新メッセージを表示するか否か. */
        boolean isShowFxMessage;
        /** EQ更新メッセージを表示するか否か. */
        boolean isShowEqMessage;
        /** FXボタンが有効か否か. */
        boolean isFxEnabled;
        /** EQボタンが有効か否か. */
        boolean isEqEnabled;
        /** FXボタンに表示するテキスト. */
        String textFxButton;
        /** EQボタンに表示するテキスト. */
        String textEqButton;

        /**
         * コンストラクタ.
         */
        SoundFxButtonInfo(boolean isShowFxMessage,
                          boolean isShowEqMessage,
                          boolean isFxEnabled,
                          boolean isEqEnabled,
                          String textFxButton,
                          String textEqButton){
            this.isShowFxMessage = isShowFxMessage;
            this.isShowEqMessage = isShowEqMessage;
            this.isFxEnabled = isFxEnabled;
            this.isEqEnabled = isEqEnabled;
            this.textFxButton = textFxButton;
            this.textEqButton = textEqButton;
        }
    }

    public void onAdasErrorAction(){
        AppStatus appStatus = mStatusHolder.execute().getAppStatus();
        if(appStatus.isAdasError()){
            Set<AdasErrorType> errors = appStatus.adasErrorList;

            Bundle bundle = new Bundle();
            String title="";
            String text="";
            if(errors.contains(AdasErrorType.ORIENTATION_PORTRAIT)){
                text = mContext.getString(R.string.err_036);
            }else if(errors.contains(AdasErrorType.PERMISSION_DENIED_ACCESS_LOCATION)){
                title = mContext.getString(R.string.err_027);
                text = mContext.getString(R.string.err_029);
            }else if(errors.contains(AdasErrorType.PERMISSION_DENIED_CAMERA)){
                title = mContext.getString(R.string.err_027);
                text = mContext.getString(R.string.err_030);
            }else if(errors.contains(AdasErrorType.DECLINE_IN_RECOGNITION_RATE)){
                title = mContext.getString(R.string.err_028);
                text = mContext.getString(R.string.err_031);
            }else if(errors.contains(AdasErrorType.LOW_ILLUMINANCE_OR_POOR_VISIBILITY)){
                title = mContext.getString(R.string.err_020);
                text = mContext.getString(R.string.err_023);
            }else if(errors.contains(AdasErrorType.ALARM_ERROR_DURING_NETWORK_MODE)){
                title = mContext.getString(R.string.err_022);
                text = mContext.getString(R.string.err_026);
            }else if(errors.contains(AdasErrorType.ALARM_ERROR_SOURCE_OFF)){
                title = mContext.getString(R.string.err_022);
                text = mContext.getString(R.string.err_025);
            }
            mEventBus.post(new AdasErrorEvent());
            bundle.putString(StatusPopupDialogFragment.TAG, title);
            bundle.putString(StatusPopupDialogFragment.TITLE, title);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, text);
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            mEventBus.post(new NavigateEvent(ScreenId.MAIN_STATUS_DIALOG, bundle));
        }
    }

    public MediaSourceType getMediaSourceType() {
        StatusHolder holder = mStatusHolder.execute();
        return holder.getCarDeviceStatus().sourceType;
    }

    /**
     * ShortCutKeyの遷移アクション
     */
    public void onKeyAction(ShortcutKey shortCutKey) {
        switch (shortCutKey) {
            case SOURCE:
                //カスタムキー
                Timber.d("onSourceAction");
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()){
                    //YouTubeLink機能設定がONの場合
                    mYouTubeLinkActionHandler.execute();
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.youTubeLinkShort, Analytics.AnalyticsActiveScreen.av_screen);
                } else {
                    //カスタムの割り当てがソース切替の場合
                    mCustomKeyActionHandler.execute();
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.customShort, Analytics.AnalyticsActiveScreen.av_screen);
                }
                break;
            case VOICE:
                Timber.d("onVoiceAction");
                mShortcutCase.execute(ShortcutKey.VOICE);
                mAnalytics.sendShortCutActionEvent(
                        mAlexaAvailableStatus.isVoiceRecognitionTypeAlexaAndAvailable()
                                ? Analytics.AnalyticsShortcutAction.alexaShort : Analytics.AnalyticsShortcutAction.voiceShort,
                        Analytics.AnalyticsActiveScreen.av_screen);
                break;
            case NAVI:
                Timber.d("onNavigateAction");
                mShortcutCase.execute(ShortcutKey.NAVI);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.navi, Analytics.AnalyticsActiveScreen.av_screen);
                break;
            case MESSAGE:
                Timber.d("onMessageAction");
                mShortcutCase.execute(ShortcutKey.MESSAGE);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.message, Analytics.AnalyticsActiveScreen.av_screen);
                break;
            case PHONE:
                Timber.d("onPhoneAction");
                mShortcutCase.execute(ShortcutKey.PHONE);
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.phoneShort, Analytics.AnalyticsActiveScreen.av_screen);
                break;
            default:
                break;
        }
    }

    public void onLongKeyAction(ShortcutKey shortCutKey) {
        switch (shortCutKey) {
            case PHONE:
                Timber.d("onDirectCallAction");
                if(mPreference.getDirectCallContactNumberId() > -1) {
                    mEventBus.post(new SmartPhoneControlCommandEvent(SmartPhoneControlCommand.DIRECT_CALL));
                }
                mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.phoneLong, Analytics.AnalyticsActiveScreen.av_screen);
                break;
            case SOURCE:
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()) {
                    // なにもしない
                    Timber.i("YouTubeLinkIcon LongKeyAction");
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.youtubeLinkLong, Analytics.AnalyticsActiveScreen.av_screen);
                } else {
                    // カスタムキー割当画面の表示
                    mEventBus.post(new NavigateEvent(ScreenId.CUSTOM_KEY_SETTING, Bundle.EMPTY));
                    mAnalytics.sendShortCutActionEvent(Analytics.AnalyticsShortcutAction.customLong, Analytics.AnalyticsActiveScreen.av_screen);
                }
                break;
            case VOICE:
                //TODO:Alexaを塞ぐ
                if(mStatusHolder.execute().getAppStatus().isAlexaAvailableCountry) {
                    mAnalytics.sendShortCutActionEvent(mPreference.getVoiceRecognitionType()==VoiceRecognizeType.ALEXA?Analytics.AnalyticsShortcutAction.alexaLong:Analytics.AnalyticsShortcutAction.voiceLong, Analytics.AnalyticsActiveScreen.av_screen);
                    VoiceRecognizeType nextType = mPreference.getVoiceRecognitionType().toggle();
                    mPreference.setVoiceRecognitionType(nextType);
                    mEventBus.post(new VoiceRecognitionTypeChangeEvent());
                }
                break;
            default:
                break;
        }
    }

    /**
     * NotificationListenerServiceConnectedEventハンドラ.
     * <p>
     * 通知監視サービスと接続した場合に動作する。
     *
     * @param event NotificationListenerServiceConnectedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationListenerServiceConnectedEvent(NotificationListenerServiceConnectedEvent event){
        updateNotification();
    }
    /**
     * ReadNotificationPostedEventハンドラ
     * <p>
     * 新規通知を受信した場合に動作する。
     *
     * @param event ReadNotificationPostedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotificationPostedEvent(ReadNotificationPostedEvent event) {
        updateNotification();
    }

    /**
     * ReadNotificationRemovedEventハンドラ
     * <p>
     * 通知を削除した場合に動作する。
     *
     * @param event ReadNotificationRemovedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotificationRemovedEvent(ReadNotificationRemovedEvent event) {
        updateNotification();
    }

    protected void updateShortcutButton() {
    }

    protected void updateNotification() {
        mNotifications = mNotificationCase.execute();
        for (int i = 0; i < mShortCutKeyList.size(); i++) {
            ShortcutKeyItem item = mShortCutKeyList.get(i);
            if (item.key == ShortcutKey.MESSAGE) {
                if (mNotifications.size() > 0) {
                    item.optionImageResource = R.drawable.p0171_notification;
                } else {
                    item.optionImageResource = 0;
                }
            }
        }
    }

    /**
     * AlexaNotificationChangeEvent
     * @param event AlexaNotificationChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlexaNotificationChangeEvent(AlexaNotificationChangeEvent event) {
        updateAlexaNotification();
    }

    protected boolean isNeedUpdateAlexaNotification() {
        AppStatus appStatus = mStatusHolder.execute().getAppStatus();
        boolean notificationQueued = false;
        if(mAlexaAvailableStatus.isVoiceRecognitionTypeAlexaAndAvailable()) {
            notificationQueued = appStatus.alexaNotification;
        }
        return notificationQueued;
    }

    protected void updateAlexaNotification() {
    }

    /**
     * VoiceRecognitionTypeChangeEvent
     * @param event VoiceRecognitionTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoiceRecognitionTypeChangeEvent(VoiceRecognitionTypeChangeEvent event) {
        VoiceRecognizeType type = mPreference.getVoiceRecognitionType();
        if(type==VoiceRecognizeType.PIONEER_SMART_SYNC) {
            AppStatus appStatus = mStatusHolder.execute().getAppStatus();
            if (appStatus.appMusicAudioMode == AudioMode.ALEXA) {
                appStatus.appMusicAudioMode = AudioMode.MEDIA;
                appStatus.playerInfoItem = null;
                mEventBus.post(new AppMusicAudioModeChangeEvent());
                AlexaAudioManager audioManager = AlexaAudioManager.getInstance();
                if (audioManager != null) {
                    audioManager.doStop();
                }
            }
            AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
            if (amazonAlexaManager != null) {
                amazonAlexaManager.doSpeechCancel();
            }
        }

        setShortcutIconResources();
        updateVoiceRecognitionType();
        updateShortcutButton();
        onResume();
    }
    protected void updateVoiceRecognitionType() {
    }

    // 12時間表示の場合、日本とそれ以外で表示形式を変える
    public void changeTimeFormatSetting(TextClock clock, TextView ampm) {
        if (mPreference.getTimeFormatSetting() == TimeFormatSetting.TIME_FORMAT_24) {
            clock.setFormat12Hour("kk:mm");
            clock.setFormat24Hour("kk:mm");
            clock.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.player_music_status_bar_clock_text_size_24));
            ampm.setVisibility(View.GONE);
        } else {
            // AM/PM 文字列
            if (AppUtil.isZero2ElevenIn12Hour(mContext)) {
                clock.setFormat12Hour("KK:mm");
                clock.setFormat24Hour("KK:mm");
            } else {
                clock.setFormat12Hour("hh:mm");
                clock.setFormat24Hour("hh:mm");
            }
            clock.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.player_music_status_bar_clock_text_size_12));
            ampm.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ショートカット領域のアイコンの切り替え
     * カスタムキーアイコン or YouTubeLinkアイコン
     * デフォルトの音声認識アイコン or 音声認識Alexaアイコン
     */
    private void setShortcutIconResources(){
        for (int i = 0; i < mShortCutKeyList.size(); i++) {
            ShortcutKeyItem item = mShortCutKeyList.get(i);
            if(item.key == ShortcutKey.SOURCE){
                // TODO カスタムキー/YouTubeLinkアイコン切り替え 改善必要
                if(mYouTubeLinkStatus.isYouTubeLinkEnabled()){
                    item.imageResource = YOUTUBE_LINK_ICON;
                }
                else {
                    item.imageResource = CUSTOM_KEY_ICON;
                }
            }
            if(item.key==ShortcutKey.VOICE){
                if(mAlexaAvailableStatus.isVoiceRecognitionTypeAlexaAndAvailable()){
                    item.imageResource = R.drawable.p0167_alexabtn_1nrm;
                }else{
                    item.imageResource = R.drawable.p0162_vrbtn_1nrm;
                }
            }
            item.enabled = true;
        }
    }
}
