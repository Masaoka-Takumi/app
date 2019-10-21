package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;
import javax.inject.Named;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.preference.HdRadioSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;
import jp.pioneer.carsync.presentation.view.fragment.Screen;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.BackgroundImagePreviewFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.GuidanceVolumeDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.LocalDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.LoudnessDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.MenuDisplayLanguageDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.VideoPlayerDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AdasCameraPositionSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AdasSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AdasWarningSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AppSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AudioFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.CarSafetyFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.DabSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.DebugSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.EulaFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.FxFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.ImpactDetectionSettingsFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.InformationFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.InitialSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.KaraokeFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.LicenseFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.MessageFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.NavigationFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.ParkingSensorFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.PhoneSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.PrivacyPolicyFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.RadioFunctionSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.SystemFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.ThemeFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.VoiceSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.YouTubeLinkSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasBillingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasCalibrationSettingFittingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasCalibrationSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasManualFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasTutorialFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdasUsageCautionFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AdvancedAudioSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AlexaExampleUsageFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AlexaSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.AlexaSplashFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.BtDeviceListFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.BtDeviceSearchFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.ClassicBtDeviceListFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.CrossOverSettingsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.DimmerSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.DirectCallContactSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.DirectCallSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.EqProSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.EqProSettingZoomFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.EqQuickSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.EqSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.FaderBalanceSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.IlluminationColorSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.ImpactDetectionContactRegisterFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.ImpactDetectionContactSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.IncomingCallColorFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.IncomingCallPatternFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.IncomingMessageColorFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.LiveSimulationSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsEntranceFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SmallCarTaSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.ThemeSetFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.TodorokiSettingFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.UiColorSettingFragment;

import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;

/**
 * Created by NSW00_008316 on 2017/03/23.
 */

public class SettingsFragmentController {

    private static final String TAG_DIALOG_GUIDANCE_VOLUME = "guidance_volume";
    private static final String TAG_DIALOG_BEAT_BLASTER = "beat_blaster";
    private static final String TAG_DIALOG_LOUDNESS = "loudness";
    private static final String TAG_DIALOG_LOCAL = "local";
    private static final String TAG_DIALOG_MENU_DISPLAY_LANGUAGE = "menu_display_language";
    private static final String TAG_DIALOG_CAUTION = "caution_setting";
    private static final String TAG_DIALOG_STATUS_POPUP = "status_popup";
    private static final String TAG_DIALOG_SELECT = "select_popup";
    private static final String TAG_DIALOG_BACKGROUND_PREVIEW = "background_preview";
    private static final String[] KEY_DIALOGS = new String[]{
            TAG_DIALOG_GUIDANCE_VOLUME, TAG_DIALOG_BEAT_BLASTER, TAG_DIALOG_LOUDNESS,
            TAG_DIALOG_LOCAL, TAG_DIALOG_MENU_DISPLAY_LANGUAGE, TAG_DIALOG_CAUTION, TAG_DIALOG_STATUS_POPUP, TAG_DIALOG_SELECT,
            TAG_DIALOG_BACKGROUND_PREVIEW
    };
    @Inject
    @Named(CHILD_FRAGMENT_MANAGER)
    FragmentManager mFragmentManager;
    @IdRes private int mContainerViewId;

    @Inject
    public SettingsFragmentController() {
    }

    public void setContainerViewId(@IdRes int containerViewId) {
        mContainerViewId = containerViewId;
    }

