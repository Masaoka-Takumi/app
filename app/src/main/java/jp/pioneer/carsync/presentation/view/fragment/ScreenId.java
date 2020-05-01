package jp.pioneer.carsync.presentation.view.fragment;

import jp.pioneer.carsync.domain.model.VoiceRecognitionSearchType;

/**
 * Created by BP06565 on 2017/02/24.
 */

public enum ScreenId {
    HOME_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                              // HOME
    CAUTION(VoiceRecognitionSearchType.GLOBAL),                                     // CAUTION
    OPENING(VoiceRecognitionSearchType.GLOBAL),                                     // OPENING
    HOME(VoiceRecognitionSearchType.GLOBAL),                                        // Communication Center
    OPENING_EULA(VoiceRecognitionSearchType.GLOBAL),                                // Opening Eula
    OPENING_PRIVACY_POLICY(VoiceRecognitionSearchType.GLOBAL),                      // Opening Privacy Policy
    UNCONNECTED_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                       // Unconnected
    TIPS(VoiceRecognitionSearchType.GLOBAL),                                        // Tips
    TIPS_WEB(VoiceRecognitionSearchType.GLOBAL),                                    // Tips Web
    EASY_PAIRING(VoiceRecognitionSearchType.GLOBAL),                                // EasyPairing
    PAIRING_SELECT(VoiceRecognitionSearchType.GLOBAL),                              // Pairing Select
    PRIVACY_POLICY(VoiceRecognitionSearchType.GLOBAL),                              // Privacy Policy (Flurry)
    LICENSE(VoiceRecognitionSearchType.GLOBAL),                                     // License
    EULA(VoiceRecognitionSearchType.GLOBAL),                                        // EULA
    PLAYER_CONTAINER(VoiceRecognitionSearchType.LOCAL),                             // Player
    SOURCE_SELECT_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                     // Source Select Container
    SOURCE_SELECT(VoiceRecognitionSearchType.GLOBAL),                               // Source Select
    SOURCE_APP_SETTING(VoiceRecognitionSearchType.GLOBAL),                          // Source App Setting
    RADIO(VoiceRecognitionSearchType.GLOBAL),                                       // Radio
    //    RADIO_PRESET(VoiceRecognitionSearchType.GLOBAL),                          // Radio Preset
    RADIO_PTY_SELECT(VoiceRecognitionSearchType.GLOBAL),                            // Radio Pty Slect
    RADIO_PTY_SEARCH(VoiceRecognitionSearchType.GLOBAL),                            // Radio Pty Search
    RADIO_LIST_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                        // Radio list
    RADIO_BSM(VoiceRecognitionSearchType.GLOBAL),                                   // Radio BSM
    RADIO_PRESET_LIST(VoiceRecognitionSearchType.GLOBAL),                           // Preset list
    RADIO_FAVORITE_LIST(VoiceRecognitionSearchType.GLOBAL),                         // Favorite list
    DAB(VoiceRecognitionSearchType.GLOBAL),                                         // DAB
    DAB_SERVICE_LIST(VoiceRecognitionSearchType.GLOBAL),                            // DAB Service list
    DAB_PTY_LIST(VoiceRecognitionSearchType.GLOBAL),                                  // DAB Pty list
    DAB_ENSEMBLE_LIST(VoiceRecognitionSearchType.GLOBAL),                           // DAB Ensemble list
    HD_RADIO(VoiceRecognitionSearchType.GLOBAL),                                    // HD Radio
    SIRIUS_XM(VoiceRecognitionSearchType.GLOBAL),                                   // Sirius XM
    USB(VoiceRecognitionSearchType.GLOBAL),                                         // USB
    USB_LIST(VoiceRecognitionSearchType.GLOBAL),                                    // USB List
    CD(VoiceRecognitionSearchType.GLOBAL),                                          // CD
    PANDORA(VoiceRecognitionSearchType.GLOBAL),                                     // Pandora
    SPOTIFY(VoiceRecognitionSearchType.GLOBAL),                                     // Spotify
    BT_AUDIO(VoiceRecognitionSearchType.GLOBAL),                                    // BT Audio
    NOW_PLAYING_LIST(VoiceRecognitionSearchType.LOCAL),                             // NowPlaying List
    ANDROID_MUSIC(VoiceRecognitionSearchType.LOCAL),                                // Android Local
    TI(VoiceRecognitionSearchType.GLOBAL),                                         	// TI
    AUX(VoiceRecognitionSearchType.GLOBAL),                                         // AUX
    SOURCE_OFF(VoiceRecognitionSearchType.GLOBAL),                                  // OFF
    UNSUPPORTED(VoiceRecognitionSearchType.GLOBAL),                                 // Unsupported
    BT_DEVICE_LIST(VoiceRecognitionSearchType.GLOBAL),                              // BtDevice list
    BT_DEVICE_SEARCH(VoiceRecognitionSearchType.GLOBAL),                            // BtDevice search
    PLAYER_LIST_CONTAINER(VoiceRecognitionSearchType.LOCAL),                        // Player list
    ARTIST_LIST(VoiceRecognitionSearchType.LOCAL),                                  // Artist
    ARTIST_ALBUM_LIST(VoiceRecognitionSearchType.LOCAL),                            // Artist-Album
    ARTIST_ALBUM_SONG_LIST(VoiceRecognitionSearchType.LOCAL),                       // Artist-Album-Song
    ALBUM_LIST(VoiceRecognitionSearchType.LOCAL),                                   // Album
    ALBUM_SONG_LIST(VoiceRecognitionSearchType.LOCAL),                              // Album-Song
    SONG_LIST(VoiceRecognitionSearchType.LOCAL),                                    // Song
    PLAYLIST_LIST(VoiceRecognitionSearchType.LOCAL),                                // Playlist
    PLAYLIST_SONG_LIST(VoiceRecognitionSearchType.LOCAL),                           // Playlist-Song
    GENRE_LIST(VoiceRecognitionSearchType.LOCAL),                                   // Genre
    GENRE_SONG_LIST(VoiceRecognitionSearchType.LOCAL),                              // Genre-Song
    CONTACTS_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                          // Contacts
    CONTACTS_LIST(VoiceRecognitionSearchType.GLOBAL),                               // Contact list
    CONTACTS_HISTORY(VoiceRecognitionSearchType.GLOBAL),                            // History
    CONTACTS_FAVORITE(VoiceRecognitionSearchType.GLOBAL),                           // Favorite
    SEARCH_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                            // Search
    SEARCH_CONTACT_RESULTS(VoiceRecognitionSearchType.GLOBAL),                      // Search contact
    SEARCH_MUSIC_RESULTS(VoiceRecognitionSearchType.LOCAL),                         // Search music
    SEARCH_MUSIC_ARTIST_ALBUM_LIST(VoiceRecognitionSearchType.LOCAL),               // Search Artist-Album
    SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST(VoiceRecognitionSearchType.LOCAL),          // Search Artist-Album-Song
    SEARCH_MUSIC_ALBUM_SONG_LIST(VoiceRecognitionSearchType.LOCAL),                 // Search Album-Song
    SETTINGS_ENTRANCE(VoiceRecognitionSearchType.GLOBAL),                           // Settings Entrance
    SETTINGS_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                          // Settings Top
    SETTINGS_SYSTEM(VoiceRecognitionSearchType.GLOBAL),                             // System
    SETTINGS_SYSTEM_INITIAL(VoiceRecognitionSearchType.GLOBAL),                     // System Initial Setting
    MENU_DISPLAY_LANGUAGE_DIALOG(VoiceRecognitionSearchType.GLOBAL),                // Menu Display Language Dialog
    SETTINGS_THEME(VoiceRecognitionSearchType.GLOBAL),                              // Theme
    THEME_SET_SETTING(VoiceRecognitionSearchType.GLOBAL),                           // Theme set
    ILLUMINATION_COLOR_SETTING(VoiceRecognitionSearchType.GLOBAL),                  // illumi color
    UI_COLOR_SETTING(VoiceRecognitionSearchType.GLOBAL),                            // UI color
    ILLUMINATION_DIMMER_SETTING(VoiceRecognitionSearchType.GLOBAL),                 // Dimmer
    SETTINGS_NAVIGATION(VoiceRecognitionSearchType.GLOBAL),                         // Navigation app
    GUIDANCE_VOLUME_DIALOG(VoiceRecognitionSearchType.GLOBAL),                      // Guidance Volume Dialog
    SETTINGS_MESSAGE(VoiceRecognitionSearchType.GLOBAL),                            // Message app
    SETTINGS_APP(VoiceRecognitionSearchType.GLOBAL),                                // App
    DIRECT_CALL_SETTING(VoiceRecognitionSearchType.GLOBAL),                         // Phone Direct Call Setting
    DIRECT_CALL_CONTACT_SETTING(VoiceRecognitionSearchType.GLOBAL),                 // Phone Contact Setting
    CAR_SAFETY_SETTINGS(VoiceRecognitionSearchType.GLOBAL),                         // CarSafety settings
    IMPACT_DETECTION_SETTINGS(VoiceRecognitionSearchType.GLOBAL),                   // Impact detection settings
    IMPACT_DETECTION_CONTACT_SETTING(VoiceRecognitionSearchType.GLOBAL),            // Impact detection contact setting
    IMPACT_DETECTION_CONTACT_REGISTER_SETTING(VoiceRecognitionSearchType.GLOBAL),   // Impact detection contact register setting
    SETTINGS_FX(VoiceRecognitionSearchType.GLOBAL),                                 // Fx
    EQ_SETTING(VoiceRecognitionSearchType.GLOBAL),                                  // Eq Setting
    EQ_QUICK_SETTING(VoiceRecognitionSearchType.GLOBAL),                            // Eq Quick Setting
    EQ_PRO_SETTING(VoiceRecognitionSearchType.GLOBAL),                              // Eq Pro Setting
    EQ_PRO_SETTING_ZOOM(VoiceRecognitionSearchType.GLOBAL),                         // Eq Pro Setting Zoom
    LIVE_SIMULATION_SETTING(VoiceRecognitionSearchType.GLOBAL),                     // Live Simulation Setting
    TODOROKI_SETTING(VoiceRecognitionSearchType.GLOBAL),                            // Super Todoroki Sound Setting
    SMALL_CAR_TA_SETTING(VoiceRecognitionSearchType.GLOBAL),                        // Small Car Ta Setting
    KARAOKE_SETTING(VoiceRecognitionSearchType.GLOBAL),                             // Karaoke Setting
    SETTINGS_AUDIO(VoiceRecognitionSearchType.GLOBAL),                              // Audio
    LOUDNESS_DIALOG(VoiceRecognitionSearchType.GLOBAL),                             // Loudness Dialog
    ADVANCED_AUDIO_SETTING(VoiceRecognitionSearchType.GLOBAL),                      // AdvancedAudioSetting
    CROSS_OVER_SETTINGS(VoiceRecognitionSearchType.GLOBAL),                         // CrossOverSettings
    FADER_BALANCE_SETTING(VoiceRecognitionSearchType.GLOBAL),                       // FaderBalanceSetting
    SETTINGS_INFORMATION(VoiceRecognitionSearchType.GLOBAL),                        // Information
    SETTINGS_RADIO(VoiceRecognitionSearchType.GLOBAL),                              // Radio
    SETTINGS_DAB(VoiceRecognitionSearchType.GLOBAL),                              // DAB
    SETTINGS_HD_RADIO(VoiceRecognitionSearchType.GLOBAL),                              // HD Radio
    LOCAL_DIALOG(VoiceRecognitionSearchType.GLOBAL),                                // Local Dialog
    SETTINGS_VOICE(VoiceRecognitionSearchType.GLOBAL),                              // Voice
    SETTINGS_PHONE(VoiceRecognitionSearchType.GLOBAL),                              // Phone
    INCOMING_CALL_PATTERN_SETTING(VoiceRecognitionSearchType.GLOBAL),               // Incoming Call Pattern
    INCOMING_CALL_COLOR_SETTING(VoiceRecognitionSearchType.GLOBAL),                 // Incoming Call Color
    INCOMING_MESSAGE_COLOR_SETTING(VoiceRecognitionSearchType.GLOBAL),              // Incoming Message Color
    SETTINGS_ADAS(VoiceRecognitionSearchType.GLOBAL),                               // ADAS
    CALIBRATION_SETTING(VoiceRecognitionSearchType.GLOBAL),                         // Calibration Setting
    CALIBRATION_SETTING_FITTING(VoiceRecognitionSearchType.GLOBAL),                 // Calibration Setting Fitting
    ADAS_CAMERA_SETTING(VoiceRecognitionSearchType.GLOBAL),                         // ADAS Camera Position Setting
    ADAS_WARNING_SETTING(VoiceRecognitionSearchType.GLOBAL),                        // ADAS Warning Settingg
    RE_CALIBRATION_NOTIFICATION(VoiceRecognitionSearchType.GLOBAL),                 // Re-CalibrationNotification
    ADAS_TUTORIAL(VoiceRecognitionSearchType.GLOBAL),                               // ADAS Tutorial
    ADAS_BILLING(VoiceRecognitionSearchType.GLOBAL),                                // ADAS Billing
    CAMERA_SETTING(VoiceRecognitionSearchType.GLOBAL),                              // CalibrationSetting
    FUNCTION_SETTING(VoiceRecognitionSearchType.GLOBAL),                            // FunctionSetting
    PARKING_SENSOR_SETTING(VoiceRecognitionSearchType.GLOBAL),                      // Parking Sensor Setting
    ALEXA(VoiceRecognitionSearchType.GLOBAL),                                       // Alexa
    ALEXA_DISPLAY_CARD(VoiceRecognitionSearchType.GLOBAL),                          // Alexa Display Card
    ALEXA_SETTING(VoiceRecognitionSearchType.GLOBAL),                               // Alexa Setting
    ALEXA_SPLASH(VoiceRecognitionSearchType.GLOBAL),                               	// Alexa Splash
    ALEXA_EXAMPLE_USAGE(VoiceRecognitionSearchType.GLOBAL),                         // Alexa Example Usage
    DEBUG_SETTING(VoiceRecognitionSearchType.GLOBAL),                               // DebugSetting
    PAIRING_DEVICE_LIST(VoiceRecognitionSearchType.GLOBAL),							// Pairing Device List
    STATUS_DIALOG(VoiceRecognitionSearchType.GLOBAL),								// Setting Status Popup
    SELECT_DIALOG(VoiceRecognitionSearchType.GLOBAL),								// Setting Select Popup
    IMPACT_DETECTION(VoiceRecognitionSearchType.GLOBAL),                            // Impact detection Dialog
    ADAS_WARNING(VoiceRecognitionSearchType.GLOBAL),                                // Adas Warning Dialog
    READING_MESSAGE(VoiceRecognitionSearchType.GLOBAL),                             // Reading Message Dialog
    PARKING_SENSOR(VoiceRecognitionSearchType.GLOBAL),                              // Parking Sensor
    SPEECH_RECOGNIZER(VoiceRecognitionSearchType.GLOBAL),                           // Speech Recognizer
    CAR_DEVICE_ERROR(VoiceRecognitionSearchType.GLOBAL),							// Car Device Error
    SXM_SUBSCRIPTION_UPDATE(VoiceRecognitionSearchType.GLOBAL),						// Sxm Subscription Update
    USB_ERROR(VoiceRecognitionSearchType.GLOBAL),						            // Usb Error
    MAIN_STATUS_DIALOG(VoiceRecognitionSearchType.GLOBAL),							// Main Status Popup
    LIST_STATUS_DIALOG(VoiceRecognitionSearchType.GLOBAL),							// List Status Popup
	VIDEO_PLAYER(VoiceRecognitionSearchType.GLOBAL),								// Video Player
    ADAS_USAGE_CAUTION(VoiceRecognitionSearchType.GLOBAL),							// Adas Usage Caution
    ADAS_MANUAL(VoiceRecognitionSearchType.GLOBAL),									// Adas Manual
    BACKGROUND_PREVIEW(VoiceRecognitionSearchType.GLOBAL),							// Background Preview
    PROMPT_AUTHORITY_PERMISSION(VoiceRecognitionSearchType.GLOBAL),					// Prompt Authority Permission Dialog Fragment
    CUSTOM_KEY_SETTING(VoiceRecognitionSearchType.GLOBAL),                           // Custom Key Setting
    YOUTUBE_LINK_SETTING(VoiceRecognitionSearchType.GLOBAL),                        // YouTubeLink Setting
    YOUTUBE_LINK_CAUTION(VoiceRecognitionSearchType.GLOBAL),                        // YouTubeLink Caution
    YOUTUBE_LINK_WEBVIEW(VoiceRecognitionSearchType.GLOBAL),                        // YouTubeLink WebView
    YOUTUBE_LINK_CONTAINER(VoiceRecognitionSearchType.GLOBAL),                      // YouTubeLink Container
    YOUTUBE_LINK_SEARCH_ITEM(VoiceRecognitionSearchType.GLOBAL),                      // YouTubeLink Search Item Dialog
    VOICE_RECOGNIZE_TYPE_DIALOG(VoiceRecognitionSearchType.GLOBAL),                      // Voice Recognize Type Select Dialog

