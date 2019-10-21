package jp.pioneer.mbg.alexa.manager;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.exoplayer2.audio.WLAudioStreamEx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import jp.pioneer.mbg.alexa.AlexaInterface.directive.Notifications.SetIndicatorItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.player.IAlexaPlayer;
import jp.pioneer.mbg.alexa.player.WLAlexaPlayer;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.android.vozsis.R;

/**
 * Created by esft-komiya on 2016/10/17.
 * Alexaからのレスポンスやリクエスト送信時に活用する処理をまとめたManagerクラス.
 */
public class AlexaManager {
    private static final String TAG = AlexaManager.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * エンドポイント
     */
    private static String urlEndpoint;

    /**
     * Eventで送信するコンテキストを生成する
     * @return
     */
    public static JSONArray createContext() throws JSONException {
        return createContext(false);
    }
    public static JSONArray createContext(boolean hasGeolocationState) throws JSONException {

        JSONArray contextArray = new JSONArray();
        //
        if (hasGeolocationState) {
            GeolocationManager geolocationManager = GeolocationManager.getInstance();
            if (geolocationManager != null) {
                Location location = geolocationManager.getLocation();
                if (location != null) {
                    JSONObject itemObject = new JSONObject();
                    // header
                    {
                        JSONObject headerObject = new JSONObject();
                        headerObject.put("namespace", "Geolocation");
                        headerObject.put("name", "GeolocationState");
                        itemObject.put("header", headerObject);
                    }
                    // payload
                    {
                        JSONObject payloadObject = new JSONObject();
                        {
                            // 時間()
                            //String timeStamp = DateFormat.format("yyyy-MM-ddTkk:mm:ssZ", Calendar.getInstance()).toString();
                            //Revision: 2451
                            //[18-278-1-00101]「99Dream_Android」端末をアラビア語を設定される場合、Alexa画面を開いてから、何も発話しないまま、「Listening」＆「Thinking」のタイムアウト時間は日本語や英語より短すぎる。 
                            //端末の言語設定に関わらずロケールを英語で表現
                            //Revision: 2820
                            //GeolocationAPIのtimestampをUTC形式に修正。
/*                            Date now = new Date();
                            // ISO 8601の拡張形式(UTC)
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'", Locale.ENGLISH);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String timeStamp = sdf.format(now);
                            payloadObject.put("timestamp", timeStamp);*/
                            // UTCだと何故か位置情報が取得できないと言われるため、端末時刻に戻す
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'", Locale.ENGLISH);
                            String timeStamp = sdf.format(Calendar.getInstance().getTime());
                            payloadObject.put("timestamp", timeStamp);
                        }
                        {
                            // 緯度経度
                            JSONObject coordinateObject = new JSONObject();
                            coordinateObject.put("latitudeInDegrees", location.getLatitude());
                            coordinateObject.put("longitudeInDegrees", location.getLongitude());
                            coordinateObject.put("accuracyInMeters", location.getAccuracy());
                            payloadObject.put("coordinate", coordinateObject);
                        }
                        {
                            if (false && location.hasAltitude() && Build.VERSION.SDK_INT > 26) {
                                // 標高(m)
                                JSONObject altitudeObject = new JSONObject();
                                altitudeObject.put("altitudeInMeters", location.getAltitude());
                                altitudeObject.put("accuracyInMeters", location.getVerticalAccuracyMeters());
                                payloadObject.put("altitude", altitudeObject);
                            }
                        }
                        {
                            if (false) {
                                // 方向(北=0度)
                                JSONObject headingObject = new JSONObject();
                                headingObject.put("directionInDegrees", 0);
                                payloadObject.put("heading", headingObject);
                            }
                        }
                        {
                            if (false && location.hasSpeed() && Build.VERSION.SDK_INT > 26) {
                                // 速度(m/s)
                                JSONObject speedObject = new JSONObject();
                                speedObject.put("speedInMetersPerSecond", location.getSpeedAccuracyMetersPerSecond());
                                payloadObject.put("speed", speedObject);
                            }
                        }
                        itemObject.put("payload", payloadObject);
                    }
                    contextArray.put(itemObject);
                }
            }
        }
        // AudioPlayer#PlaybackState
        {
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            if (manager.getAlexaPlayer() != null) {
                JSONObject itemObject = new JSONObject();
                // header
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "AudioPlayer");
                    headerObject.put("name", "PlaybackState");
                    itemObject.put("header", headerObject);
                }
                // payload
                {
                    String token = manager.getPreviousToken();

                    IAlexaPlayer player = manager.getAlexaPlayer();

                    int offsetInMilliseconds = player.getCurrentPosition();
                    String playerActivity = "IDLE";// IDLE, PLAYING, STOPPED, PAUSED, BUFFER_UNDERRUN, and FINISHED.
                    if (TextUtils.isEmpty(token) == false) {
                        if(player instanceof WLAlexaPlayer) {
                            switch (player.getWLPlaybackState()) {
                                case WLAudioStreamEx.WL_PLAYSTATE_PLAYING:
                                    playerActivity = "PLAYING";
                                    break;
                                case WLAudioStreamEx.WL_PLAYSTATE_PAUSED:
                                    playerActivity = "PAUSED";
                                    break;
                                case WLAudioStreamEx.WL_PLAYSTATE_STOPPED:
                                    playerActivity = "FINISHED";
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            switch (player.getPlaybackState()) {
                                case PLAYING: {
                                    playerActivity = "PLAYING";
                                    break;
                                }
                                case PAUSE: {
                                    // 音楽再生時にAlexaボタンを押したときはPLAYINGとしてContextを送信
                                    if(AlexaQueueManager.getInstance().isActiveContentChannel()
                                            && AlexaQueueManager.getInstance().isRec()){
                                        playerActivity = "PLAYING";
                                    } else {
                                        playerActivity = "PAUSED";
                                    }
                                    break;
                                }
                                case STOP: {
                                    playerActivity = "STOPPED";
                                    break;
                                }
                                case PREPARE: {
                                    playerActivity = "BUFFER_UNDERRUN";
                                    break;
                                }
                                case FINISHED: {
                                    playerActivity = "FINISHED";
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                    } else {
                        // tokenが無い -> 再生中の楽曲が無い
                        playerActivity = "IDLE";
                    }

                    if (token == null) {
                        token = "";
                    }
                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("token", token);
                    payloadObject.put("offsetInMilliseconds", offsetInMilliseconds);
                    payloadObject.put("playerActivity", playerActivity);
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
            else {
                JSONObject itemObject = new JSONObject();
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "AudioPlayer");
                    headerObject.put("name", "PlaybackState");
                    itemObject.put("header", headerObject);
                }
                {
                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("token", "");
                    payloadObject.put("offsetInMilliseconds", 0);
                    payloadObject.put("playerActivity", "IDLE");
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
        }
        // SpeechRecognizer#RecognizerState
        {
            JSONObject itemObject = new JSONObject();
            // header
            {
                JSONObject headerObject = new JSONObject();
                headerObject.put("namespace", "SpeechRecognizer");
                headerObject.put("name", "RecognizerState");
                itemObject.put("header", headerObject);
            }
            // payload
            {
                JSONObject payloadObject = new JSONObject();
                payloadObject.put("wakeword", "ALEXA");
                itemObject.put("payload", payloadObject);
            }
            // TODO:Tap-to-talk方式なので設定しない
        }
        // Notifications#IndicatorState
        {
            JSONObject itemObject = new JSONObject();
            // header
            {
                JSONObject headerObject = new JSONObject();
                headerObject.put("namespace", "Notifications");
                headerObject.put("name", "IndicatorState");
                itemObject.put("header", headerObject);
            }
            // payload
            AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
            SetIndicatorItem item = notificationManager.getLastIndicatorItem();
            JSONObject payloadObject = new JSONObject();
            if (item != null) {
                payloadObject.put("isEnabled", true);
                payloadObject.put("isVisualIndicatorPersisted", Boolean.TRUE.equals(item.persistVisualIndicator));
            } else {
                payloadObject.put("isEnabled", false);
                payloadObject.put("isVisualIndicatorPersisted", false);
            }
            itemObject.put("payload", payloadObject);

            contextArray.put(itemObject);
        }
        /*// Alerts#AlertsState
        {
            JSONObject itemObject = new JSONObject();
            // header
            {
                JSONObject headerObject = new JSONObject();
                headerObject.put("namespace", "Alerts");
                headerObject.put("name", "AlertsState");
                itemObject.put("header", headerObject);
            }
            // payload
            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();

            List<SetAlertItem> alertList = alertManager.getAlertList();
            List<SetAlertItem> activeAlertList = alertManager.makeContextActiveAlertList();

            JSONObject payloadObject = new JSONObject();
            JSONArray allAlerts = new JSONArray();
            for (SetAlertItem alertItem : alertList) {
                JSONObject alert = new JSONObject();
                alert.put("token", alertItem.token);
                alert.put("type", alertItem.type);
                alert.put("scheduledTime", alertItem.scheduledTime);
                allAlerts.put(alert);
            }
            payloadObject.put("allAlerts", allAlerts);
            JSONArray activeAlerts = new JSONArray();
            for (SetAlertItem alertItem : activeAlertList) {
                JSONObject alert = new JSONObject();
                alert.put("token", alertItem.token);
                alert.put("type", alertItem.type);
                alert.put("scheduledTime", alertItem.scheduledTime);
                activeAlerts.put(alert);
            }
            payloadObject.put("activeAlerts", activeAlerts);
            itemObject.put("payload", payloadObject);

            contextArray.put(itemObject);
        }*/
        /*// Speaker#VolumeState
        {
            AlexaAudioManager manager = AlexaAudioManager.getInstance();
            if (manager.getAlexaPlayer() != null) {
                JSONObject itemObject = new JSONObject();
                // header
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "Speaker");
                    headerObject.put("name", "VolumeState");
                    itemObject.put("header", headerObject);
                }
                // payload
                {
                    IAlexaPlayer player = manager.getAlexaPlayer();
                    float volume = player.getVolume();
                    boolean mute = player.isMute();

                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("volume", (int) volume);
                    payloadObject.put("muted", mute);
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
            else {
                JSONObject itemObject = new JSONObject();
                // header
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "Speaker");
                    headerObject.put("name", "VolumeState");
                    itemObject.put("header", headerObject);
                }
                // payload
                {
                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("volume", 100);
                    payloadObject.put("muted", false);
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
        }*/
        // SpeechSynthesizer#SpeechState
        {
            AlexaSpeakManager manager = AlexaSpeakManager.getInstance();
            if (manager.getAlexaPlayer() != null) {
                JSONObject itemObject = new JSONObject();
                // header
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "SpeechSynthesizer");
                    headerObject.put("name", "SpeechState");
                    itemObject.put("header", headerObject);
                }
                // payload
                {
                    SpeakItem speakItem = manager.getLastSpeckItem();
                    String token = null;
                    if (speakItem != null) {
                        token = speakItem.token;
                    }
                    IAlexaPlayer player = manager.getAlexaPlayer();
                    int offsetInMilliseconds = player.getCurrentPosition();
                    String playerActivity = "FINISHED"; // PLAYING or FINISHED
                    if (TextUtils.isEmpty(token) == false) {
                        if (player.isPlaying()) {
                            playerActivity = "PLAYING";
                        }
                        else {
                            playerActivity = "FINISHED";
                        }
                    }
                    else {
                        // tokenが無い -> 再生中の音声が無い
                        playerActivity = "FINISHED";
                    }
                    if (token == null) {
                        token = "";
                    }
                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("token", token);
                    payloadObject.put("offsetInMilliseconds", offsetInMilliseconds);
                    payloadObject.put("playerActivity", playerActivity);
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
            else {
                JSONObject itemObject = new JSONObject();
                // header
                {
                    JSONObject headerObject = new JSONObject();
                    headerObject.put("namespace", "SpeechSynthesizer");
                    headerObject.put("name", "SpeechState");
                    itemObject.put("header", headerObject);
                }
                // payload
                {
                    JSONObject payloadObject = new JSONObject();
                    payloadObject.put("token", "");
                    payloadObject.put("offsetInMilliseconds", 0);
                    payloadObject.put("playerActivity", "FINISHED");
                    itemObject.put("payload", payloadObject);
                }
                contextArray.put(itemObject);
            }
        }

        return contextArray;
    }

    /**
     * エンドポイントの取得
     * @param context
     * @return
     */
    public static String getUrlEndpoint(Context context){
        if(urlEndpoint==null) {
            urlEndpoint = context.getString(R.string.alexa_api);
        }
        return urlEndpoint;
    }

    /**
     * エンドポイントの設定
     * @param url
     */
    public  static void setUrlEndpoint(String url){
        urlEndpoint = url;
    }

    /**
     * DownChannelのURL
     * @return
     */
    public static String getDirectivesUrl(Context context){
        //Context context = MyApplication.getInstance();
        if(context == null){
            return null;
        }        return new StringBuilder()
                .append(getUrlEndpoint(context))
                .append("/")
                .append(context.getString(R.string.alexa_api_version))
                .append("/")
                .append(Constant.DIRECTIVES)
                .toString();
    }

    /**
     * イベント送信のURL
     * @return
     */
    public static String getEventsUrl(Context context){
       // Context context = MyApplication.getInstance();
        if(context == null){
            return null;
        }        return new StringBuilder()
                .append(getUrlEndpoint(context))
                .append("/")
                .append(context.getString(R.string.alexa_api_version))
                .append("/")
                .append(Constant.EVENTS)
                .toString();
    }

    /**
     * PingのURL.
     * @return
     */
    public static String getPingUrl(Context context){
        //Context context = MyApplication.getInstance();
        if(context == null){
            return null;
        }        return new StringBuilder()
                .append(getUrlEndpoint(context))
                .append("/")
                .append(Constant.PING)
                .toString();
    }
    /**
     * SupportedCountriesのURL.
     * @return
     */

    public static  String getSupportedCountriesUrl(){
        return new StringBuilder()
                .append("https://api.amazonalexa.com")
                .append("/")
                .append("v1")
                .append("/")
                .append("avs")
                .append("/")
                .append("supportedCountries")
                .toString();
    }

    public static  String getCapabilitiesApiUri(){
        return new  StringBuilder()
                .append("https://api.amazonalexa.com")
                .append("/")
                .append("v1")
                .append("/")
                .append("devices")
                .append("/")
                .append("@self")
                .append("/")
                .append("capabilities")
                .toString();
    }
}
