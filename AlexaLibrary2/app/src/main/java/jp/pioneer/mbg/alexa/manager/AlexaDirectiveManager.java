package jp.pioneer.mbg.alexa.manager;

import android.content.Context;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Settings.SettingsUpdatedItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.connection.OkHttpClientUtil;
import jp.pioneer.mbg.alexa.response.ResponseParser2;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.alexa.util.LogUtil;
import jp.pioneer.mbg.alexa.util.SettingsUpdatedUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by esft-sakamori on 2017/08/10.
 */

public class AlexaDirectiveManager {

    private static final String TAG = AlexaDirectiveManager.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * DownChannelの状態変化を通知するコールバックインスタンスのリスト
     */
    private static final ArrayList<DownChannelCallback> mDownChannelCallbackList = new ArrayList<DownChannelCallback>();

    /**
     * アプリの終了を設定する
     * @param isFinishFlag
     */
    public static void setIsFinishFlag(boolean isFinishFlag) {
        AlexaDirectiveManager.isFinishFlag = isFinishFlag;
    }

    /**
     * 終了フラグ
     */
    private static boolean isFinishFlag = false;
    /**
     * 即再接続フラグ（ディレイなし）
     */
    private static boolean isReconnectFlag = false;
    /**
     * DownChannelのCallインスタンス
     */
    private static Call mDownChannelCall = null;

    /**
     * DownChannelの状態変化を通知するコールバックリスナー
     */
    public interface DownChannelCallback {
        /**
         * DownChannel接続
         */
        public void downChannelOpened();

        /**
         * DownChannel切断
         * @param isReconnect
         * @param isFinish
         */
        public void downChannelClosed(boolean isReconnect, boolean isFinish);

        /**
         * DownChannel初期設定完了
         */
        public void completeInitializeConnection();
    }

    /**
     * コールバックリスナー登録
     * @param callback
     */
    public static void registrationDownChannelCallback(DownChannelCallback callback) {
        if (mDownChannelCallbackList.contains(callback) == false) {
            mDownChannelCallbackList.add(callback);
        }
    }

    /**
     * コールバックリスナー削除
     * @param callback
     */
    public static void unregistrationDownChannelCallback(DownChannelCallback callback) {
        if (mDownChannelCallbackList.contains(callback) == true) {
            mDownChannelCallbackList.remove(callback);
        }
    }

