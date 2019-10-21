package jp.pioneer.mbg.alexa.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.System.UserInactivityReportItem;
import jp.pioneer.mbg.alexa.response.ResponseParser2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

/**
 * UserInactivityReportイベント送信処理用マネージャ
 */
public class AlexaUserInactivityReportManager {

    private static final String TAG = AlexaUserInactivityReportManager.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * シングルトン
     */
    private static AlexaUserInactivityReportManager mManager = null;

    /**
     * コンテキスト
     */
    private Context mContext = null;
    private Handler mHandler = null;

    /**
     * タイマー
     */
    private Timer mTimer = null;
    /**
     * イベント送信処理を行うタイマータスク
     */
    private TimerTask mTimerTask = null;

    /**
     * 実際に送信する時間（3600の倍数でなければならない仕様のため）
     */
    private long mInactiveTimeInSeconds = 0;
    /**
     * 今回の無操作期間の開始時間
     */
    private long mStartInactiveTimeInMilliSeconds = 0;
    /**
     * 最後にイベントを送信した時間
     */
    private long mLastSendTimeInMilliSeconds = 0;

    /**
     * イベント送信をコールバックするリスナー
     */
    private IAlexaUserInactivityReportCallback mCallback = null;

    /**
     * 送信間隔
     */
    private final static long INTERVAL_SECONDS = 3600;      // 1時間（3600秒）

    /**
     * プレファレンス保存用 プレファレンス名
     */
    private final static String NAME_PREFERENCES = "user_inactivity_report";
    /**
     * プレファレンス保存用 無操作開始時間 キー名
     */
    private final static String KEY_START_INACTIVE_TIME_IN_MILLI_SECONDS = "start_inactive_time_in_milli_seconds";
    /**
     * プレファレンス保存用 最後にイベント送信した時間 キー名
     */
    private final static String KEY_LAST_SEND_TIME_IN_MILLI_SECONDS = "last_send_time_in_milli_seconds";
    /**
     * プレファレンス保存用 イベントに設定する無操作時間の値 キー名
     */
    private final static String KEY_INACTIVE_TIME_IN_SECONDS = "inactive_time_in_seconds";

    /**
     * コールバックリスナー
     */
    public interface IAlexaUserInactivityReportCallback {
        public void onSendEvent(UserInactivityReportItem item);
        public void onResetTimer();
    }

    /**
     * コンストラクタ
     */
    private AlexaUserInactivityReportManager() {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mTimer = new Timer();
    }

    /**
     * インスタンス取得
     * @return
     */
    public static AlexaUserInactivityReportManager getInstance() {
        if (mManager == null) {
            mManager = new AlexaUserInactivityReportManager();
        }
        return mManager;
    }

    public static void resetManager() {
        if (mManager != null) {
            // コンテキストの削除
            mManager.mContext = null;
            // タイマーのキャンセル、破棄
            if (mManager.mTimer != null) {
                mManager.mTimer.cancel();
                mManager.mTimer = null;
            }
            // タイマータスクのキャンセル、破棄
            if (mManager.mTimerTask != null) {
                mManager.mTimerTask.cancel();
                mManager.mTimerTask = null;
            }
            // 送信時間の初期化
            mManager.mInactiveTimeInSeconds = 0;
            // 無操作開始時間の初期化
            mManager.mStartInactiveTimeInMilliSeconds = 0;
            // 前回イベント送信時間の初期化
            mManager.mLastSendTimeInMilliSeconds = 0;
            // コールバックインスタンスの削除
            mManager.mCallback = null;
        }
    }

