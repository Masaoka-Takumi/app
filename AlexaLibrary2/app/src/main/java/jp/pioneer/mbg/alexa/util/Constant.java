package jp.pioneer.mbg.alexa.util;

/**
 * Created by esft-komiya on 2016/09/14.
 */
public class Constant {

    public static final String PRODUCT_ID = "gateramadin_amazon_alexa_sample";
    public static final String[] APP_SCOPES = {"alexa:all"};

    public static final String KEY_GRANT_TYPE = "grant_type";
    public static final String KEY_CODE = "code";
    public static final String KEY_REDIRECT_URI = "redirect_uri";
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CODE_VERIFIER = "code_verifier";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_TOKEN_TYPE = "token_type";
    public static final String KEY_EXPIRES_IN = "expires_in";
    public static final String KEY_AUTHORIZATION = "authorization";
    public static final String KEY_BEARER = "Bearer ";
    public static final String KEY_MATA_DATA = "metadata";
    public static final String KEY_AUDIO = "audio";

    public static final String CONTENT_TYPE = "Content-Type:";
    public static final String CONTENT_LENGTH = "Content-Length:";
    public static final String MEDIA_TYPE_OCTET = "application/octet-stream";
    public static final String MEDIA_TYPE_JSON = "application/json; charset=UTF-8";

    public static final String DIRECTIVES = "directives";
    public static final String EVENTS = "events";
    public static final String PING = "ping";

    public static final String INTERFACE_SPEECH_RECOGNIZER = "SpeechRecognizer";
    public static final String INTERFACE_SPEECH_SYNTHESIZER = "SpeechSynthesizer";
    public static final String INTERFACE_SYSTEM = "System";
    public static final String INTERFACE_ALERTS = "Alerts";
    public static final String INTERFACE_AUDIO_PLAYER = "AudioPlayer";
    public static final String INTERFACE_PLAYBACK_CONTROLLER = "PlaybackController";
    public static final String INTERFACE_SPEAKER = "Speaker";
    public static final String INTERFACE_SETTINGS = "Settings";
    public static final String INTERFACE_NOTIFICATIONS = "Notifications";
    public static final String INTERFACE_TEMPLATE_RUNTIME = "TemplateRuntime";
    public static final String INTERFACE_NAVIGATION = "Navigation";

    public static final String JSON_PARAM_CONTEXT = "context";
    public static final String JSON_PARAM_EVENT = "event";
    public static final String JSON_PARAM_DIRECTIVE = "directive";

    public static final String JSON_EVENT_HEADER = "header";
    public static final String JSON_EVENT_PAYLOAD = "payload";

    public static final String JSON_HEADER_NAME_SPACE = "namespace";
    public static final String JSON_HEADER_NAME = "name";
    public static final String JSON_HEADER_MESSAGE_ID = "messageId";
    public static final String JSON_HEADER_DIALOG_ID = "dialogRequestId";

    public static final String JSON_PAYLOAD_PROFILE = "profile";
    public static final String JSON_PAYLOAD_FORMAT = "format";
    public static final String JSON_PAYLOAD_TOKEN = "token";
    public static final String JSON_PAYLOAD_OFFSET = "offsetInMilliseconds";
    public static final String JSON_PAYLOAD_AUDIO_ITEM = "audioItem";
    public static final String JSON_PAYLOAD_STREAM = "stream";
    public static final String JSON_PAYLOAD_PLAY_BEHAVIOR = "playBehavior";
    public static final String JSON_PAYLOAD_CLEAR_BEHAVIOR = "clearBehavior";
    public static final String JSON_PAYLOAD_CURRENT_PLAYBACK = "currentPlaybackState";
    public static final String JSON_PAYLOAD_ERROR = "error";
    public static final String JSON_PAYLOAD_PLAYER_ACTIVITY = "playerActivity";
    public static final String JSON_PAYLOAD_ERROR_TYPE = "type";
    public static final String JSON_PAYLOAD_ERROR_MESSAGE = "message";
    public static final String JSON_PAYLOAD_ALL_ALERTS = "allAlerts";
    public static final String JSON_PAYLOAD_ACTIVE_ALERTS = "activeAlerts";
    public static final String JSON_PAYLOAD_VOLUME = "volume";
    public static final String JSON_PAYLOAD_MUTED = "muted";
    public static final String JSON_PAYLOAD_MUTE = "mute";

    public static final String JSON_PAYLOAD_SETTINGS = "settings";
    public static final String JSON_PAYLOAD_SETTINGS_KEY = "key";
    public static final String JSON_PAYLOAD_SETTINGS_VALUE = "value";

    public static final String JSON_PAYLOAD_TIMEOUT_IN_MILLISECONDS = "timeoutInMilliseconds";

    public static final String JSON_EXCEPTION = "Exception";

    public static final String STATE_ALERTS = "AlertsState";
    public static final String STATE_VOLUME = "VolumeState";
    public static final String STATE_PLAYBACK = "PlaybackState";
    public static final String STATE_SPEECH = "SpeechState";