    /**
     * DownChannel接続開始
     * @param context
     * @param token
     */
    public synchronized static void openDownChannel(final Context context, final String token){
        final String url = AlexaManager.getDirectivesUrl(context);

        mDownChannelCall = null;

        final Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader(Constant.KEY_AUTHORIZATION, Constant.KEY_BEARER + token)
                .build();

        OkHttpClient client = OkHttpClientUtil.getAvsConnectionOkHttpClient();

        //キャンセル
        client.dispatcher().cancelAll();
        if (DBG)android.util.Log.d(TAG, "*** client.dispatcher().runningCalls() = " + client.dispatcher().runningCalls());
        if (DBG)android.util.Log.d(TAG, "*** client.dispatcher().runningCallsCount() = " + client.dispatcher().runningCallsCount());

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "DownChannel onFailure() e = " + e.toString());
                for (DownChannelCallback callback : mDownChannelCallbackList) {
                    if(callback != null){
                        callback.downChannelClosed(isReconnectFlag, isFinishFlag);
                        isReconnectFlag = false;
                        isFinishFlag = false;
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (DBG) android.util.Log.w(TAG, "DownChannel onResponse()", new Throwable());
                boolean successful = response.isSuccessful();
                for (DownChannelCallback callback : mDownChannelCallbackList) {
                    if(callback != null){
                        callback.downChannelOpened();
                    }
                }
                mDownChannelCall = call;

                // SynchronizeStateEvent
                sendSynchronizeStateEvent(context);

                if (DBG) android.util.Log.d(TAG, "Start While - DownChannel.");
                BufferedSource bufferedSource = response.body().source();
                // 切断フラグ
                boolean disconnect = false;
                // Speakディレクティブなどの待機用変数
                AlexaIfDirectiveItem tempDirectiveItem = null;
                DownChannelParserThread thread = new DownChannelParserThread(context);
                thread.setBoundary(ResponseParser2.getBoundary(response));
                thread.start();

                do {
                    if (DBG) android.util.Log.d(TAG, "DownChannel While...");
                    try {
                        boolean exhausted = bufferedSource.exhausted();
                        // すべてのバイトを読み込んだ場合にexhaustedがtrueになる -> バックチャネルが切断
                        disconnect = exhausted;
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 例外発生によりバックチャネルが切断
                        disconnect = true;
                    }
                    if (DBG) android.util.Log.d(TAG, "DownChannel, disconnect = " + disconnect);

                    if (disconnect == false) {
                        thread.mWait = true;
                        // バックチャネルが切断されていなければ読み込み
                        Buffer buffer = bufferedSource.buffer().clone();
                        int readSize = 0;
                        try {
                            // TODO:読み込んだデータがbufのサイズよりも小さい場合、readメソッドから返ってこなくなる。
                            // TODO:  -> bufferをクローンしてDownChannelと引き離して使用することで対応。
                            InputStream responseStream = buffer.inputStream();

                            byte[] buf = new byte[1024];
                            int n = 0;
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            while ((n = responseStream.read(buf)) >= 0) {
                                baos.write(buf, 0, n);
                                if (DBG) android.util.Log.d(TAG, "DownChannel, POINT_1");
                                {
                                    // デバッグ
                                    String debugStr = new String(buf, "UTF-8");
                                    LogUtil.d(TAG, "--DownChannel, POINT_1 : debugStr = " + debugStr);
                                }
                            }
                            byte[] tempContent = baos.toByteArray();
                            readSize = tempContent.length;
                            {
                                // デバッグ
                                String debugStr = new String(tempContent, "UTF-8");
                                LogUtil.d(TAG, "--DownChannel, POINT_2 : debugStr = " + debugStr);
                            }
                            thread.writeContent(tempContent);
                            thread.mWait = false;

                            baos.close();
                        } catch (IOException ioe) {
                            if (DBG) android.util.Log.d(TAG, "DownChannel, IOException = " + ioe);
                            ioe.printStackTrace();
                        }
                        finally {

                        }
                        bufferedSource.skip(readSize);
                    }
                } while (disconnect == false);

                thread.cancel = true;

/* 問題点リストNo.3、7 対応 End */
                //closeしておく
                response.close();
                bufferedSource.close();
                if (DBG) android.util.Log.d(TAG, "*** Exit While - DownChannel.");
                for (DownChannelCallback callback : mDownChannelCallbackList) {
                    if (callback != null) {
                        callback.downChannelClosed(isReconnectFlag || successful, isFinishFlag);
                        isReconnectFlag = false;
                        isFinishFlag = false;
                    }
                }
            }
        });
    }

