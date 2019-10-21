package jp.pioneer.mbg.alexa.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.common.Initiator;
import jp.pioneer.mbg.alexa.AlexaInterface.event.SpeechRecognizer.RecognizeItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.System.SynchronizeStateItem;
import jp.pioneer.mbg.alexa.connection.OkHttpClientUtil;
import jp.pioneer.mbg.alexa.response.ResponseParser2;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.android.vozsis.R;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by esft-sakamori on 2017/08/10.
 */

public class AlexaEventManager {
    //private final static Logger loggerAVS = TagManager.getInstance().getLogger(TAGS.AVS_SendEvent);
    private final static String TAG = AlexaEventManager.class.getSimpleName();
    private static final boolean DBG = true;

    public interface AlexaCallback {
        public void onExecute(Call call);
        public void onResponse(Call call, int httpCode);
        public void onFailure(Call call, IOException e);

        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList);
    }

    /**
     * SynchronizeStateEvent送信メソッド.
     * @param accessToken
     * @param context
     */
    public synchronized static void sendSynchronizeStateEvent(final String accessToken, Context context, final AlexaCallback callback){
        final MediaType JSON = MediaType.parse(Constant.MEDIA_TYPE_JSON);

        final String url = AlexaManager.getEventsUrl(context);
        JSONObject jsonObject = new JSONObject();
        SynchronizeStateItem synchronizeStateItem = new SynchronizeStateItem();
        String stringJson = null;
        try {
            JSONObject eventJson = synchronizeStateItem.toJsonObject();
            JSONArray contextJson = AlexaManager.createContext();

            jsonObject.put(Constant.JSON_PARAM_CONTEXT, contextJson);
            jsonObject.put(Constant.JSON_PARAM_EVENT, eventJson);

            stringJson = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (stringJson == null) {
            stringJson = "";
        }

        if (DBG) android.util.Log.d(TAG, " - sendSynchronizeStateEvent() : event = " + stringJson);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // multipartでmetadata部分にJsonを詰める.
                .addFormDataPart(Constant.KEY_MATA_DATA, Constant.KEY_MATA_DATA,
                        RequestBody.create(JSON, stringJson))
                .build();
        Request request = new Request.Builder()
                .addHeader(Constant.KEY_AUTHORIZATION, Constant.KEY_BEARER + accessToken)
                .url(url)
                .post(requestBody)
                .build();

//        AlexaEventManager.sendEvent(request, callback);
        AlexaEventManager.sendEvent(context, request, callback);
    }

    /**
     * SupportedCountries API送信メソッド.
     * @param context
     */
    public synchronized static void sendSupportedCountries(Context context, final AlexaCallback callback){

        final String supportedCountriesUrl = AlexaManager.getSupportedCountriesUrl();
        Request request = new Request.Builder()
                .url(supportedCountriesUrl)
                .get()
                .build();
        AlexaEventManager.sendEvent(context, request, callback);
    }


    /**
     * Capabilities API送信メソッド.
     * @param context
     */
    public static void sendCapabilities(Context context, String accessToken, final AlexaCallback callback){
        //CapabilitiesAPIのJSONを作成
        JSONObject object = new JSONObject();

        try {
            //alexa_api_versionrから取得できるようにしたほうがきち
            String apiVer = context.getString(R.string.alexa_api_version);
            apiVer = apiVer.replaceAll("[^0-9]", "");

            object.put("envelopeVersion",apiVer);
            object.put("capabilities",CapabiritiesItem.capabilitiesApiItem());
            Log.d(TAG, "*** capabilities JSON = " + object.toString() );
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "*** capabilities JSON error = " +e );
        }

        String path = AlexaManager.getCapabilitiesApiUri();
        Uri uri = Uri.parse(path);


        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, object.toString());
        final Request request = new Request.Builder()
                //ここの二つ削ったらなんかうまくいったっす