    public static final String DIRECTIVE_PLAY = "Play";
    public static final String DIRECTIVE_STOP = "Stop";
    public static final String DIRECTIVE_CLEAR_QUEUE = "ClearQueue";
    public static final String DIRECTIVE_SPEAK = "Speak";
    public static final String DIRECTIVE_PLAYBACK_STOPPED = "PlaybackStopped";
    public static final String DIRECTIVE_SET_VOLUME = "SetVolume";
    public static final String DIRECTIVE_SET_MUTE = "SetMute";
    public static final String DIRECTIVE_ADJUST_VOLUME = "AdjustVolume";
    public static final String DIRECTIVE_STOP_CAPTURE = "StopCapture";
    public static final String DIRECTIVE_EXPECT_SPEECH = "ExpectSpeech";
    public static final String DIRECTIVE_SET_ALERT = "SetAlert";
    public static final String DIRECTIVE_DELETE_ALERT = "DeleteAlert";
    public static final String DIRECTIVE_SET_INDICATOR = "SetIndicator";
    public static final String DIRECTIVE_CLEAR_INDICATOR = "ClearIndicator";
    public static final String DIRECTIVE_RESET_USER_INACTIVITY = "ResetUserInactivity";
    public static final String DIRECTIVE_SET_ENDPOINT = "SetEndpoint";
    public static final String DIRECTIVE_RENDER_TEMPLATE = "RenderTemplate";
    public static final String DIRECTIVE_RENDER_PLAYER_INFO = "RenderPlayerInfo";
    public static final String DIRECTIVE_REPORT_SOFTWARE_INFO = "ReportSoftwareInfo";
    public static final String DIRECTIVE_SET_DESTINATION = "SetDestination";


    public static final String EVENT_RECOGNIZE = "Recognize";
    public static final String EVENT_PLAYBACK_STARTED = "PlaybackStarted";
    public static final String EVENT_PLAYBACK_NEARLY_FINISHED = "PlaybackNearlyFinished";
    public static final String EVENT_PROGRESS_REPORT_DELAY_ELAPSED = "ProgressReportDelayElapsed";
    public static final String EVENT_PROGRESS_REPORT_INTERVAL_ELAPSED = "ProgressReportIntervalElapsed";
    public static final String EVENT_PLAYBACK_SHUTTER_STARTED = "PlaybackStutterStarted";
    public static final String EVENT_PLAYBACK_SHUTTER_FINISHED = "PlaybackStutterFinished";
    public static final String EVENT_PLAYBACK_FINISHED = "PlaybackFinished";
    public static final String EVENT_PLAYBACK_FAILED = "PlaybackFailed";
    public static final String EVENT_PLAYBACK_PAUSED = "PlaybackPaused";
    public static final String EVENT_PLAYBACK_RESUMED = "PlaybackResumed";
    public static final String EVENT_PLAYBACK_STOPPED = "PlaybackStopped";
    public static final String EVENT_PLAYBACK_QUEUE_CLEARED = "PlaybackQueueCleared";
    public static final String EVENT_STREAM_METADATA_EXTRACTED = "StreamMetadataExtracted";
    public static final String EVENT_SPEECH_STARTED = "SpeechStarted";
    public static final String EVENT_SPEECH_FINISHED = "SpeechFinished";
    public static final String EVENT_MUTE_CHANGED = "MuteChanged";
    public static final String EVENT_VOLUME_CHANGED = "VolumeChanged";
    public static final String EVENT_EXPECT_SPEECH_TIMEOUT = "ExpectSpeechTimedOut";
    public static final String EVENT_SET_ALERT_SUCCEEDED = "SetAlertSucceeded";
    public static final String EVENT_SET_ALERT_FAILED = "SetAlertFailed";
    public static final String EVENT_DELETE_ALERT_SUCCEEDED = "DeleteAlertSucceeded";
    public static final String EVENT_DELETE_ALERT_FAILED = "DeleteAlertFailed";
    public static final String EVENT_ALERT_STARTED = "AlertStarted";
    public static final String EVENT_ALERT_STOPPED = "AlertStopped";
    public static final String EVENT_ALERT_ENTERED_FOREGROUND = "AlertEnteredForeground";
    public static final String EVENT_ALERT_ENTERED_BACKGROUND = "AlertEnteredBackground";
    public static final String EVENT_PLAY_COMMAND_ISSUED = "PlayCommandIssued";
    public static final String EVENT_PAUSE_COMMAND_ISSUED = "PauseCommandIssued";
    public static final String EVENT_NEXT_COMMAND_ISSUED = "NextCommandIssued";
    public static final String EVENT_PREVIOUS_COMMAND_ISSUED = "PreviousCommandIssued";
    public static final String EVENT_SYNCHRONIZE_STATE = "SynchronizeState";
    public static final String EVENT_USER_INACTIVITY_REPORT = "UserInactivityReport";
    public static final String EVENT_RESET_USER_INACTIVITY = "ResetUserInactivity";
    public static final String EVENT_EXCEPTION_ENCOUNTERED = "ExceptionEncountered";
    public static final String EVENT_SETTINGS_UPDATED = "SettingsUpdated";
    public static final String EVENT_SOFTWARE_INFO = "SoftwareInfo";

    public static final String CLOSE_TALK = "CLOSE_TALK";
    public static final String AUDIO_FORMAT = "AUDIO_L16_RATE_16000_CHANNELS_1";
    public static final String PLAYER_ACTIVITY_IDLE = "IDLE";
    public static final String PLAYER_ACTIVITY_FINISHED = "FINISHED";
    public static final String PLAYER_ACTIVITY_PLAYING = "PLAYING";

    public static final String BEHAVIOR_REPLACE_ALL = "REPLACE_ALL";
    public static final String BEHAVIOR_ENQUEUE = "ENQUEUE";
    public static final String BEHAVIOR_REPLACE_ENQUEUED = "REPLACE_ENQUEUED";
    public static final String BEHAVIOR_CLEAR_ENQUEUED = "CLEAR_ENQUEUED";
    public static final String BEHAVIOR_CLEAR_ALL = "CLEAR_ALL";
}