/* 問題点リストNo.3、7 対応 Start */
    /**
     * DownChannelで複数回に分けてディレクティブが受信される現象に対する対策スレッド.<br>
     * 受信したデータをStreamに溜め、一定時間追加が無ければParserに通す。<br>
     * <br>
     * 時間固定で処理を待つことは推奨されないが、BufferedSourceのexhausted()メソッドが、
     * 受信済みデータが無いとreturnせずにスレッドを停止させ、又、exhausted()メソッドを実行しないと
     * 次のデータを受信しないため、他に方法が思いつかず暫定的に対策を実施。
     */
    private static class DownChannelParserThread extends Thread {

        /**
         * コンテキスト
         */
        private Context mContext = null;
        /**
         * 待機フラグ <br>
         * 受信したデータをbyte配列にする前に解析処理を行わないよう処理待ちを行うフラグ
         */
        public boolean mWait = false;

        /**
         * Cidの数判定で受信データに不足が判明した場合true
         */
        private boolean mWaitCId = false;
        /**
         * 新たなデータを受信した際に解析処理を行うまでの猶予時間を保持。
         */
        private long mWaitTime = 0;

        /**
         * タイムアウト無効時の設定値
         */
        private static final long TIME_OUT_LIMIT_NONE = -1;
        /**
         * タイムアウト
         */
        private static final long TIME_OUT_ADD_TIME = 5 * 1000;
        /**
         * タイムアウトする時間（ms）
         */
        private long mTimeOutLimit = TIME_OUT_LIMIT_NONE;

        /**
         * バウンダリー
         */
        private String boundary = "";
        /**
         * 受信データのStream
         */
        private ByteArrayOutputStream content = null;

        /**
         * キャンセルフラグ
         */
        public boolean cancel = false;

        public DownChannelParserThread(Context context) {
            mContext = context;
            mWait = true;
            initContentStream();
        }

        public void initContentStream() {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            content = new ByteArrayOutputStream();
        }

        public void setBoundary(String boundary) {
            if (DBG) android.util.Log.d(TAG + "#" + getClass().getSimpleName(), "setBoundary(), boundary = " + boundary);
            this.boundary = boundary;
        }

        public void writeContent(byte[] content) {
            if (DBG) android.util.Log.d(TAG + "#" + getClass().getSimpleName(), "writeContent()");
            if (this.content != null) {
                if (DBG) android.util.Log.d(TAG + "#" + getClass().getSimpleName(), " -- writeContent. content.length = " + content.length);
                synchronized (this.content) {
                    if (mTimeOutLimit == TIME_OUT_LIMIT_NONE) {
                        this.mTimeOutLimit = System.currentTimeMillis() + TIME_OUT_ADD_TIME;
                    }
                    this.content.write(content, 0, content.length);
//                    this.mWaitTime = 500;
                    boolean isBoundaryLast = false;
                    {
                        byte[] bytes = this.content.toByteArray();
                        String str = null;
                        try {
                            str = new String(bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if (DBG) android.util.Log.d(TAG + "#" + getClass().getSimpleName(), "writeContent() str = " + str);
                        if (!TextUtils.isEmpty(str) && this.boundary != null) {
                            // 最後の文字がバウンダリーか？
//                            isBoundaryLast = str.endsWith(this.boundary);
                            isBoundaryLast = str.endsWith(this.boundary) || str.endsWith(this.boundary + "\r\n");   // 改行コード含む
                        }
                    }
                    if (isBoundaryLast) {
                        boolean success = false;
                        try {
                            success = ResponseParser2.checkContentIdCount(this.content.toByteArray(), boundary);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (success) {
                            mWaitCId = false;
                            this.mWaitTime = 0;
                        }
                        else {
                            mWaitCId = true;
                            this.mWaitTime = 500;
                        }
                    }
                    else {
                        mWaitCId = true;
                        this.mWaitTime = 500;
                    }
                }
            }
        }

        @Override
        public void run() {

            while (true) {
                if (cancel) {
                    break;
                }
                if (mWaitTime > 0) {
                    long tempTime = mWaitTime;
                    mWaitTime = 0;
                    try {
                        Thread.sleep(tempTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                boolean isTimeOut = false;
                if (mTimeOutLimit - System.currentTimeMillis() > 0) {
                    // タイムアウト時間未経過
                    isTimeOut = false;
                }
                else {
                    // タイムアウト時間経過
                    isTimeOut = true;
                }
                if (!mWait && !mWaitCId || isTimeOut) {
                    ArrayList<AlexaIfDirectiveItem> items = null;
                    synchronized (this.content) {
                        if (content != null && content.size() > 0) {
                            if (DBG) {
                                byte[] bytes = content.toByteArray();
                                android.util.Log.d(TAG + "#" + getClass().getSimpleName(), " -- Parse content. bytes.length = " + bytes.length);
                                String data = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                                android.util.Log.d(TAG + "#" + getClass().getSimpleName(), "data = " + data);
                            }
                            try {
                                items = ResponseParser2.parseDataWithDownChannel(content.toByteArray(), mContext, boundary);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mTimeOutLimit = TIME_OUT_LIMIT_NONE;
                            content.reset();
                        }
                    }
                    if (items != null && items.size() > 0) {
                        if (DBG) android.util.Log.d(TAG + "#" + getClass().getSimpleName(), " -- post items.");
                        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                        queueManager.post(items);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                content.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            content = null;
        }
    }
/* 問題点リストNo.3、7 対応 End */

    private synchronized  static void  sendCapabilitiesAPI(Context context){
        AlexaEventManager.sendCapabilities(context,TokenManager.getToken(), new AlexaEventManager.AlexaCallback() {

            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "CapabilitiesAPI onExecute()" + call);
                android.util.Log.d(TAG, "*** CapabilitiesAPI onExecute()" + call);
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "CapabilitiesAPI onResponse()" + httpCode);
                android.util.Log.d(TAG, "*** CapabilitiesAPI onResponse()" + httpCode);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.d(TAG, "CapabilitiesAPI onFailure()" + e);
                android.util.Log.d(TAG, "*** CapabilitiesAPI onFailure()" + e);
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "*** CapabilitiesAPI onParsedResponse(), itemList = " + itemList);
            }
        });
    }

    private  synchronized static void sendSupportedCountries(final Context context){
        AlexaEventManager.sendSupportedCountries(context, new AlexaEventManager.AlexaCallback() {
            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "SupportedCountries onExecute()");
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "SupportedCountries onResponse()");
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.d(TAG, "SupportedCountries onFailure()");
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "SupportedCountries onParsedResponse(), itemList = " + itemList);
            }
        });
    }

    /**
     * SynchronizeStateイベント送信
     * @param context
     */
    private synchronized static void sendSynchronizeStateEvent(final Context context) {
        AlexaEventManager.sendSynchronizeStateEvent(TokenManager.getToken(), context, new AlexaEventManager.AlexaCallback() {

            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "SynchronizeStateEvent onExecute()");
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "SynchronizeStateEvent onResponse()");

                if (200 <= httpCode && httpCode < 300) {
                    // 成功
                    if (DBG) android.util.Log.d(TAG, " - SynchronizeStateEvent onResponse(), Success");
                    // SettingsUpdatedイベント送信
                    sendSettingsUpdated(context);
                    sendCapabilitiesAPI(context);
                }
                else {
                    // 失敗
                    if (DBG) android.util.Log.w(TAG, " - SynchronizeStateEvent onResponse(), Error");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "SynchronizeStateEvent onFailure(), e = " + e);
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "SynchronizeStateEvent onParsedResponse(), itemList = " + itemList);
            }
        });
    }

    /**
     * SettingsUpdatedイベント送信
     * @param context
     */
    public synchronized static void sendSettingsUpdated(final Context context) {
        ArrayList<AlexaIfEventItem.Setting> settings = new ArrayList<AlexaIfEventItem.Setting>();
        {
            String locale = SettingsUpdatedUtil.getLocaleSetting(context);
            AlexaIfEventItem.Setting setting = new AlexaIfEventItem.Setting("locale", locale);
            settings.add(setting);
        }
        SettingsUpdatedItem settingsUpdatedItem = new SettingsUpdatedItem(settings);
        AlexaEventManager.sendEvent(TokenManager.getToken(), settingsUpdatedItem, context, new AlexaEventManager.AlexaCallback() {
            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "SettingsUpdated onExecute()");
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "SettingsUpdated onResponse()");
                if (200 <= httpCode && httpCode < 300) {
                    if (DBG) android.util.Log.d(TAG, " - SettingsUpdated onResponse(), Success ");
                }
                else {
                    // 失敗
                    if (DBG) android.util.Log.w(TAG, " - SettingsUpdated onResponse(), Error ");
                }


                /*
                * onFailure時にもisAlexaConnectionをTrueにしてしまうことにより、
                * Alexa開始直後にRecgnizeを開始させると、Idle状態のまま録音される。
                * 現状onFailureでisAlexaConnectionをTrueにしないことで問題は発生しない
                * */
                AmazonAlexaManager.getInstance().mIsAlexaConnection = true;
                for (DownChannelCallback callback : mDownChannelCallbackList) {
                    if (callback != null) {
                        callback.completeInitializeConnection();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "SettingsUpdated onFailure(), e = " + e);
                for (DownChannelCallback callback : mDownChannelCallbackList) {
                    if (callback != null) {
                        //     callback.completeInitializeConnection();
                    }
                }
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "SettingsUpdated onParsedResponse(), itemList = " + itemList);
            }
        });
    }

    /**
     * DownChannelの切断
     * @param isReconnect
     * @param isFinish
     * @return
     */
    public static boolean cancelDownChannel(boolean isReconnect, boolean isFinish) {
        if (DBG) android.util.Log.d(TAG, "cancelDownChannel()");
        boolean result = false;
        isReconnectFlag = isReconnect;
        isFinishFlag = isFinish;
        if (mDownChannelCall != null) {
            mDownChannelCall.cancel();
            result = true;
        }
        return result;
    }
}