//                .header(Constant.CONTENT_TYPE, "application/json")
                //.addHeader(Constant.CONTENT_LENGTH, String.valueOf(object))
                .addHeader(Constant.KEY_AUTHORIZATION,  Constant.KEY_BEARER + accessToken)
                .url(path)
                .put(body)
                .build();
        AlexaEventManager.sendEvent(context, request, callback);
    }

    /**
     * Tap-to-talk用音声認識メソッド
     * @param accessToken
     * @param dataPartBody
     * @param context
     * @param callBack
     * @return dialogRequestId
     */
    public synchronized static String sendAudioEvent(final String accessToken, RequestBody dataPartBody,
                                                   Context context, AlexaCallback callBack, Initiator initiator){

        final String url = AlexaManager.getEventsUrl(context);
        final MediaType MEDIA_TYPE_OCTET = MediaType.parse(Constant.MEDIA_TYPE_OCTET);
        final MediaType MEDIA_TYPE = MediaType.parse(Constant.MEDIA_TYPE_JSON);

        JSONObject jsonObject = new JSONObject();
        RecognizeItem recognizeItem = new RecognizeItem(initiator);
        String stringJson = null;
        try {
            JSONObject event = recognizeItem.toJsonObject();
            JSONArray contextJson = AlexaManager.createContext(true);

            jsonObject.put(Constant.JSON_PARAM_CONTEXT, contextJson);
            jsonObject.put(Constant.JSON_PARAM_EVENT, event);

            stringJson = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (stringJson == null) {
            stringJson = "";
        }

        if (DBG) android.util.Log.d(TAG, " - sendAudioEvent() : event = " + stringJson);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // multipartでmetadata部分にJsonを詰める.
                .addFormDataPart(Constant.KEY_MATA_DATA, Constant.KEY_MATA_DATA,
                        RequestBody.create(MEDIA_TYPE, stringJson))
                // multipartでaudio部分に録音音声のバイト配列を詰める.
                .addFormDataPart(Constant.KEY_AUDIO, "speech.wav", dataPartBody)
                .build();

        Request request = new Request.Builder()
                .addHeader(Constant.KEY_AUTHORIZATION, Constant.KEY_BEARER + accessToken)
                .url(url)
                .post(requestBody)
                .build();
//        AlexaEventManager.sendEvent(request, callBack);
        AlexaEventManager.sendEvent(context, request, true, callBack);

        // UserInactivityReportのタイマーをリセット
        AlexaUserInactivityReportManager userInactivityReportManager = AlexaUserInactivityReportManager.getInstance();
        userInactivityReportManager.resetTimer();

        return recognizeItem.getDialogRequestId();
    }

    /**
     * 汎用的なイベント送信メソッド
     * @param accessToken
     * @param item
     * @param context
     * @param callback
     */
    public synchronized static void sendEvent(String accessToken, AlexaIfEventItem item, Context context, AlexaCallback callback) {
        AlexaEventManager.sendEvent(accessToken, item, context, false, callback);
    }

    /**
     * 汎用的なイベント送信メソッド（コンテキスト有無の設定可能）
     * @param accessToken
     * @param item
     * @param context
     * @param hasContext
     * @param callback
     */
    public synchronized static void sendEvent(String accessToken, AlexaIfEventItem item, Context context, boolean hasContext, AlexaCallback callback) {
        JSONObject jsonObject = new JSONObject();
        String request = null;
        try {
            JSONObject eventJson = item.toJsonObject();
            if (hasContext) {
                JSONArray contextJson = AlexaManager.createContext();
                jsonObject.put(Constant.JSON_PARAM_CONTEXT, contextJson);
            }
            jsonObject.put(Constant.JSON_PARAM_EVENT, eventJson);

            request = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (request == null) {
            request = "";
        }
        if (DBG) android.util.Log.d(TAG, " - sendEvent() : event = " + request);

        AlexaEventManager.sendTextEvent(accessToken, request, context, callback);

        boolean isResetTimer = false;
        if (item instanceof jp.pioneer.mbg.alexa.AlexaInterface.event.AlexaIfPlaybackControllerItem) {
            isResetTimer = true;
        }
        else if (item instanceof RecognizeItem) {
            // RecognizeItemはsendAudioEvent()で実行されるが、一応。
            isResetTimer = true;
        }
        if (isResetTimer) {
            // UserInactivityReportのタイマーをリセット
            AlexaUserInactivityReportManager userInactivityReportManager = AlexaUserInactivityReportManager.getInstance();
            userInactivityReportManager.resetTimer();
        }
    }

    public synchronized static void sendTextEvent(final String accessToken, String event, Context context, AlexaCallback callBack){
        //loggerAVS.json(event);

        if (DBG) android.util.Log.d("TEST_20190606", " - sendTextEvent() : event = " + event);

        final String url = AlexaManager.getEventsUrl(context);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), event));

        Request request = new Request.Builder()
                .addHeader(Constant.KEY_AUTHORIZATION, Constant.KEY_BEARER + accessToken)
                .url(url)
                .post(bodyBuilder.build())
                .build();