    /**
     * 保存されている経過時間を復元
     */
    public void init(Context context) {
        if (DBG) android.util.Log.d(TAG, "initInactiveTime()");
        mContext = context;
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(NAME_PREFERENCES, Context.MODE_PRIVATE);
            this.mLastSendTimeInMilliSeconds = preferences.getLong(KEY_LAST_SEND_TIME_IN_MILLI_SECONDS, System.currentTimeMillis());
            this.mStartInactiveTimeInMilliSeconds = preferences.getLong(KEY_START_INACTIVE_TIME_IN_MILLI_SECONDS, mLastSendTimeInMilliSeconds);
            this.mInactiveTimeInSeconds = preferences.getLong(KEY_INACTIVE_TIME_IN_SECONDS, 0L);

            if (DBG) android.util.Log.d(TAG, " - initInactiveTime(), mLastSendTimeInMilliSeconds = " + mLastSendTimeInMilliSeconds);
            if (DBG) android.util.Log.d(TAG, " - initInactiveTime(), mStartInactiveTimeInMilliSeconds = " + mStartInactiveTimeInMilliSeconds);
            if (DBG) android.util.Log.d(TAG, " - initInactiveTime(), mInactiveTimeInSeconds = " + mInactiveTimeInSeconds);
        }
    }

    /**
     * 経過時間を保存
     */
    private void saveInactiveTime() {
        if (DBG) android.util.Log.d(TAG, "saveInactiveTime()");
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(NAME_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(KEY_LAST_SEND_TIME_IN_MILLI_SECONDS, this.mLastSendTimeInMilliSeconds);
            editor.putLong(KEY_START_INACTIVE_TIME_IN_MILLI_SECONDS, this.mStartInactiveTimeInMilliSeconds);
            editor.putLong(KEY_INACTIVE_TIME_IN_SECONDS, this.mInactiveTimeInSeconds);
            editor.commit();

            if (DBG) android.util.Log.d(TAG, " - saveInactiveTime(), mLastSendTimeInMilliSeconds = " + mLastSendTimeInMilliSeconds);
            if (DBG) android.util.Log.d(TAG, " - saveInactiveTime(), mStartInactiveTimeInMilliSeconds = " + mStartInactiveTimeInMilliSeconds);
            if (DBG) android.util.Log.d(TAG, " - saveInactiveTime(), mInactiveTimeInSeconds = " + mInactiveTimeInSeconds);
        }
    }

    /**
     * コールバックリスナー設定
     * @param callback
     */
    public void setAlexaUserInactivityReportCallback(IAlexaUserInactivityReportCallback callback) {
        this.mCallback = callback;
    }

    /**
     * 最後に送信した時間を取得
     * @return
     */
    public long getLastSendTimeInMilliSeconds() {
        return this.mLastSendTimeInMilliSeconds;
    }

    /**
     * 無操作タイマーを開始
     */
    public void startTimer() {
        if (DBG) android.util.Log.d(TAG, "startTimer()");
        // すでにスケジュール済みの場合は設定を行わない
        if (mTimerTask == null) {
            Long currentTime = System.currentTimeMillis();
            if (DBG) android.util.Log.d(TAG, " - startTimer(), currentTime = " + currentTime);

            if (this.mLastSendTimeInMilliSeconds <= 0) {
                // 初回
                setNextTask();
            } else if ((currentTime - this.mLastSendTimeInMilliSeconds) >= (INTERVAL_SECONDS * 1000)) {
                if (DBG) android.util.Log.d(TAG, " - startTimer(), case 1.");
                // 前回送信時から1時間以上経っていた場合。
                // 　->　すぐにEventを送信し、途中からタイマーを再開
                long intervalCount = (currentTime - mStartInactiveTimeInMilliSeconds) / (INTERVAL_SECONDS * 1000);    // 余り部分を除去
                long intervalRemainder = (currentTime - mStartInactiveTimeInMilliSeconds) % (INTERVAL_SECONDS * 1000);    // 余り部分を抽出
                this.mInactiveTimeInSeconds = intervalCount * INTERVAL_SECONDS;
                sendEvent();
                setNextTask(intervalRemainder);
            } else {
                if (DBG) android.util.Log.d(TAG, " - startTimer(), case 2.");
                // 前回送信時から1時間未満の場合。
                // 　->　途中からタイマーを再開
                setNextTask((mLastSendTimeInMilliSeconds + INTERVAL_SECONDS * 1000) - currentTime);
            }
            // 初期値を保存
            saveInactiveTime();
        }
    }

    /**
     * イベント送信間隔をリセットする
     * [使用ケース]・音声入力／再生コントロールが操作された時
     */
    public void resetTimer() {
        if (DBG) android.util.Log.d(TAG, "resetTimer()");
        this.mInactiveTimeInSeconds = 0;
        this.mStartInactiveTimeInMilliSeconds = System.currentTimeMillis();
        this.mLastSendTimeInMilliSeconds = this.mStartInactiveTimeInMilliSeconds;
        // リセット後の値を保存
        saveInactiveTime();

        if (this.mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onResetTimer();
                }
            }
        });
        this.setNextTask();
    }

    /**
     * UserInactivityReportイベントをすぐに送信する
     * [使用ケース]・アプリ起動時、すぐにEventを送信する場合
     */
    private void sendEvent() {
        if (DBG) android.util.Log.d(TAG, "sendEvent()");
        if (this.mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                // ここでは、mInactiveTimeInSecondsの更新はしない
                sendUserInactivityReportEvent();
            }
        };
        thread.start();
    }

    /**
     * 次のイベント送信に向けたタイマーを開始する
     */
    private void setNextTask() {
        if (DBG) android.util.Log.d(TAG, "setNextTask(1)");
        this.setNextTask(INTERVAL_SECONDS * 1000);
    }

    /**
     * 次のイベント送信に向けたタイマーを開始する
     */
    private void setNextTask(long delay) {
        if (delay < 0) {
            return;
        }
        if (DBG) android.util.Log.d(TAG, "setNextTask(2), delay = " + delay);
        this.mTimerTask = new TimerTask() {
            @Override
            public void run() {
                // UIスレッドではないので直接呼ぶ
                mInactiveTimeInSeconds = mInactiveTimeInSeconds + INTERVAL_SECONDS;
                sendUserInactivityReportEvent();
                setNextTask();
            }
        };
        if (DBG)
            android.util.Log.d(TAG, " - setNextTask(), mInactiveTimeInSeconds = " + mInactiveTimeInSeconds);
        if (mTimer != null) {
            this.mTimer.schedule(mTimerTask, delay);
        }
    }

    /**
     * UserInactivityReportイベント送信
     */
    private void sendUserInactivityReportEvent() {
        if (DBG) android.util.Log.d(TAG, "sendUserInactivityReportEvent()");
        if (DBG) android.util.Log.d(TAG, " - sendUserInactivityReportEvent(), mInactiveTimeInSeconds = " + mInactiveTimeInSeconds);
        final UserInactivityReportItem event = new UserInactivityReportItem(mInactiveTimeInSeconds);
        AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

            @Override
            public void onExecute(Call call) {
                if (DBG) android.util.Log.d(TAG, "UserInactivityReport onExecute()");
            }

            @Override
            public void onResponse(Call call, int httpCode) {
                if (DBG) android.util.Log.d(TAG, "UserInactivityReport onResponse()");

                if (200 <= httpCode && httpCode < 300) {
                    // 成功
                    if (DBG) android.util.Log.d(TAG, " - UserInactivityReport onResponse(), Success");
                }
                else {
                    // 失敗
                    if (DBG) android.util.Log.w(TAG, " - UserInactivityReport onResponse(), Error");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (DBG) android.util.Log.w(TAG, "UserInactivityReport onFailure(), e = " + e);
            }

            @Override
            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                if (DBG) android.util.Log.w(TAG, "UserInactivityReport onParsedResponse(), itemList = " + itemList);
            }
        });
        this.mLastSendTimeInMilliSeconds = System.currentTimeMillis();
        if (DBG) android.util.Log.d(TAG, " - sendUserInactivityReportEvent(), mLastSendTimeInMilliSeconds = " + mLastSendTimeInMilliSeconds);
        // 送信するたびに保存
        saveInactiveTime();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onSendEvent(event);
                }
            }
        });
    }

}
