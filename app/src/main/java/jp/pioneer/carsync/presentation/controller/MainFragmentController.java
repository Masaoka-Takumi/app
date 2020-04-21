package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.ActivityLifeCycle;
import jp.pioneer.carsync.domain.model.CarDeviceErrorType;
import jp.pioneer.carsync.presentation.presenter.MainPresenter;
import jp.pioneer.carsync.presentation.view.fragment.OnCloseDialogListener;
import jp.pioneer.carsync.presentation.view.fragment.OnExitSettingListener;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.OnShowDialogListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AccidentDetectDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AdasWarningDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AlexaFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AppConnectMethodDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.CautionDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.CustomKeySettingDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.ParkingSensorDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.PromptAuthorityPermissionDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.ReadingMessageDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SessionStoppedDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.VoiceRecognizeTypeSelectDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.YouTubeLinkContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.YouTubeLinkSearchItemDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.contacts.ContactsContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.HomeContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.OpeningEulaFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.OpeningFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.OpeningPrivacyPolicyFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.list.PlayerTabContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasCalibrationSettingFittingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasCalibrationSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.unconnected.UnconnectedContainerFragment;
import timber.log.Timber;

/**
 * 最上位コンテナの管理クラス
 */
@ActivityLifeCycle
public class MainFragmentController {

    private static final String TAG_DIALOG_CAUTION = "caution";
    private static final String TAG_DIALOG_ACCIDENT_DETECTION = "accident_detect";
    private static final String TAG_DIALOG_ALEXA = "alexa";
    private static final String TAG_DIALOG_SPEECH_RECOGNIZER = "speech_recognizer";
    private static final String TAG_DIALOG_SEARCH_CONTAINER = "search_container";
    private static final String TAG_DIALOG_CONTACT_CONTAINER = "contact_container";
    private static final String TAG_DIALOG_SESSION_STOP = "session_stop";
    private static final String TAG_DIALOG_PARKING_SENSOR = "parking_sensor";
    private static final String TAG_DIALOG_ADAS_WARNING = "adas_warning";
    private static final String TAG_DIALOG_READING_MESSAGE = "reading_message";
    private static final String TAG_DIALOG_SELECT_VOICE_TYPE = "select_voice_type";
    private static final String TAG_DIALOG_PROMPT_AUTHORITY_PERMISSION = "prompt_authority_permission";
    private static final String TAG_DIALOG_CUSTOM_KEY_SETTING = "custom_key_setting";
    private static final String TAG_DIALOG_YOUTUBE_LINK_CONTAINER = "youtube_link_container";
    private static final String TAG_DIALOG_YOUTUBE_LINK_SEARCH_ITEM = "youtube_link_search_item";
    private static final String TAG_DIALOG_VOICE_RECOGNIZE_TYPE_SELECT = "tag_dialog_voice_recognize_type_select";
    private static final String TAG_DIALOG_APP_CONNECT_METHOD = "app_connect_method";
    private static final String[] KEY_DEVICE_ERROR_DIALOGS = new String[]{
        MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE, CarDeviceErrorType.AMP_ERROR.toString(), CarDeviceErrorType.CHECK_USB.toString(), CarDeviceErrorType.CHECK_TUNER.toString(), CarDeviceErrorType.CHECK_ANTENNA.toString()
    };
    private static final String TAG_DIALOG_NORMAL = "dialog_normal";
    private static final String[] KEY_DIALOGS = new String[]{
            TAG_DIALOG_NORMAL, TAG_DIALOG_ALEXA, TAG_DIALOG_CUSTOM_KEY_SETTING,TAG_DIALOG_YOUTUBE_LINK_SEARCH_ITEM,TAG_DIALOG_VOICE_RECOGNIZE_TYPE_SELECT
    };
    @Inject FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;
    private Handler handler = new Handler(Looper.getMainLooper());
    @Inject
    public MainFragmentController() {
    }