//        AlexaEventManager.sendEvent(request, callBack);
        AlexaEventManager.sendEvent(context, request, callBack);
    }

    // TODO:ピングを送信
    public synchronized static void sendPing(String accessToken, Context context, AlexaCallback callback) {

        if (DBG) android.util.Log.d(TAG, " - sendPing()");

        final String url = AlexaManager.getPingUrl(context);
        Request request = new Request.Builder()
                .addHeader(Constant.KEY_AUTHORIZATION, Constant.KEY_BEARER + accessToken)
                .url(url)
                .build();
//        AlexaEventManager.sendEvent(request, callback);
        AlexaEventManager.sendEvent(context, request, callback);

    }

    /**
     * Eventの通知。
     * @param request
     * @param callback
     */
//    private synchronized static void sendEvent(final Request request, final AlexaCallback callback){
    private synchronized static void sendEvent(final Context context, final Request request, final AlexaCallback callback) {
        sendEvent(context, request, false, callback);
    }

    /**
     * Eventの通知。
     * @param context
     * @param request
     * @param isRecognize
     * @param callback
     */
    private synchronized static void sendEvent(final Context context, final Request request, final boolean isRecognize, final AlexaCallback callback){
        Log.d(TAG, "AlexaEventManager.sendEvent() : " + request.toString());

        OkHttpClient client = OkHttpClientUtil.getAvsConnectionOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            private final String TAG = okhttp3.Callback.class.getSimpleName();
            @Override
            public void onFailure(Call call, IOException e) {
                // 失敗
                if (DBG) android.util.Log.w(TAG, "onFailure : AlexaEventManager.sendEvent()");
                if (callback != null) {
                    callback.onFailure(call, e);
                }
                else {
                    Request request = call.request();
                    HttpUrl url = null;
                    RequestBody body = null;
                    if (request != null) {
                        url = request.url();
                        body = request.body();
                    }
                    final StringBuffer buffer = new StringBuffer();
                    buffer.append("url = ").append(url);
                    buffer.append(", ");
                    buffer.append("body = ").append(body);
                    Log.w(TAG, " - onFailure(), " + buffer.toString());
                }
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 成功
                if (DBG) android.util.Log.d(TAG, "onResponse : AlexaEventManager.sendEvent()");
                if (callback != null) {
//                    callback.onResponse(call, response);
                    callback.onResponse(call, response.code());
                    ArrayList<AlexaIfDirectiveItem> itemList = ResponseParser2.parseResponse(response, context, isRecognize);

                    // 一度、発行側に見てもらう
                    callback.onParsedResponse(itemList);

                    if (itemList.size() > 0) {
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.post(itemList);
                    }
                    else if (isRecognize) {
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.onRecognizeNoResponse();
                    }

                }
                else {
                    Log.d(TAG, " - AlexaEventManager.sendEvent() onResponse : " + response.toString());
                    ArrayList<AlexaIfDirectiveItem> itemList = ResponseParser2.parseResponse(response, context, isRecognize);
                    if (itemList.size() > 0) {
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.post(itemList);
                    }
                    else if (isRecognize) {
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.onRecognizeNoResponse();
                    }

                }
                response.close();
                call.cancel();
            }
        });

        if (callback != null) {
            callback.onExecute(call);
        }

    }

}