    public ScreenId getScreenIdInContainer() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }

        return ((Screen) fragment).getScreenId();
    }

    public Fragment getContainerFragment() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment == null) {
            return null;
        }
        return fragment;
    }

    public boolean navigate(ScreenId screenId, Bundle args) {
        //表示しているダイアログを閉幕
        for (String key : KEY_DIALOGS) {
            Fragment dialog = mFragmentManager.findFragmentByTag(key);
            if (dialog instanceof DialogFragment) {
                ((DialogFragment) dialog).dismiss();
                break;
            }
        }
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnNavigateListener) {
            if (((OnNavigateListener) fragment).onNavigate(screenId, args)) {
                return true;
            }
        }

        SettingsParams params = SettingsParams.from(args);
        switch (screenId) {
            case SETTINGS_ENTRANCE:
                clearBackStack();
                animationReplaceFragment(createSettingsEntranceFragment(args), false);
                return true;
            case SETTINGS_SYSTEM:
                replaceFragment(createSystemFragment(args), true);
                return true;
            case SETTINGS_THEME:
                replaceFragment(createThemeFragment(args), true);
                return true;
            case SETTINGS_APP:
                replaceFragment(createAppSettingFragment(args), true);
                return true;
            case SETTINGS_FX:
                replaceFragment(createFxFragment(args), true);
                return true;
            case EQ_SETTING:
                replaceFragment(createEqSettingFragment(args), true);
                return true;
            case EQ_QUICK_SETTING:
                replaceFragment(createEqQuickSettingFragment(args), true);
                return true;
            case EQ_PRO_SETTING:
                replaceFragment(createEqProSettingFragment(args), true);
                return true;
            case EQ_PRO_SETTING_ZOOM:
                replaceFragment(createEqProSettingZoomFragment(args), true);
                return true;
            case LIVE_SIMULATION_SETTING:
                replaceFragment(createLiveSimulationSettingFragment(args), true);
                return true;
            case TODOROKI_SETTING:
                replaceFragment(createTodorokiSettingFragment(args), true);
                return true;
            case SMALL_CAR_TA_SETTING:
                replaceFragment(createSmallCarTaSettingFragment(args), true);
                return true;
            case KARAOKE_SETTING:
                replaceFragment(createKaraokeFragment(args), true);
                return true;
            case SETTINGS_AUDIO:
                replaceFragment(createAudioFragment(args), true);
                return true;
            case LOUDNESS_DIALOG:
                showLoudness(args);
                return true;
            case ADVANCED_AUDIO_SETTING:
                replaceFragment(createAdvancedAudioSettingFragment(args), true);
                return true;
            case CROSS_OVER_SETTINGS:
                replaceFragment(createCrossOverSettingsFragment(args), true);
                return true;
            case FADER_BALANCE_SETTING:
                replaceFragment(createAdvancedFaderBalanceSettingFragment(args), true);
                return true;
            case SETTINGS_INFORMATION:
                replaceFragment(createInformationFragment(args), true);
                return true;
            case LICENSE:
                replaceFragment(createLicenseFragment(args), true);
                return true;
            case EULA:
                replaceFragment(createEulaFragment(args), true);
                return true;
            case PRIVACY_POLICY:
                replaceFragment(createPrivacyPolicyFragment(args), true);
                return true;
            case THEME_SET_SETTING:
                replaceFragment(createThemeSetFragment(args), true);
                return true;
            case ILLUMINATION_COLOR_SETTING:
                replaceFragment(createIlluminationColorFragment(args), true);
                return true;
            case UI_COLOR_SETTING:
                replaceFragment(createUiColorFragment(args), true);
                return true;
            case ILLUMINATION_DIMMER_SETTING:
                replaceFragment(createDimmerSettingFragment(args), true);
                return true;
            case CAR_SAFETY_SETTINGS:
                replaceFragment(createCarSafetySettingsFragment(args), true, ScreenId.CAR_SAFETY_SETTINGS.name());
                return true;
            case IMPACT_DETECTION_SETTINGS:
                replaceFragment(createImpactDetectionSettingsFragment(args), true);
                return true;
            case IMPACT_DETECTION_CONTACT_SETTING:
                replaceFragment(createImpactDetectionContactSettingFragment(args), true);
                return true;
            case IMPACT_DETECTION_CONTACT_REGISTER_SETTING:
                replaceFragment(createImpactDetectionContactRegisterFragment(args), true);
                return true;
            case DIRECT_CALL_SETTING:
                replaceFragment(createDirectCallSettingFragment(args), true);
                return true;
            case DIRECT_CALL_CONTACT_SETTING:
                replaceFragment(createDirectCallContactSettingFragment(args), true);
                return true;
            case SETTINGS_NAVIGATION:
                replaceFragment(createNavigationFragment(args), true);
                return true;
            case GUIDANCE_VOLUME_DIALOG:
                showGuidanceVolume(args);
                return true;
            case SETTINGS_MESSAGE:
                replaceFragment(createMessageFragment(args), true);
                return true;
            case SETTINGS_RADIO:
                replaceFragment(createRadioFunctionSettingFragment(args), true);
                return true;
            case SETTINGS_DAB:
                replaceFragment(createDabSettingFragment(args), true);
                return true;
            case SETTINGS_HD_RADIO:
                replaceFragment(createHdRadioSettingFragment(args), true);
                return true;
            case LOCAL_DIALOG:
                showLocal(args);
                return true;
            case SETTINGS_VOICE:
                replaceFragment(createVoiceSettingFragment(args), true);
                return true;
            case SETTINGS_SYSTEM_INITIAL:
                replaceFragment(createInitialSettingFragment(args), true);
                return true;
            case MENU_DISPLAY_LANGUAGE_DIALOG:
                showMenuDisplayLanguage(args);
                return true;
            case SETTINGS_PHONE:
                replaceFragment(createPhoneSettingFragment(args), true);
                return true;
            case BT_DEVICE_LIST:
                replaceFragment(createBtDeviceSelectFragment(args), true);
                return true;
            case BT_DEVICE_SEARCH:
                replaceFragment(createBtDeviceSearchFragment(args), true);
                return true;
            case SETTINGS_ADAS:
                if (getScreenIdInContainer() == ScreenId.CALIBRATION_SETTING_FITTING
                ||getScreenIdInContainer() == ScreenId.ADAS_MANUAL) {
                    goBack(ScreenId.SETTINGS_ADAS.name());
                    //goBack(ScreenId.CAR_SAFETY_SETTINGS.name());
                    //replaceFragment(createAdasSettingFragment(args), true, ScreenId.SETTINGS_ADAS.name());
                    return false;
                } else {
                    replaceFragment(createAdasSettingFragment(args), true, ScreenId.SETTINGS_ADAS.name());
                    return true;
                }
            case CALIBRATION_SETTING:
                replaceFragment(createAdasCalibrationSettingFragment(args), true);
                return true;
            case CALIBRATION_SETTING_FITTING:
                replaceFragment(createAdasCalibrationSettingFittingFragment(args), true);
                return true;
            case ADAS_CAMERA_SETTING:
                replaceFragment(createAdasCameraPositionSettingFragment(args), true);
                return true;
            case ADAS_WARNING_SETTING:
                replaceFragment(createAdasWarningSettingFragment(args), true);
                return true;
            case ADAS_TUTORIAL:
                replaceFragment(createAdasTutorialFragment(args), true);
                return true;
            case ADAS_BILLING:
                replaceFragment(createAdasBillingFragment(args), true);
                return true;
            case INCOMING_CALL_PATTERN_SETTING:
                replaceFragment(createIncomingCallPatternFragment(args), true);
                return true;
            case INCOMING_CALL_COLOR_SETTING:
                replaceFragment(createIncomingCallColorFragment(args), true);
                return true;
            case INCOMING_MESSAGE_COLOR_SETTING:
                replaceFragment(createIncomingMessageColorFragment(args), true);
                return true;
            case PARKING_SENSOR_SETTING:
                replaceFragment(createParkingSensorFragment(args), true);
                return true;
            case ALEXA_SETTING:
                replaceFragment(createAlexaSettingFragment(args), true);
                return true;
            case ALEXA_SPLASH:
                replaceFragment(createAlexaSplashFragment(args), true);
                return true;
            case ALEXA_EXAMPLE_USAGE:
                replaceFragment(createAlexaExampleUsageFragment(args), true);
                return true;
            case DEBUG_SETTING:
                replaceFragment(createDebugSettingFragment(args), true);
                return true;
            case PAIRING_DEVICE_LIST:
                replaceFragment(createClassicBtDeviceListFragment(args), true);
                return true;
            case STATUS_DIALOG:
                showStatusPopup(fragment, args);
                return true;
            case SELECT_DIALOG:
                if (!isShowSelectDialog()) {
                    showSelectDialog(fragment, args);
                }
                return true;
            case VIDEO_PLAYER:
                replaceFragment(createVideoPlayerDialogFragment(args), true);
                return true;
            case ADAS_USAGE_CAUTION:
                replaceFragment(createAdasUsageCautionFragment(args), true);
                return true;
            case ADAS_MANUAL:
                replaceFragment(createAdasManualFragment(args), true);
                return true;
            case BACKGROUND_PREVIEW:
                showBackgroundPreviewDialog(fragment, args);
                return true;
            case YOUTUBE_LINK_SETTING:
                replaceFragment(createYouTubeLinkSettingFragment(args), true);
                return true;
            case PLAYER_CONTAINER:
            case HOME_CONTAINER:
                clearBackStack();
                break;
        }
        return false;
    }

    public boolean goBack() {
        Fragment fragment = mFragmentManager.findFragmentById(mContainerViewId);
        if (fragment instanceof OnGoBackListener) {
            if (((OnGoBackListener) fragment).onGoBack()) {
                return true;
            }
        }
        mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentById(mContainerViewId)).commit();
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
            return true;
        }
        return false;
    }

    public boolean goBack(String tag) {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate(tag, 0);
            return true;
        }
        return false;
    }

    public void clearBackStack() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = mFragmentManager.getBackStackEntryAt(0);
            mFragmentManager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    private void replaceFragment(Fragment fragment, boolean isAddToBackStack, String tag) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(tag);
        }
        tr.commit();
    }

    private void animationReplaceFragment(Fragment fragment, boolean isAddToBackStack) {
        FragmentTransaction tr = mFragmentManager.beginTransaction();
        tr.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        tr.replace(mContainerViewId, fragment);
        if (isAddToBackStack) {
            tr.addToBackStack(null);
        }
        tr.commit();
    }

    /**
     * ナビガイド音声設定ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showGuidanceVolume(Bundle args) {
        createGuidanceVolumeDialogFragment(args).show(mFragmentManager, TAG_DIALOG_GUIDANCE_VOLUME);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Loudness設定ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showLoudness(Bundle args) {
        createLoudnessDialogFragment(args).show(mFragmentManager, TAG_DIALOG_BEAT_BLASTER);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Local設定ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showLocal(Bundle args) {
        createLocalDialogFragment(args).show(mFragmentManager, TAG_DIALOG_LOCAL);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Menu表示言語設定ダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showMenuDisplayLanguage(Bundle args) {
        createMenuDisplayLanguageDialogFragment(args).show(mFragmentManager, TAG_DIALOG_MENU_DISPLAY_LANGUAGE);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * StatusPopupダイアログ表示.
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showStatusPopup(Fragment fragment, Bundle args) {
        createStatusPopupDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_STATUS_POPUP);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * StatusPopupダイアログ閉幕
     */
    public void dismissStatusPopup() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_STATUS_POPUP);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 選択ダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showSelectDialog(Fragment fragment, Bundle args) {
        createSelectDialogFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_SELECT);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 選択ダイアログ閉幕
     */
    public void dismissSelectDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_SELECT);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 選択ダイアログ表示確認
     *
     * @return 選択ダイアログが表示されているか否か
     */
    public boolean isShowSelectDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_SELECT) != null);
    }


    /**
     * 背景プレビューダイアログ表示
     *
     * @param args Bundle 引き継ぎ情報
     */
    public void showBackgroundPreviewDialog(Fragment fragment, Bundle args) {
        createBackgroundImagePreviewFragmentFragment(fragment, args).show(mFragmentManager, TAG_DIALOG_BACKGROUND_PREVIEW);
        mFragmentManager.executePendingTransactions();
    }

    /**
     * 背景プレビューダイアログ閉幕
     */
    public void dismissBackgroundPreviewDialog() {
        Fragment dialog = mFragmentManager.findFragmentByTag(TAG_DIALOG_BACKGROUND_PREVIEW);
        if (dialog instanceof DialogFragment) {
            ((DialogFragment) dialog).dismiss();
        }
    }

    /**
     * 背景プレビューダイアログ表示確認
     *
     * @return 背景プレビューダイアログが表示されているか否か
     */
    public boolean isShowBackgroundPreviewDialog() {
        return (mFragmentManager.findFragmentByTag(TAG_DIALOG_BACKGROUND_PREVIEW) != null);
    }
    @VisibleForTesting
    Fragment createSettingsEntranceFragment(Bundle args) {
        return SettingsEntranceFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSystemFragment(Bundle args) {
        return SystemFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createThemeFragment(Bundle args) {
        return ThemeFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAppSettingFragment(Bundle args) {
        return AppSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createFxFragment(Bundle args) {
        return FxFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEqSettingFragment(Bundle args) {
        return EqSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEqQuickSettingFragment(Bundle args) {
        return EqQuickSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEqProSettingFragment(Bundle args) {
        return EqProSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEqProSettingZoomFragment(Bundle args) {
        return EqProSettingZoomFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createLiveSimulationSettingFragment(Bundle args) {
        return LiveSimulationSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createTodorokiSettingFragment(Bundle args) {
        return TodorokiSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createSmallCarTaSettingFragment(Bundle args) {
        return SmallCarTaSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createKaraokeFragment(Bundle args) {
        return KaraokeFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAudioFragment(Bundle args) {
        return AudioFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdvancedAudioSettingFragment(Bundle args) {
        return AdvancedAudioSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createCrossOverSettingsFragment(Bundle args) {
        return CrossOverSettingsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdvancedFaderBalanceSettingFragment(Bundle args) {
        return FaderBalanceSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createInformationFragment(Bundle args) {
        return InformationFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createLicenseFragment(Bundle args) {
        return LicenseFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createEulaFragment(Bundle args) {
        return EulaFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPrivacyPolicyFragment(Bundle args) {
        return PrivacyPolicyFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createThemeSetFragment(Bundle args) {
        return ThemeSetFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createIlluminationColorFragment(Bundle args) {
        return IlluminationColorSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createUiColorFragment(Bundle args) {
        return UiColorSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDimmerSettingFragment(Bundle args) {
        return DimmerSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createCarSafetySettingsFragment(Bundle args) {
        return CarSafetyFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createImpactDetectionSettingsFragment(Bundle args) {
        return ImpactDetectionSettingsFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createImpactDetectionContactSettingFragment(Bundle args) {
        return ImpactDetectionContactSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createImpactDetectionContactRegisterFragment(Bundle args) {
        return ImpactDetectionContactRegisterFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDirectCallSettingFragment(Bundle args) {
        return DirectCallSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDirectCallContactSettingFragment(Bundle args) {
        return DirectCallContactSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createNavigationFragment(Bundle args) {
        return NavigationFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createMessageFragment(Bundle args) {
        return MessageFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createRadioFunctionSettingFragment(Bundle args) {
        return RadioFunctionSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createHdRadioSettingFragment(Bundle args) {
        return HdRadioSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDabSettingFragment(Bundle args) {
        return DabSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createVoiceSettingFragment(Bundle args) {
        return VoiceSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createInitialSettingFragment(Bundle args) {
        return InitialSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createPhoneSettingFragment(Bundle args) {
        return PhoneSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createBtDeviceSelectFragment(Bundle args) {
        return BtDeviceListFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createBtDeviceSearchFragment(Bundle args) {
        return BtDeviceSearchFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasSettingFragment(Bundle args) {
        return AdasSettingFragment.newInstance(args);
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
    Fragment createAdasCameraPositionSettingFragment(Bundle args) {
        return AdasCameraPositionSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasWarningSettingFragment(Bundle args) {
        return AdasWarningSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasTutorialFragment(Bundle args) {
        return AdasTutorialFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasBillingFragment(Bundle args) {
        return AdasBillingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createIncomingCallPatternFragment(Bundle args) {
        return IncomingCallPatternFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createIncomingCallColorFragment(Bundle args) {
        return IncomingCallColorFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createIncomingMessageColorFragment(Bundle args) {
        return IncomingMessageColorFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createParkingSensorFragment(Bundle args) {
        return ParkingSensorFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAlexaSettingFragment(Bundle args) {
        return AlexaSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAlexaSplashFragment(Bundle args) {
        return AlexaSplashFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAlexaExampleUsageFragment(Bundle args) {
        return AlexaExampleUsageFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createDebugSettingFragment(Bundle args) {
        return DebugSettingFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createClassicBtDeviceListFragment(Bundle args) {
        return ClassicBtDeviceListFragment.newInstance(args);
    }

    @VisibleForTesting
    DialogFragment createGuidanceVolumeDialogFragment(Bundle args) {
        return GuidanceVolumeDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createLoudnessDialogFragment(Bundle args) {
        return LoudnessDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createLocalDialogFragment(Bundle args) {
        return LocalDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createMenuDisplayLanguageDialogFragment(Bundle args) {
        return MenuDisplayLanguageDialogFragment.newInstance(null, args);
    }

    @VisibleForTesting
    DialogFragment createStatusPopupDialogFragment(Fragment fragment, Bundle args) {
        return StatusPopupDialogFragment.newInstance(fragment, args);
    }

    @VisibleForTesting
    DialogFragment createSelectDialogFragment(Fragment fragment, Bundle args) {
        return SingleChoiceDialogFragment.newInstance(fragment, args);
    }
    @VisibleForTesting
    DialogFragment createBackgroundImagePreviewFragmentFragment(Fragment fragment, Bundle args) {
        return BackgroundImagePreviewFragment.newInstance(fragment, args);
    }
    @VisibleForTesting
    Fragment createVideoPlayerDialogFragment(Bundle args) {
        return VideoPlayerDialogFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasUsageCautionFragment(Bundle args) {
        return AdasUsageCautionFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createAdasManualFragment(Bundle args) {
        return AdasManualFragment.newInstance(args);
    }

    @VisibleForTesting
    Fragment createYouTubeLinkSettingFragment(Bundle args) {
        return YouTubeLinkSettingFragment.newInstance(args);
    }
}