    /**
     * コンテナのViewIDを設定
     *
     * @param containerViewId ResourceID
     */
    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    /**
     * コンテナ内のScreenId取得
     *
     * @return ScreenId 現在表示されている画面ID
     */
    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    /**
     * コンテナ内のFragment取得
     *
     * @return Fragment 現在表示されている画面
     */
    public Fragment getScreenInContainer() {
        return mFragmentManager.findFragmentById(mContainerViewId);
    }

    /**
     * 画面遷移
     *
     * @param screenId 遷移先ID
     * @param args     引き継ぎ情報
     * @return 遷移したか否か
     */
    public boolean navigate(ScreenId screenId, Bundle args) {
        // 検索ダイアログは他ダイアログよりも優先
        Fragment searchDialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SEARCH_CONTAINER);
        if (searchDialog instanceof OnNavigateListener) {
            if (((OnNavigateListener) searchDialog).onNavigate(screenId, args)) {
                return true;
            } else {
                ((DialogFragment) searchDialog).dismiss();
            }
        }
        // 電話帳ダイアログは他ダイアログよりも優先
        Fragment contactDialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_CONTACT_CONTAINER);
        if (contactDialog instanceof OnNavigateListener) {
            if (((OnNavigateListener) contactDialog).onNavigate(screenId, args)) {
                return true;
            } else {
                ((DialogFragment) contactDialog).dismiss();
            }
        }
        // YouTubeLinkダイアログ内の画面遷移、YouTubeLinkダイアログ閉じる処理
        Fragment youtubeLinkDialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_YOUTUBE_LINK_CONTAINER);
        if(youtubeLinkDialog instanceof OnNavigateListener){
            if( ((OnNavigateListener) youtubeLinkDialog).onNavigate(screenId, args)){
                Timber.i("YouTubeLinkContainer Fragment Change");
                return true;
            }else{
                if(youtubeLinkDialog instanceof YouTubeLinkContainerFragment){
                    Timber.i("YouTubeLinkContainer close");
                    // YouTubeLink画面はラストソース復帰で閉じる
                    ((YouTubeLinkContainerFragment) youtubeLinkDialog).closeContainerDialogByChangeLastSource();
                    ((YouTubeLinkContainerFragment) youtubeLinkDialog).closeContainerDialogResetLastSource();
                }
            }
        }

        //表示しているダイアログを閉幕
        Fragment dialog;
        for (String key : KEY_DIALOGS) {
            dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof DialogFragment) {
                ((DialogFragment) dialog).dismiss();
                break;
            }
        }
        // 子View内の画面遷移がある場合、優先的に画面遷移
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        switch (screenId) {
            case OPENING:
                replaceFragment(createOpeningFragment(args), false);
                return true;
            case OPENING_EULA:
                replaceFragment(createOpeningEulaFragment(args), false);
                return true;
            case OPENING_PRIVACY_POLICY:
                replaceFragment(createOpeningPrivacyPolicyFragment(args), false);
                return true;
            case HOME_CONTAINER:
                replaceFragment(createHomeContainerFragment(args), false);
                return true;
            case PLAYER_CONTAINER:
                replaceFragment(createPlayerContainerFragment(args), false);
                return true;
            case CONTACTS_CONTAINER:
                showContactContainer(args);
                return true;
            case SEARCH_CONTAINER:
                showSearchContainer(args);
                return true;
            case SETTINGS_CONTAINER:
                replaceFragment(createSettingsContainerFragment(args), false);
                return true;
            case UNCONNECTED_CONTAINER:
                replaceFragment(createUnconnectedContainerFragment(args), false);
                return true;
            case CALIBRATION_SETTING:
                replaceFragment(createAdasCalibrationSettingFragment(args), false);
                return true;
            case CALIBRATION_SETTING_FITTING:
                replaceFragment(createAdasCalibrationSettingFittingFragment(args), true);
                return true;
            case IMPACT_DETECTION:
                showAccidentDetect(args);
                return true;
            case ADAS_WARNING:
                showAdasWarning(args);
                return true;
            case PARKING_SENSOR:
                showParkingSensor(args);
                return true;
            case CAR_DEVICE_ERROR:
                showCarDeviceErrorDialog(args, args.getString(StatusPopupDialogFragment.TAG));
                return true;
            case SXM_SUBSCRIPTION_UPDATE:
                showCarDeviceErrorDialog(args, MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
                return true;
            case USB_ERROR:
                showUsbError(args, MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE);
                return true;
            case MAIN_STATUS_DIALOG:
                showCarDeviceErrorDialog(args, TAG_DIALOG_NORMAL);
                return true;
            case READING_MESSAGE:
                showReadingMessage(args);
                return true;
            case SPEECH_RECOGNIZER:
                showSpeechRecognizerDialog(args);
                return true;
            case ALEXA:
                showAlexaDialog(args);
                return true;
            case SELECT_DIALOG:
                showSelectDialog(fragment,args);
                return true;
            case PROMPT_AUTHORITY_PERMISSION:
                if(!isShowPromptAuthorityPermissionDialog()) {
                    showPromptAuthorityPermissionDialog(args);
                }
                return true;
            case CUSTOM_KEY_SETTING:
                showCustomKeySettingDialog(fragment, args);
                return true;
            case YOUTUBE_LINK_CONTAINER:
                showYouTubeLinkContainerDialog(fragment, args);
                return true;
            case YOUTUBE_LINK_SEARCH_ITEM:
                showYouTubeLinkSearchItemDialog(fragment, args);
                return true;
            case VOICE_RECOGNIZE_TYPE_DIALOG:
                showVoiceRecognizeTypeSelectDialog(fragment, args);
                return true;
            default:
                return false;
        }
    }

    /**
     * 背景透過ダイアログの表示確認
     *
     * @return ダイアログ有無
     */
    public boolean isShowListDialog() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnShowDialogListener) {
            if (((OnShowDialogListener) fragment).isShowDialog()) {
                return true;
            }
        }
        return isShowSearchContainer()||isShowContactContainer();
    }

    /**
     * ダイアログのクローズ.
     *
     * @param screenId id
     * @return クローズしたか否か
     */
    public boolean closeDialog(ScreenId screenId) {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnCloseDialogListener) {
            return ((OnCloseDialogListener) fragment).onClose(screenId);
        }
        return false;
    }

    /**
     * Caution表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showCaution(Bundle args) {
        if(isShowSessionStopped()){
            dismissSessionStopped();
        }

        createCautionDialogFragment(args).show(mFragmentManager, TAG_DIALOG_CAUTION);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Caution閉幕
     */
    public void dismissCaution() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_CAUTION);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * Caution表示確認
     *
     * @return Cautionが表示されているか否か
     */
    public boolean isShowCaution() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_CAUTION) != null);
    }

    /**
     * Alexaダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showAlexaDialog(Bundle args) {
        //createSpeechRecognizerDialogFragment(args).show(mFragmentManager, TAG_DIALOG_SPEECH_RECOGNIZER);
        //mFragmentManager.executePendingTransactions();
        //画面OFF時のIllegalStateException回避
        DialogFragment dialog = createAlexaFragment(args);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(dialog, TAG_DIALOG_ALEXA);
        ft.commitAllowingStateLoss();
    }

    /**
     * Alexaダイアログ閉幕
     */
    public void dismissAlexaDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_ALEXA);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }
    /**
     * Alexaダイアログ表示確認
     *
     * @return 音声認識結果が表示されているか否か
     */
    public boolean isShowAlexaDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_ALEXA) != null);
    }

    /**
     * 音声認識ダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSpeechRecognizerDialog(Bundle args) {
        //createSpeechRecognizerDialogFragment(args).show(mFragmentManager, TAG_DIALOG_SPEECH_RECOGNIZER);
        //mFragmentManager.executePendingTransactions();
        //画面OFF時のIllegalStateException回避
        DialogFragment dialog = createSpeechRecognizerDialogFragment(args);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(dialog, TAG_DIALOG_SPEECH_RECOGNIZER);
        ft.commitAllowingStateLoss();
    }

    /**
     * 音声認識ダイアログ閉幕
     */
    public void dismissSpeechRecognizerDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SPEECH_RECOGNIZER);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }
    /**
     * 音声認識ダイアログ表示確認
     *
     * @return 音声認識結果が表示されているか否か
     */
    public boolean isShowSpeechRecognizerDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_SPEECH_RECOGNIZER) != null);
    }

    /**
     * 音声認識ダイアログ取得
     *
     * @return ダイアログ
     */
    public Fragment getSpeechRecognizerDialog() {
        return mFragmentManager.findFragmentByTag(TAG_DIALOG_SPEECH_RECOGNIZER);
    }

    /**
     * 音声認識結果表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSearchContainer(Bundle args) {
        createSearchContainerDialogFragment(args).show(mFragmentManager, TAG_DIALOG_SEARCH_CONTAINER);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 音声認識結果閉幕
     */
    public void dismissSearchContainer() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SEARCH_CONTAINER);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 音声認識結果表示確認
     *
     * @return 音声認識結果が表示されているか否か
     */
    public boolean isShowSearchContainer() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_SEARCH_CONTAINER) != null);
    }

    /**
     * 電話帳表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showContactContainer(Bundle args) {
        createContactsContainerFragment(args).show(mFragmentManager, TAG_DIALOG_CONTACT_CONTAINER);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 電話帳閉幕
     */
    public void dismissContactContainer() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_CONTACT_CONTAINER);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 電話帳示確認
     *
     * @return 電話帳が表示されているか否か
     */
    public boolean isShowContactContainer() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_CONTACT_CONTAINER) != null);
    }

    /**
     * 事故通知ダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showAccidentDetect(Bundle args) {
        //衝突検知でのActivity復帰でソフトウェアキーボードが消えないことがあるため、Handlerで実行
        handler.post(new Runnable() {
            @Override
            public void run() {
                createAccidentDetectDialogFragment(args).show(mFragmentManager, TAG_DIALOG_ACCIDENT_DETECTION);
            }
        });
        //パーキングセンサー閉幕後の再表示でエラーが発生する
        //mFragmentManager.executePendingTransactions();
    }

    /**
     * 事故通知ダイアログ閉幕
     */
    public void dismissAccidentDetect() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_ACCIDENT_DETECTION);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 事故通知ダイアログ表示確認
     *
     * @return 事故通知ダイアログが表示されているか否か
     */
    public boolean isShowAccidentDetect() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_ACCIDENT_DETECTION) != null);
    }

    /**
     * ADAS警告ダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showAdasWarning(Bundle args) {
        createAdasWarningDialogFragment(args).show(mFragmentManager, TAG_DIALOG_ADAS_WARNING);
        //即座実行が必要でなければ不要
        //mFragmentManager.executePendingTransactions();
    }

    /**
     * ADAS警告ダイアログ閉幕
     */
    public void dismissAdasWarning() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_ADAS_WARNING);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * ADAS警告ダイアログ表示確認
     *
     * @return ADAS警告ダイアログが表示されているか否か
     */
    public boolean isShowAdasWarning() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_ADAS_WARNING) != null);
    }

    /**
     * パーキングセンサーダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showParkingSensor(Bundle args) {
        createParkingSensorDialogFragment(args).show(mFragmentManager, TAG_DIALOG_PARKING_SENSOR);
        //即座実行が必要でなければ不要
        //mFragmentManager.executePendingTransactions();
    }

    /**
     * パーキングセンサーダイアログ閉幕
     */
    public void dismissParkingSensor() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_PARKING_SENSOR);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * パーキングセンサーダイアログ表示確認
     *
     * @return パーキングセンサーダイアログが表示されているか否か
     */
    public boolean isShowParkingSensor() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_PARKING_SENSOR) != null);
    }

    /**
     * ReadingMessage表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showReadingMessage(Bundle args) {
        createReadingMessageDialogFragment(args).show(mFragmentManager, TAG_DIALOG_READING_MESSAGE);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * ReadingMessage閉幕
     */
    public void dismissReadingMessage() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_READING_MESSAGE);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * ReadingMessage表示確認
     *
     * @return ReadingMessageが表示されているか否か
     */
    public boolean isShowReadingMessage() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_READING_MESSAGE) != null);
    }

    /**
     * CarDeviceErrorDialog表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showCarDeviceErrorDialog(Bundle args, String tag) {
        createCarDeviceErrorDialogFragment(args).show(mFragmentManager, tag);
        //パーキングセンサー解除直後の表示でエラーが発生する
        //即座実行が必要でなければ不要
        //mFragmentManager.executePendingTransactions();
    }

    /**
     * CarDeviceErrorDialog閉幕
     */
    public void dismissCarDeviceErrorDialog(String tag) {
        Fragment dialog = mFragmentManager.findFragmentByTag(tag);
        if (dialog instanceof DialogFragment) {
            StatusPopupDialogFragment dialogFragment = (StatusPopupDialogFragment) dialog;
            dialogFragment.callbackClose();
        }
    }

    /**
     * 全てのCarDeviceErrorDialog閉幕
     */
    public void dismissCarDeviceErrorDialog() {
        for (String key : KEY_DEVICE_ERROR_DIALOGS) {
            Fragment dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof DialogFragment) {
                StatusPopupDialogFragment dialogFragment = (StatusPopupDialogFragment) dialog;
                dialogFragment.callbackClose();
            }
        }
    }

    /**
     * CarDeviceErrorDialog表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showUsbError(Bundle args, String tag) {
        if(isShowSessionStopped()){
            dismissSessionStopped();
        }
        dismissCarDeviceErrorDialog();

        createCarDeviceErrorDialogFragment(args).show(mFragmentManager, tag);
    }

    /**
     * 背面のCarDeviceErrorDialogを隠す
     */
    public void hideCarDeviceErrorDialog(String tag) {
        if (tag == null) {
            dismissCarDeviceErrorDialog();
        }
        for (String key : KEY_DEVICE_ERROR_DIALOGS) {

            Fragment dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof DialogFragment) {
                DialogFragment dialogFragment = ((DialogFragment) dialog);
                if(dialogFragment.getDialog()!=null) {
                    if (!key.equals(tag)) {
                        dialogFragment.getDialog().hide();
                    } else {
                        dialogFragment.getDialog().show();
                    }
                }
            }
        }
    }

    /**
     * CarDeviceErrorDialog再表示
     */
    public void reshowCarDeviceErrorDialog(String tag) {
        Fragment dialog = mFragmentManager.findFragmentByTag(tag);
        if (dialog instanceof DialogFragment) {
            DialogFragment dialogFragment = ((DialogFragment) dialog);
            if (dialogFragment.getDialog() != null) {
                dialogFragment.getDialog().show();
            }
        }
    }

    /**
     * CarDeviceErrorDialog表示確認
     *
     * @return CarDeviceErrorDialogが表示されているか否か
     */
    public boolean isShowCarDeviceErrorDialog(String tag) {
        return (mFragmentManager.findFragmentByTag(tag) != null);
    }

    /**
     * 全てのCarDeviceErrorDialog表示確認
     *
     * @return CarDeviceErrorDialogが表示されているか否か
     */
    public boolean isShowCarDeviceErrorDialog() {
        return (mFragmentManager.findFragmentByTag(CarDeviceErrorType.AMP_ERROR.toString()) != null)
                || (mFragmentManager.findFragmentByTag(CarDeviceErrorType.CHECK_USB.toString()) != null)
                || (mFragmentManager.findFragmentByTag(CarDeviceErrorType.CHECK_TUNER.toString()) != null)
                || (mFragmentManager.findFragmentByTag(CarDeviceErrorType.CHECK_ANTENNA.toString()) != null)
                || (mFragmentManager.findFragmentByTag(MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE) != null);
    }

    /**
     * Selectダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSelectDialog(Fragment fragment, Bundle args) {
        createSelectDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_SELECT_VOICE_TYPE);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * PromptAuthorityPermissionダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showPromptAuthorityPermissionDialog(Bundle args) {
        createPromptAuthorityPermissionDialogFragment(args).show(mFragmentManager, TAG_DIALOG_PROMPT_AUTHORITY_PERMISSION);
        mFragmentManager.executePendingTransactions();
    }
    /**
     * PromptAuthorityPermissionダイアログ閉幕
     */
    public void dismissPromptAuthorityPermissionDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_PROMPT_AUTHORITY_PERMISSION);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * PromptAuthorityPermissionダイアログ表示確認
     *
     * @return PromptAuthorityPermissionダイアログが表示されているか否か
     */
    public boolean isShowPromptAuthorityPermissionDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_PROMPT_AUTHORITY_PERMISSION) != null);
    }

    /**
     * 画面戻し処理
     *
     * @return 戻ったか否か
     */
    public boolean goBack() {
        Fragment dialog;
        dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SEARCH_CONTAINER);
        if (dialog instanceof OnGoBackListener) {
            if (((OnGoBackListener) dialog).onGoBack()) {
                return true;
            } else {
                ((DialogFragment) dialog).dismiss();
                return true;
            }
        }

        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnGoBackListener) {
            if (((OnGoBackListener) fragment).onGoBack()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 設定画面解除処理.
     */
    public void exitSetting() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnExitSettingListener) {
            ((OnExitSettingListener) fragment).onExitSetting();
        }
    }

    /**
     * 車載機切断ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSessionStopped(Bundle args) {
        createSessionStopDialogFragment(args).show(mFragmentManager, TAG_DIALOG_SESSION_STOP);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 車載機切断ダイアログ確認.
     *
     * @return 車載機切断ダイアログが表示されているか否か
     */
    public boolean isShowSessionStopped() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_SESSION_STOP) != null);
    }

    /**
     * 車載機切断ダイアログ閉幕
     */
    public void dismissSessionStopped() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SESSION_STOP);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * App連携方法ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showAppConnectMethodDialog(Bundle args) {
        createAppConnectMethodDialogFragment(args).show(mFragmentManager, TAG_DIALOG_APP_CONNECT_METHOD);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * App連携方法ダイアログ確認.
     *
     * @return App連携方法ダイアログが表示されているか否か
     */
    public boolean isShowAppConnectMethodDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_APP_CONNECT_METHOD) != null);
    }

    /**
     * App連携方法ダイアログ閉幕
     */
    public void dismissAppConnectMethodDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_APP_CONNECT_METHOD);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }

        tr.commit();
    }

    private void animationReplaceFragment(Fragment fragment, boolean isAddToBackStack) {
        //Container間のreplaceで画面が一瞬消えるため、アニメーションがちらつく
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }
        tr.commitAllowingStateLoss();
    }
    /**
     * カスタムキー割当画面表示
     * @param fragment 表示する画面のインスタンス
     * @param args      Bundle 引き継ぎ情報
     */
    private void showCustomKeySettingDialog(Fragment fragment, Bundle args){
        createCustomKeyDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_CUSTOM_KEY_SETTING);
    }

    /**
     * YouTubeLinkContainer画面表示
     * @param fragment 表示する画面のインスタンス
     * @param args      Bundle 引き継ぎ情報
     */
    private void showYouTubeLinkContainerDialog(Fragment fragment, Bundle args){
        createYouTubeLinkContainerDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_YOUTUBE_LINK_CONTAINER);
    }

    /**
     * YouTubeLinkSearchItemDialog画面表示
     * @param fragment 表示する画面のインスタンス
     * @param args      Bundle 引き継ぎ情報
     */
    private void showYouTubeLinkSearchItemDialog(Fragment fragment, Bundle args){
        createYouTubeLinkSearchItemDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_YOUTUBE_LINK_SEARCH_ITEM);
    }

    /**
     * showVoiceRecognizeTypeSelectDialog画面表示
     * @param fragment 表示する画面のインスタンス
     * @param args      Bundle 引き継ぎ情報
     */
    private void showVoiceRecognizeTypeSelectDialog(Fragment fragment, Bundle args){
        createVoiceRecognizeTypeSelectDialog(fragment, args).show(mFragmentManager, TAG_DIALOG_VOICE_RECOGNIZE_TYPE_SELECT);
    }

    @VisibleForTesting
    Fragment createOpeningFragment(Bundle args) {
        return OpeningFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createOpeningEulaFragment(Bundle args) {
        return OpeningEulaFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createOpeningPrivacyPolicyFragment(Bundle args) {
        return OpeningPrivacyPolicyFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createHomeContainerFragment(Bundle args) {
        return HomeContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPlayerContainerFragment(Bundle args) {
        return PlayerContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createCautionDialogFragment(Bundle args) {
        return CautionDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createSessionStopDialogFragment(Bundle args) {
        return SessionStoppedDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createAppConnectMethodDialogFragment(Bundle args) {
        return AppConnectMethodDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createSpeechRecognizerDialogFragment(Bundle args) {
        return SpeechRecognizerDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createSearchContainerDialogFragment(Bundle args) {
        return SearchContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createContactsContainerFragment(Bundle args) {
        return ContactsContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createAccidentDetectDialogFragment(Bundle args) {
        return AccidentDetectDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createAdasWarningDialogFragment(Bundle args) {
        return AdasWarningDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createParkingSensorDialogFragment(Bundle args) {
        return ParkingSensorDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createPlayerTabContainerDialogFragment(Bundle args) {
        return PlayerTabContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createReadingMessageDialogFragment(Bundle args) {
        return ReadingMessageDialogFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSettingsContainerFragment(Bundle args) {
        return SettingsContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createUnconnectedContainerFragment(Bundle args) {
        return UnconnectedContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createCarDeviceErrorDialogFragment(Bundle args) {
        return StatusPopupDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    Fragment createAdasCalibrationSettingFragment(Bundle args) {
        return AdasCalibrationSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasCalibrationSettingFittingFragment(Bundle args) {
        return AdasCalibrationSettingFittingFragment.newInstance(args);
    }
    @VisibleForTesting
    DialogFragment createAlexaFragment(Bundle args) {
        return AlexaFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createSelectDialogFragment(Fragment fragment, Bundle args) {
        return SingleChoiceDialogFragment.newInstance(fragment, args);
    }

    @VisibleForTesting
    DialogFragment createPromptAuthorityPermissionDialogFragment(Bundle args) {
        return PromptAuthorityPermissionDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createCustomKeyDialogFragment(Fragment fragment, Bundle args){
        return CustomKeySettingDialogFragment.newInstance(fragment, args);
    }

    @VisibleForTesting
    DialogFragment createYouTubeLinkContainerDialogFragment(Fragment fragment, Bundle args){
        return YouTubeLinkContainerFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createYouTubeLinkSearchItemDialogFragment(Fragment fragment, Bundle args){
        return YouTubeLinkSearchItemDialogFragment.newInstance(fragment, args);
    }

    @VisibleForTesting
    DialogFragment createVoiceRecognizeTypeSelectDialog(Fragment fragment, Bundle args){
        return VoiceRecognizeTypeSelectDialogFragment.newInstance(fragment, args);
    }
}