    ;

    private VoiceRecognitionSearchType mVoiceRecognitionSearchType;

    ScreenId(VoiceRecognitionSearchType voiceRecognitionSearchType) {
        mVoiceRecognitionSearchType = voiceRecognitionSearchType;
    }

    public VoiceRecognitionSearchType getVoiceRecognitionSearchType() {
        return mVoiceRecognitionSearchType;
    }

    public boolean isSettings() {
        switch (this) {
            case SETTINGS_ENTRANCE:
            case SETTINGS_SYSTEM:
            case SETTINGS_SYSTEM_INITIAL:
            case SETTINGS_THEME:
            case THEME_SET_SETTING:
            case ILLUMINATION_COLOR_SETTING:
            case UI_COLOR_SETTING:
            case ILLUMINATION_DIMMER_SETTING:
            case SETTINGS_NAVIGATION:
            case SETTINGS_MESSAGE:
            case SETTINGS_APP:
            case DIRECT_CALL_SETTING:
            case DIRECT_CALL_CONTACT_SETTING:
            case CAR_SAFETY_SETTINGS:
            case IMPACT_DETECTION_SETTINGS:
            case IMPACT_DETECTION_CONTACT_SETTING:
            case IMPACT_DETECTION_CONTACT_REGISTER_SETTING:
            case SETTINGS_FX:
            case EQ_SETTING:
            case EQ_QUICK_SETTING:
            case EQ_PRO_SETTING:
            case EQ_PRO_SETTING_ZOOM:
            case LIVE_SIMULATION_SETTING:
            case TODOROKI_SETTING:
            case SMALL_CAR_TA_SETTING:
            case KARAOKE_SETTING:
            case SETTINGS_AUDIO:
            case ADVANCED_AUDIO_SETTING:
            case CROSS_OVER_SETTINGS:
            case FADER_BALANCE_SETTING:
            case SETTINGS_INFORMATION:
            case SETTINGS_RADIO:
            case SETTINGS_VOICE:
            case SETTINGS_PHONE:
            case INCOMING_CALL_PATTERN_SETTING:
            case INCOMING_CALL_COLOR_SETTING:
            case INCOMING_MESSAGE_COLOR_SETTING:
            case SETTINGS_ADAS:
            case CALIBRATION_SETTING:
            case CALIBRATION_SETTING_FITTING:
            case ADAS_CAMERA_SETTING:
            case ADAS_WARNING_SETTING:
            case RE_CALIBRATION_NOTIFICATION:
            case ADAS_TUTORIAL:
            case ADAS_BILLING:
            case FUNCTION_SETTING:
            case CAMERA_SETTING:
            case PARKING_SENSOR_SETTING:
            case DEBUG_SETTING:
            case PAIRING_DEVICE_LIST:
            case GUIDANCE_VOLUME_DIALOG:
            case LOUDNESS_DIALOG:
            case LOCAL_DIALOG:
            case MENU_DISPLAY_LANGUAGE_DIALOG:
			case STATUS_DIALOG:
            case BT_DEVICE_LIST:
            case BT_DEVICE_SEARCH:
            case ALEXA_SETTING:
            case ALEXA_SPLASH:
            case ALEXA_EXAMPLE_USAGE:
            case VIDEO_PLAYER:
            case ADAS_USAGE_CAUTION:
            case ADAS_MANUAL:
            case BACKGROUND_PREVIEW:
            case SETTINGS_DAB:
            case SETTINGS_HD_RADIO:
            case YOUTUBE_LINK_SETTING:
                return true;
            default:
                return false;
        }
    }

    public boolean isLandscape() {
        switch (this) {
            case EQ_PRO_SETTING:
            case EQ_PRO_SETTING_ZOOM:
            case CALIBRATION_SETTING:
                return true;
            default:
                return false;
        }
    }

    public boolean isPortrait() {
        switch (this) {
            case UNCONNECTED_CONTAINER:
            case TIPS:
            case TIPS_WEB:
            case EASY_PAIRING:
            case PAIRING_SELECT:
            case ADAS_TUTORIAL:
            case ADAS_BILLING:
            case ALEXA_EXAMPLE_USAGE:
            case ALEXA_SPLASH:
                return true;
            default:
                return false;
        }
    }

    public boolean isLocked(){
        switch (this) {
            case CALIBRATION_SETTING_FITTING:
            case ADAS_CAMERA_SETTING:
            case ADAS_WARNING_SETTING:
            case RE_CALIBRATION_NOTIFICATION:
            case ALEXA:
            case ALEXA_DISPLAY_CARD:
                return true;
            default:
                return false;
        }
    }

    public boolean isDialog(){
        switch (this) {
            case IMPACT_DETECTION:
            case ADAS_WARNING:
            case CAUTION:
            case GUIDANCE_VOLUME_DIALOG:
            case LOCAL_DIALOG:
            case LOUDNESS_DIALOG:
            case MENU_DISPLAY_LANGUAGE_DIALOG:
            case PARKING_SENSOR:
            case READING_MESSAGE:
            case SPEECH_RECOGNIZER:
            case STATUS_DIALOG:
            case CONTACTS_CONTAINER:
            case SOURCE_SELECT_CONTAINER:
            case PLAYER_LIST_CONTAINER:
            case RADIO_LIST_CONTAINER:
            case USB_LIST:
            case SEARCH_CONTAINER:
            case CAR_DEVICE_ERROR:
            case SXM_SUBSCRIPTION_UPDATE:
            case USB_ERROR:
            case MAIN_STATUS_DIALOG:
            case SELECT_DIALOG:
            case LIST_STATUS_DIALOG:
            case BACKGROUND_PREVIEW:
            case PROMPT_AUTHORITY_PERMISSION:
            case CUSTOM_KEY_SETTING:
            case YOUTUBE_LINK_CONTAINER:
            case YOUTUBE_LINK_SEARCH_ITEM:
            case VOICE_RECOGNIZE_TYPE_DIALOG:
                return true;
            default:
                return false;
        }
    }

    public boolean isPlayer(){
        switch (this) {
            case ANDROID_MUSIC:
            case CD:
            case SIRIUS_XM:
            case USB:
            case PANDORA:
            case SOURCE_OFF:
            case TI:
            case PLAYER_CONTAINER:
            case BT_AUDIO:
            case SPOTIFY:
            case RADIO:
            case DAB:
            case HD_RADIO:
                return true;
            default:
                return false;
        }
    }

    public boolean isAdasWarnVisible(){
        switch (this) {
            case HOME:
            case ANDROID_MUSIC:
            case CD:
            case SIRIUS_XM:
            case USB:
            case PANDORA:
            case SOURCE_OFF:
            case TI:
            case PLAYER_CONTAINER:
            case BT_AUDIO:
            case SPOTIFY:
            case RADIO:
            case DAB:
            case HD_RADIO:
            case UNSUPPORTED:
                return true;
            default:
                return false;
        }
    }
}
