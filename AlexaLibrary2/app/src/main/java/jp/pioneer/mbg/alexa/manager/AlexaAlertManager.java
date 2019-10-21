package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-sakamori on 2017/08/25.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfEventItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.DeleteAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.Alerts.SetAlertItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.AlertEnteredBackgroundItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.AlertEnteredForegroundItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.AlertStartedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.AlertStoppedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.DeleteAlertFailedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.DeleteAlertSucceededItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.SetAlertFailedItem;
import jp.pioneer.mbg.alexa.AlexaInterface.event.Alerts.SetAlertSucceededItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.player.WLPlayer;
import jp.pioneer.mbg.alexa.util.AssetCacheUtil;
import jp.pioneer.mbg.android.vozsis.R;
import okhttp3.Call;


/**
 * アラートを管理するマネージャ
 */
public class AlexaAlertManager {
    private final static String TAG = AlexaAlertManager.class.getSimpleName();
    private static final boolean DBG = true;
    /**
     * シングルトンインスタンス
     */
    private static AlexaAlertManager mAlexaAlertManager = null;

    /**
     * コールバック用ハンドラー
     */
    private Handler mHandler = null;
    /**
     * 設定中アラートリスト
     */
    private ArrayList<SetAlertItem> mAlertList = null;
    /**
     * 鳴動時間になったアラートリスト
     */
    private ArrayList<SetAlertItem> mActiveAlertList = null;

    /**
     * 鳴動時間中にAudioFocusが奪われたアラートリスト
     */
    private ArrayList<SetAlertItem> mFocusLossAlertList = null;

    /**
     * アラートQueueの動作を一時停止するフラグ
     */
    private boolean mIsAlertQueueBlockFlag = false;

    /**
     * アラート鳴動待ちのハッシュマップ
     */
    private HashMap<SetAlertItem, AlertActivateRunnable> mAlertMap = null;

    /**
     * 送信に失敗したイベントのリスト（エラーハンドリング用）
     */
    private ArrayList<AlexaIfEventItem> mSendFailedItemList = null;

    /**
     * コンテキスト
     */
    private Context mContext = null;

    /**
     * AlexaAlertManagerの終了フラグ
     */
    private boolean mIsFinish = false;

    /**
     * 鳴動中のアラート
     */
    private SetAlertItem mCurrentAlertItem = null;

    /**
     * アラート鳴動Runnable
     */
    private IAlertRingingRunnable mCurrentAlertRunnable = null;

    /**
     * アラートのプレーヤー
     */
    private MediaPlayer mAlertPlayer = null;

    /**
     * アラートのWeblinkAudio対応プレーヤー
     */
    private WLPlayer mWLAlertPlayer = null;

    /**
     * アラート機能動作のコールバック
     */
    private IAlertCallback mIAlertCallback = null;

    /**
     * アラート再生／停止のコールバックインターフェース
     */
    public interface IAlertCallback {
        /**
         * アラート再生開始
         */
        void onAlertStarted();

        /**
         * ショートアラート再生開始
         */
        void onShortAlertStarted();

        /**
         * アラート再生停止
         */
        void onAlertFinished();

        /**
         * アラートのセット
         */
        void onSetAlert();

        /**
         * アラートの消去
         */
        void onStopAlertAll();
    }

    /**
     * コンストラクタ
     */
    private AlexaAlertManager() {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mAlertList = new ArrayList<>();
        this.mActiveAlertList = new ArrayList<>();
        this.mFocusLossAlertList = new ArrayList<>();
        this.mAlertMap = new HashMap<>();
        this.mSendFailedItemList = new ArrayList<>();
    }

    /**
     * 生成済みシングルトンインスタンス取得
     * @return
     */
    public static AlexaAlertManager getInstance() {
        if (mAlexaAlertManager == null) {
            mAlexaAlertManager = new AlexaAlertManager();
        }
        return mAlexaAlertManager;
    }

    /**
     * マネージャのリセット
     */
    public static void resetManager() {
        //mAlexaAlertManager.mIsFinish = true;        // 鳴動中アラートの停止フラグON

        // メモリ上のアラートをクリア
        mAlexaAlertManager.mAlertList.clear();      // アラートリストのクリア
        mAlexaAlertManager.mActiveAlertList.clear();    // アクティブアラートリストのクリア
        mAlexaAlertManager.mFocusLossAlertList.clear(); // FocusLoss時のアクティブアラートリストのクリア
        mAlexaAlertManager.mAlertMap.clear();       // アラート対Runnableのマップクリア
        // エラーイベントリストを初期化
        mAlexaAlertManager.mSendFailedItemList.clear();

        if (mAlexaAlertManager.mCurrentAlertRunnable != null) {
            mAlexaAlertManager.mCurrentAlertRunnable.cancel();
            mAlexaAlertManager.mCurrentAlertRunnable = null;
        }
        if (mAlexaAlertManager.mAlertPlayer != null) {
            mAlexaAlertManager.mAlertPlayer.stop();
            mAlexaAlertManager.mAlertPlayer.release();
            mAlexaAlertManager.mAlertPlayer = null;
        }
        // Queue一時停止フラグを初期化
        mAlexaAlertManager.mIsAlertQueueBlockFlag = false;
        // 鳴動中のアラートを削除
        mAlexaAlertManager.mCurrentAlertItem = null;
        // コールバックを削除
        mAlexaAlertManager.mIAlertCallback = null;
        // コンテキストを削除
        mAlexaAlertManager.mContext = null;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * コールバックリスナーの設定
     * @param callback
     */
    public void setAlertCallback(IAlertCallback callback) {
        this.mIAlertCallback = callback;
    }

    /**
     * 設定中のアラートリスト取得
     * @return
     */
    public List<SetAlertItem> getAlertList() {
        return mAlertList;
    }

    /**
     * 鳴動時間になっているアラートリストの取得
     * @return
     */
    public List<SetAlertItem> getActiveAlertList() {
        return mActiveAlertList;
    }

    /**
     * context作成時はFocusLossAlertListとActiveAlertListを両方取得
     */
    public List<SetAlertItem> makeContextActiveAlertList() {
        List<SetAlertItem> returnList = new ArrayList<>();
        for (SetAlertItem item : mActiveAlertList) {
            returnList.add(item);
        }
        for (SetAlertItem item : mFocusLossAlertList) {
            returnList.add(item);
        }
        return returnList;
    }

    /**
     * 鳴動中のSetAlertItemを取得
     * @return
     */
    public SetAlertItem getCurrentAlertItem() {
        return mCurrentAlertItem;
    }

    DatabaseManager mDatabaseManager = null;
    private DatabaseManager getDatabaseManager() {
        if (mDatabaseManager == null) {
            mDatabaseManager = new DatabaseManager(mContext);
        }
        return mDatabaseManager;
    }
    /**
     * アラートのデータベース登録
     * @param alertItem
     */
    public void insertAlertItem(final SetAlertItem alertItem) {
        DatabaseManager manager = getDatabaseManager();
        SQLiteDatabase db = manager.getWritableDatabase();
        db.beginTransaction();

        long alertId;
        long result = -1;

        try {
            // alert_table
            ContentValues alertValues = new ContentValues();
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.TOKEN.getColumnName(), alertItem.token);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.TYPE.getColumnName(), alertItem.type);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.SCHEDULED_TIME.getColumnName(), alertItem.scheduledTime);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.BACKGROUND_ALERT_ASSET.getColumnName(), alertItem.backgroundAlertAsset);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_COUNT.getColumnName(), alertItem.loopCount);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_PAUSE_IN_MILLI_SECONDS.getColumnName(), alertItem.loopPauseInMilliseconds);
            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.STOP_FLAG.getColumnName(), 0);

            alertId = db.insert(DatabaseConstant.AlertTable.TABLE_NAME, null, alertValues);
            if (DBG) android.util.Log.d("test", "insert alert = " + alertId);

            String[] assetPlayOrderArray = null;
            if (alertItem.assetPlayOrder.size() > 0) {
                assetPlayOrderArray = (String[]) alertItem.assetPlayOrder.toArray(new String[alertItem.assetPlayOrder.size()]);
            }
            // asset_play_oder_table
            for (int i = 0; i < alertItem.assetPlayOrder.size(); i++) {
                ContentValues assetPlayOderValues = new ContentValues();


                assetPlayOderValues.put(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ALERT_ID.getColumnName(), alertId);
                assetPlayOderValues.put(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.SEQUENCE_NUMBER.getColumnName(), i);
                assetPlayOderValues.put(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ASSET_ID.getColumnName(), assetPlayOrderArray[i]);

                result = db.insert(DatabaseConstant.AssetPlayOderTable.TABLE_NAME, null, assetPlayOderValues);
                if (DBG) android.util.Log.d("test", "insert asset_play_oder_table = " + result);
            }

            // asset_cache_table
            for (int i = 0; i < alertItem.assets.size(); i++) {
                AlexaIfDirectiveItem.Asset asset = alertItem.assets.get(i);

                ContentValues assetCacheValues = new ContentValues();
                assetCacheValues.put(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ALERT_ID.getColumnName(), alertId);
                assetCacheValues.put(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ASSET_ID.getColumnName(), asset.assetId);
                assetCacheValues.put(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.URL.getColumnName(), asset.url);
                byte[] assetCache = AssetCacheUtil.getAssetCache(asset.url);
                assetCacheValues.put(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.CACHE.getColumnName(), assetCache);
                asset.setCache(assetCache);

                result = db.insert(DatabaseConstant.AssetCacheTable.TABLE_NAME, null, assetCacheValues);
                if (DBG) android.util.Log.d("test", "insert asset_cache_table = " + result);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        // インサートしたAlertのインスタンスにIDを設定
        alertItem.setAlertId(alertId);
    }

    /**
     * アラートをデータベースから削除(token)
     * @param token
     */
    public void deleteAlertItem(String token) {
        // tokenをDB上のidに変換して削除する
        DatabaseManager manager = getDatabaseManager();
        Cursor cursor = null;

        if (DBG) android.util.Log.d("test", "#####################");
        if (DBG) android.util.Log.d("test", "AlertTable");
        if (DBG) android.util.Log.d("test", "#####################");
        String[] selectionArgs = {token};
        String[] projection = {DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName()};
        long id = 0;
        try {
            cursor = manager.query(DatabaseConstant.AlertTable.TABLE_NAME, projection, DatabaseConstant.AlertTable.AlertTableColumns.TOKEN.getColumnName() + " = ? ", selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    id = cursor.getInt(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName()));
                    break;
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }

        }
        deleteAlertItem(id);
    }

    /**
     * アラートをデータベースから削除(alertId)
     * @param alertId
     */
    public void deleteAlertItem(long alertId) {
        DatabaseManager manager = getDatabaseManager();
        SQLiteDatabase db = manager.getWritableDatabase();
        db.beginTransaction();
        try {
                {
                long result = -1;
                String tableName = DatabaseConstant.AlertTable.TABLE_NAME;
                String whereClause = DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName() + " = ?";
                String[] whereArgs = new String[] {
                        "" + alertId
                };
                result = db.delete(tableName, whereClause, whereArgs);
                    if (DBG) android.util.Log.d("test", "delete alert = " + result);
            }

            {
                long result = -1;
                String tableName = DatabaseConstant.AssetPlayOderTable.TABLE_NAME;
                String whereClause = DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ALERT_ID.getColumnName() + " = ?";
                String[] whereArgs = new String[] {
                        "" + alertId
                };
                result = db.delete(tableName, whereClause, whereArgs);
                if (DBG) android.util.Log.d("test", "delete assetPlayOder = " + result);
            }

            {
                long result = -1;
                String tableName = DatabaseConstant.AssetCacheTable.TABLE_NAME;
                String whereClause = DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ALERT_ID.getColumnName() + " = ?";
                String[] whereArgs = new String[] {
                        "" + alertId
                };
                result = db.delete(tableName, whereClause, whereArgs);
                if (DBG) android.util.Log.d("test", "delete assetCacheTable = " + result);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * データベースのアラート更新(stop_flagを更新)
     * @param alertId
     * @param stopflag
     */
    public void updateAlertItem(long alertId ,int stopflag) {
        DatabaseManager manager = getDatabaseManager();
        SQLiteDatabase db = manager.getWritableDatabase();
        db.beginTransaction();
        try {
            long result = -1;
            String tableName = DatabaseConstant.AlertTable.TABLE_NAME;

            ContentValues alertValues = new ContentValues();

            alertValues.put(DatabaseConstant.AlertTable.AlertTableColumns.STOP_FLAG.getColumnName(), stopflag);
            String whereClause = DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName() + " = ?";

            String[] whereArgs = new String[]{
                    "" + alertId
            };
            result = db.update(tableName, alertValues, whereClause, whereArgs);
            if (DBG) android.util.Log.d("test", "update alert = " + result);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * データベースのアラート一覧取得
     * @return
     */
    private ArrayList<SetAlertItem> selectAlertItem() {
        ArrayList<SetAlertItem> result = new ArrayList<>();

        DatabaseManager manager = getDatabaseManager();
        Cursor cursor = null;

        if (DBG) android.util.Log.d("test", "#####################");
        if (DBG) android.util.Log.d("test", "AlertTable");
        if (DBG) android.util.Log.d("test", "#####################");
        try {
            cursor = manager.query(DatabaseConstant.AlertTable.TABLE_NAME, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {

                do {
                    long id = cursor.getInt(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName()));
                    String token = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.TOKEN.getColumnName()));
                    String type = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.TYPE.getColumnName()));
                    String scheduledTime = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.SCHEDULED_TIME.getColumnName()));
                    String backgroundAlertAsset = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.BACKGROUND_ALERT_ASSET.getColumnName()));
                    Long loopCount = null;
                    if (!cursor.isNull(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_COUNT.getColumnName()))) {
                        loopCount = cursor.getLong(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_COUNT.getColumnName()));
                    }
                    Long loopPauseInMilliSeconds = null;
                    if (!cursor.isNull(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_PAUSE_IN_MILLI_SECONDS.getColumnName()))) {
                        loopPauseInMilliSeconds = cursor.getLong(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.LOOP_PAUSE_IN_MILLI_SECONDS.getColumnName()));
                    }
                    SetAlertItem item = new SetAlertItem();
                    item.alertId = id;
                    item.token = token;
                    item.type = type;
                    item.scheduledTime = scheduledTime;
                    item.backgroundAlertAsset = backgroundAlertAsset;
                    item.loopCount = loopCount;
                    item.loopPauseInMilliseconds = loopPauseInMilliSeconds;

                    result.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        if (DBG) android.util.Log.d("test", "#####################");
        if (DBG) android.util.Log.d("test", "AssetPlayOderTable");
        if (DBG) android.util.Log.d("test", "#####################");
        ArrayList<String> playOrderList = new ArrayList<>();

        String sortOrder = DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ALERT_ID.getColumnName() + " , "
                + DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.SEQUENCE_NUMBER.getColumnName() + " ASC";

        try {
            cursor = manager.query(DatabaseConstant.AssetPlayOderTable.TABLE_NAME, null, null, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                long tempAlertId = -1;
                // アラートのAssetを配列にして読み込む
                ArrayList<String> assetList = new ArrayList<>();
                do {
                    long alertId = cursor.getLong(cursor.getColumnIndex(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ALERT_ID.getColumnName()));
                    long sequenceNumber = cursor.getLong(cursor.getColumnIndex(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.SEQUENCE_NUMBER.getColumnName()));
                    String assetId = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AssetPlayOderTable.AssetPlayOderTableColumns.ASSET_ID.getColumnName()));

                    if (alertId == -1 || alertId != tempAlertId) {
                        // 前回取得したIDと異なっていた場合、新しいアラートが取得された。
                        if (tempAlertId != -1) {
                            for (SetAlertItem item : result) {
                                if (item.alertId == alertId) {
                                    // alertIdが一致するアイテムに登録
                                    item.assetPlayOrder = assetList;
                                    assetList = new ArrayList<>();
                                    break;
                                }
                            }
                        }
                        tempAlertId = alertId;
                    }
                    assetList.add(assetId);

                } while (cursor.moveToNext());
                if (tempAlertId != -1) {
                    // DBのデータを最後まで読み込んだ
                    for (SetAlertItem item : result) {
                        if (item.alertId == tempAlertId) {
                            item.assetPlayOrder = assetList;
                            break;
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        if (DBG) android.util.Log.d("test", "#####################");
        if (DBG) android.util.Log.d("test", "AssetCacheTable");
        if (DBG) android.util.Log.d("test", "#####################");
        // alertIdでソートする
        sortOrder = DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ALERT_ID.getColumnName() + " ASC";
        try {
            cursor = manager.query(DatabaseConstant.AssetCacheTable.TABLE_NAME, null, null, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                long tempAlertId = -1;
                ArrayList<AlexaIfDirectiveItem.Asset> assetList = new ArrayList<>();
                do {
                    long alertId = cursor.getLong(cursor.getColumnIndex(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ALERT_ID.getColumnName()));
                    String assetId = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.ASSET_ID.getColumnName()));
                    String url = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.URL.getColumnName()));
                    byte[] cache = cursor.getBlob(cursor.getColumnIndex(DatabaseConstant.AssetCacheTable.AssetCacheTableColumns.CACHE.getColumnName()));

                    AlexaIfDirectiveItem.Asset asset = new AlexaIfDirectiveItem.Asset();
                    asset.setAssetId(assetId);
                    asset.setUrl(url);
                    asset.setCache(cache);

                    if (alertId == -1 || alertId != tempAlertId) {
                        // 前回取得したIDと異なっていた場合、新しいアラートが取得された。
                        if (tempAlertId != -1) {
                            for (SetAlertItem item : result) {
                                if (item.alertId == alertId) {
                                    // alertIdが一致するアイテムに登録
                                    item.assets = assetList;
                                    assetList = new ArrayList<>();
                                    break;
                                }
                            }
                        }
                        tempAlertId = alertId;
                    }
                    assetList.add(asset);

                } while (cursor.moveToNext());
                if (tempAlertId != -1) {
                    // DBのデータを最後まで読み込んだ
                    for (SetAlertItem item : result) {
                        if (item.alertId == tempAlertId) {
                            item.assets = assetList;
                            break;
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    /**
     * スケジューリングされたアラート復帰処理
     */
    public synchronized void rescheduleAlert(){
        DatabaseManager manager = getDatabaseManager();
        Cursor cursor = null;

        // 前回の値が残っている可能性を考えてクリア処理
        if (mAlertMap.size() > 0) {
            Set<SetAlertItem> keySet = mAlertMap.keySet();
            for (SetAlertItem item : keySet) {
                if (mAlertMap.containsKey(item)) {
                    mHandler.removeCallbacks(mAlertMap.get(item));
                    mAlertList.remove(item);
                }
            }
        }
        mAlertList.clear();
        mActiveAlertList.clear();
        mFocusLossAlertList.clear();
        mAlertMap.clear();

        try {
            // すべてのアラートを取得
            cursor = manager.query(DatabaseConstant.AlertTable.TABLE_NAME, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {

                //今から鳴らすやつ
                ArrayList<Long> arrayList1 = new ArrayList<>();
                //後で鳴らすやつ
                ArrayList<Long> arrayList2 = new ArrayList<>();

                do {
                    long stopFlag = cursor.getInt(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.STOP_FLAG.getColumnName()));
                    final long id = cursor.getInt(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns._ID.getColumnName()));
                    final String token = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.TOKEN.getColumnName()));
                    //long→String
                    String scheduledTime = cursor.getString(cursor.getColumnIndex(DatabaseConstant.AlertTable.AlertTableColumns.SCHEDULED_TIME.getColumnName()));
                    if (DBG) android.util.Log.i(TAG, " - selectStopFlag Token = " + token );


                    if (stopFlag == 1) {
                        // AlertStoppedイベントのみ送信
                        final AlertStoppedItem event = new AlertStoppedItem(token);
                        AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                            @Override
                            public void onExecute(Call call) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onExecute()");
                            }

                            @Override
                            public void onResponse(Call call, int httpCode) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onResponse()");

                                if (200 <= httpCode && httpCode < 300) {
                                    // 成功
                                    if (DBG) android.util.Log.d(TAG, " - AlertStopped onResponse(), Success");
                                    deleteAlertItem(id);
                                } else {
                                    // 失敗
                                    //TODO　削除しないで次の処理へ
                                    if (DBG) android.util.Log.w(TAG, " - AlertStopped onResponse(), Error");
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onFailure(), e = " + e);
                                mSendFailedItemList.add(event);
                            }

                            @Override
                            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onParsedResponse(), itemList = " + itemList);
                            }
                        });
                    }
                    else if(stopFlag==2){
                        // DeleteAlertSucceededイベントのみ送信
                        final DeleteAlertSucceededItem event = new DeleteAlertSucceededItem(token);
                        AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                            @Override
                            public void onExecute(Call call) {
                                if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onExecute()");
                            }

                            @Override
                            public void onResponse(Call call, int httpCode) {
                                if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onResponse()");

                                if (200 <= httpCode && httpCode < 300) {
                                    // 成功
                                    if (DBG) android.util.Log.d(TAG, " - DeleteAlertSucceeded onResponse(), Success");
                                    deleteAlertItem(id);
                                }
                                else {
                                    // 失敗
                                    //TODO　削除しないで次の処理へ
                                    if (DBG) android.util.Log.w(TAG, " - DeleteAlertSucceeded onResponse(), Error");
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onFailure(), e = " + e);
                                mSendFailedItemList.add(event);
                            }

                            @Override
                            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onParsedResponse(), itemList = " + itemList);
                            }
                        });
                    }
                    else if(stopFlag==3) {
                        //AlertStoppedとDeleteAlertSucceededの両方を送信
                        final AlertStoppedItem event = new AlertStoppedItem(token);
                        AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                            @Override
                            public void onExecute(Call call) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onExecute()");
                            }

                            @Override
                            public void onResponse(Call call, int httpCode) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onResponse()");

                                if (200 <= httpCode && httpCode < 300) {
                                    // 成功
                                    if (DBG) android.util.Log.d(TAG, " - AlertStopped onResponse(), Success");
                                    //Stopに成功したらDeleteSuccessを送信
                                    final DeleteAlertSucceededItem deleteEvent = new DeleteAlertSucceededItem(token);
                                    AlexaEventManager.sendEvent(TokenManager.getToken(), deleteEvent, mContext, new AlexaEventManager.AlexaCallback() {
                                        @Override
                                        public void onExecute(Call call) {
                                            if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onExecute()");
                                        }
                                        @Override
                                        public void onResponse(Call call, int httpCode) {
                                            if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onResponse()");
//                                            if (response.isSuccessful()) {
                                            if (200 <= httpCode && httpCode < 300) {
                                                // 成功
                                                if (DBG) android.util.Log.d(TAG, " - DeleteAlertSucceeded onResponse(), Success");
                                                deleteAlertItem(id);
                                            } else {
                                                // 失敗
                                                //TODO　削除しないで次の処理へ
                                                if (DBG) android.util.Log.w(TAG, " - DeleteAlertSucceeded onResponse(), Error");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onFailure(), e = " + e);
                                            mSendFailedItemList.add(deleteEvent);
                                        }

                                        @Override
                                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                            if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onParsedResponse(), itemList = " + itemList);
                                        }
                                    });
                                } else {
                                    // 失敗
                                    //TODO　削除しないで次の処理へ
                                    if (DBG) android.util.Log.w(TAG, " - AlertStopped onResponse(), Error");
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onFailure(), e = " + e);
                                mSendFailedItemList.add(event);
                                final DeleteAlertSucceededItem deleteEvent = new DeleteAlertSucceededItem(token);
                                mSendFailedItemList.add(deleteEvent);
                            }

                            @Override
                            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onParsedResponse(), itemList = " + itemList);
                            }
                        });
                    } else {
                        // 停止処理が行われていないアラート
                        long alertTime = 0;
                        {
                            long parsedTime = parseScheduledTime(scheduledTime);
                            long currentTime = System.currentTimeMillis();
                            alertTime = parsedTime - currentTime;
                        }
                        //期限切れアラート復帰処理
                        if (alertTime < -60 * 30 * 1000) {
                            // 今から30分以上前に鳴動時間を迎えたアラート
                            final AlertStoppedItem event = new AlertStoppedItem(token);
                            AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                                @Override
                                public void onExecute(Call call) {
                                    if (DBG) android.util.Log.d(TAG, "AlertStopped onExecute()");
                                }

                                @Override
                                public void onResponse(Call call, int httpCode) {
                                    if (DBG) android.util.Log.d(TAG, "AlertStopped onResponse()");

                                    if (200 <= httpCode && httpCode < 300) {
                                        // 成功
                                        if (DBG) android.util.Log.d(TAG, " - AlertStopped onResponse(), Success");
                                        deleteAlertItem(id);
                                    } else {
                                        // 失敗
                                        if (DBG) android.util.Log.w(TAG, " - AlertStopped onResponse(), Error");
                                    }
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    if (DBG) android.util.Log.w(TAG, "AlertStopped onFailure(), e = " + e);
                                    mSendFailedItemList.add(event);
                                }

                                @Override
                                public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                    if (DBG) android.util.Log.w(TAG, "AlertStopped onParsedResponse(), itemList = " + itemList);
                                }
                            });

                        }
                        //未処理アラート復帰処理
                        else if(0 > alertTime && alertTime > - 60 * 30 * 1000){
                            arrayList1.add(id);
                            mIAlertCallback.onSetAlert();
                        }
                        //鳴動していなかったアラート復帰処理
                        else if(alertTime > 0) {
                            arrayList2.add(id);
                            mIAlertCallback.onSetAlert();
                        }
                    }
                } while (cursor.moveToNext());
                //未処理アラート復帰処理
                {
                    ArrayList<SetAlertItem> arrayList = selectAlertItem();
                    for(SetAlertItem s : arrayList){
                        if(arrayList1.contains(s.getAlertId())){
                            mAlertList.add(s);
                            // すぐに鳴動させる
                            doAlertStart(s);
                        }
                        else if(arrayList2.contains(s.getAlertId())){
                            mAlertList.add(s);
                            long alertTime = 0;
                            {
                                long scheduledTime = parseScheduledTime(s.scheduledTime);
                                long currentTime = System.currentTimeMillis();
                                alertTime = scheduledTime - currentTime;
                            }
                            // タイマーを設定
                            doAlertStart(s, alertTime);
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    /**
     * Event送信失敗したアラートのイベント送信リトライ処理
     */
    public void retrySendAlertEvent() {
        final ArrayList<AlexaIfEventItem> tempList = new ArrayList<>(mSendFailedItemList);
        mSendFailedItemList.clear();
        if (tempList.size() > 0) {
            retrySendAlertEvent(tempList.get(0), new RetrySendEventRunnable.Callback() {
                @Override
                public void onSuccess(AlexaIfEventItem alexaIfItem) {
                    android.util.Log.d(TAG, alexaIfItem.getName() + " onSuccess()");
                    tempList.remove(alexaIfItem);
                    if(tempList.size() > 0) {
                        retrySendAlertEvent(tempList.get(0), this);
                    }
                }

                @Override
                public void onFailure(AlexaIfEventItem alexaIfItem) {
                    android.util.Log.d(TAG, alexaIfItem.getName() + " onFailure()");
                    mSendFailedItemList.add(alexaIfItem);
                    tempList.remove(alexaIfItem);
                    if(tempList.size() > 0){
                        retrySendAlertEvent(tempList.get(0), this);

                    }
                }
            });
        }
    }

    private void retrySendAlertEvent(AlexaIfEventItem alexaIfItem, RetrySendEventRunnable.Callback callback) {
        RetrySendEventRunnable retrySendEventRunnable = new RetrySendEventRunnable(mContext, alexaIfItem, callback);
        Thread thread = new Thread(retrySendEventRunnable);
        thread.start();
    }

    /**
     * 手動でアラートを停止した場合.
     * -> 再生中のアラートのみ停止させる
     */
    public void onAlertStop() {
        if (DBG) android.util.Log.d(TAG, "onAlertStop()");
        if (mCurrentAlertItem != null) {
            this.stopAlert(false);
            int stopFlag = 1;
            boolean result = false;
            SetAlertItem alertItem = null;
            for (SetAlertItem item : mAlertList) {
                if (mCurrentAlertItem.token.equals(item.token) == true) {
                    // トークンが一致するものがある
                    alertItem = item;
                    break;
                }
            }
            if (alertItem != null) {
                result = mAlertList.remove(alertItem);
                if(DBG)android.util.Log.d(TAG, " - onStopAlert() : mAlertList.size = " + mAlertList.size());
                if(mAlertList.size() == 0) {
                    mIAlertCallback.onStopAlertAll();
                }
                if (mAlertMap.containsKey(alertItem)) {
                    if (DBG) android.util.Log.d(TAG, " - onAlertStop(), remove Runnable, Runnable = " + mAlertMap.get(alertItem));
                    // コールバックを解除
                    mHandler.removeCallbacks(mAlertMap.get(alertItem));
                    mAlertMap.remove(alertItem);
                }
                if (mCurrentAlertRunnable != null) {
                    mCurrentAlertRunnable.finish();
                }
            }
        }
    }

    /**
     * アラートの設定
     * @param alertItem
     * @return
     */
    public boolean setAlert(final SetAlertItem alertItem) {
        //AlertDB対応
        insertAlertItem(alertItem);
        if (DBG) android.util.Log.d(TAG, "setAlert()");
        boolean result = false;
        if (mAlertList.contains(alertItem) == false) {
            result = mAlertList.add(alertItem);
            mIAlertCallback.onSetAlert();

            // Handlerにアラームを設定
            long alertTime = 0;
            {
                long scheduledTime = parseScheduledTime(alertItem.scheduledTime);
                long currentTime = System.currentTimeMillis();
                alertTime = scheduledTime - currentTime;
            }
            if(DBG)android.util.Log.i(TAG, " - setAlert(), AlertTime after " + alertTime + " ms.");
            doAlertStart(alertItem, alertTime);
        }

        if (result) {
            final SetAlertSucceededItem event = new SetAlertSucceededItem(alertItem.token);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "SetAlertSucceeded onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "SetAlertSucceeded onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - SetAlertSucceeded onResponse(), Success");
                            } else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - SetAlertSucceeded onResponse(), Error");
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, "SetAlertSucceeded onFailure(), e = " + e);
                            mSendFailedItemList.add(event);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, "SetAlertSucceeded onParsedResponse(), itemList = " + itemList);
                        }
                    });
                }
            });
            t.start();
        } else {
            final SetAlertFailedItem event = new SetAlertFailedItem(alertItem.token);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "SetAlertFailed onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "SetAlertFailed onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - SetAlertFailed onResponse(), Success");
                            } else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - SetAlertFailed onResponse(), Error");
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, "SetAlertFailed onFailure(), e = " + e);
                            mSendFailedItemList.add(event);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, "SetAlertFailed onParsedResponse(), itemList = " + itemList);
                        }
                    });
                }
            });
            t.start();
        }
        return result;
    }

    /**
     * アラートの鳴動停止
     */
    public void stop(){
        if (mWLAlertPlayer != null) {
            mWLAlertPlayer.stop();
        }
        else if (mAlertPlayer != null) {
            if (DBG) android.util.Log.d(TAG, " - stop()");
            mAlertPlayer.stop();
        }
    }

    /**
     * アラートの解除（DeleteAlertディレクティブ）
     * @param deleteItem
     * @return
     */
    public boolean deleteAlert(DeleteAlertItem deleteItem) {
        if (DBG) android.util.Log.d(TAG, "deleteAlert()");
        boolean result = false;
        int stopFlag = 0;
        SetAlertItem alertItem = null;
        for (SetAlertItem item : mAlertList) {
            if (deleteItem.token.equals(item.token) == true) {
                // トークンが一致するものがある
                alertItem = item;
                break;
            }
        }

        if (alertItem != null) {
            result = mAlertList.remove(alertItem);
            if(DBG)android.util.Log.d(TAG, " - deleteAlert() : mAlertList.size = " + mAlertList.size());
            if(mAlertList.size() == 0) {
                mIAlertCallback.onStopAlertAll();
            }
            if (mAlertMap.containsKey(alertItem)) {
                if (DBG) android.util.Log.d(TAG, " - deleteAlert(), remove Runnable, Runnable = " + mAlertMap.get(alertItem));
                // コールバックを解除
                mHandler.removeCallbacks(mAlertMap.get(alertItem));
                mAlertMap.remove(alertItem);
            }
            if (mActiveAlertList.contains(alertItem) == true) {
                if (mCurrentAlertItem == alertItem) {
                    if (DBG) android.util.Log.d(TAG, " - deleteAlert(), Active alert stop. AlertItem =  " + alertItem);
                    // 現在鳴動中のアラートを停止
                    onAlertStop();
                    if (mCurrentAlertRunnable != null) {
                        mCurrentAlertRunnable.cancel();
                    }
                }
                stopFlag=3;
            } else if(mFocusLossAlertList.contains(alertItem)){
                for(SetAlertItem item : mFocusLossAlertList){
                    if(item == alertItem) {
                        mFocusLossAlertList.remove(item);
                        break;
                    }
                }
                stopFlag = 3;
            }
            else{
                stopFlag=2;
            }

            updateAlertItem(alertItem.getAlertId(), stopFlag);
            final SetAlertItem tempAlertItem = alertItem;

            if (result) {
                if(stopFlag == 3) {
                    // TODO:DeleteAlertを受信した場合、AlertStopを送信した後に、AlertSucceededを送信する
                    final AlertStoppedItem event = new AlertStoppedItem(alertItem.token);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                                @Override
                                public void onExecute(Call call) {
                                    if (DBG) android.util.Log.d(TAG, "AlertStopped onExecute()");
                                }

                                @Override
                                public void onResponse(Call call, int httpCode) {
                                    if (DBG) android.util.Log.d(TAG, "AlertStopped onResponse()");

                                    if (200 <= httpCode && httpCode < 300) {
                                        // 成功
                                        if (DBG) android.util.Log.d(TAG, " - AlertStopped onResponse(), Success");
                                    } else {
                                        // 失敗
                                        if (DBG) android.util.Log.w(TAG, " - AlertStopped onResponse(), Error");
                                    }
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    if (DBG) android.util.Log.w(TAG, "AlertStopped onFailure(), e = " + e);
                                    mSendFailedItemList.add(event);
                                }

                                @Override
                                public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                    if (DBG) android.util.Log.w(TAG, "AlertStopped onParsedResponse(), itemList = " + itemList);
                                }
                            });
                        }
                    });
                    t.start();
                }

                final DeleteAlertSucceededItem deleteEvent = new DeleteAlertSucceededItem(tempAlertItem.token);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AlexaEventManager.sendEvent(TokenManager.getToken(), deleteEvent, mContext, new AlexaEventManager.AlexaCallback() {

                            @Override
                            public void onExecute(Call call) {
                                if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onExecute()");
                            }

                            @Override
                            public void onResponse(Call call, int httpCode) {
                                if (DBG) android.util.Log.d(TAG, "DeleteAlertSucceeded onResponse()");

                                if (200 <= httpCode && httpCode < 300) {
                                    // 成功
                                    deleteAlertItem(tempAlertItem.getAlertId());
                                    if (DBG) android.util.Log.d(TAG, " - DeleteAlertSucceeded onResponse(), Success");
                                }
                                else {
                                    // 失敗
                                    if (DBG) android.util.Log.w(TAG, " - DeleteAlertSucceeded onResponse(), Error");
                                }
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onFailure(), e = " + e);
                                mSendFailedItemList.add(deleteEvent);
                            }

                            @Override
                            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                if (DBG) android.util.Log.w(TAG, "DeleteAlertSucceeded onParsedResponse(), itemList = " + itemList);
                            }
                        });
                    }
                });
                t.start();
            } else {
                // TODO:DeleteAlertを受信した場合、AlertStopを送信した後に、DeleteAlertFailedを送信する
                final AlertStoppedItem event = new AlertStoppedItem(alertItem.token);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                            @Override
                            public void onExecute(Call call) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onExecute()");
                            }

                            @Override
                            public void onResponse(Call call, int httpCode) {
                                if (DBG) android.util.Log.d(TAG, "AlertStopped onResponse()");

                                if (200 <= httpCode && httpCode < 300) {
                                    // 成功
                                    if (DBG) android.util.Log.d(TAG, " - AlertStopped onResponse(), Success");
                                }
                                else {
                                    // 失敗
                                    if (DBG) android.util.Log.w(TAG, " - AlertStopped onResponse(), Error");
                                }
                                final DeleteAlertFailedItem event = new DeleteAlertFailedItem(tempAlertItem.token);
                                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                                    @Override
                                    public void onExecute(Call call) {
                                        if (DBG) android.util.Log.d(TAG, "DeleteAlertFailed onExecute()");
                                    }

                                    @Override
                                    public void onResponse(Call call, int httpCode) {
                                        if (DBG) android.util.Log.d(TAG, "DeleteAlertFailed onResponse()");

                                        if (200 <= httpCode && httpCode < 300) {
                                            // 成功
                                            if (DBG) android.util.Log.d(TAG, " - DeleteAlertFailed onResponse(), Success");
                                        }
                                        else {
                                            // 失敗
                                            if (DBG) android.util.Log.w(TAG, " - DeleteAlertFailed onResponse(), Error");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        if (DBG) android.util.Log.w(TAG, "DeleteAlertFailed onFailure(), e = " + e);
                                        mSendFailedItemList.add(event);
                                    }

                                    @Override
                                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                        if (DBG) android.util.Log.w(TAG, "DeleteAlertFailed onParsedResponse(), itemList = " + itemList);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onFailure(), e = " + e);
                                mSendFailedItemList.add(event);
                                final DeleteAlertFailedItem deleteEvent = new DeleteAlertFailedItem(tempAlertItem.token);
                                mSendFailedItemList.add(deleteEvent);
                            }

                            @Override
                            public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                                if (DBG) android.util.Log.w(TAG, "AlertStopped onParsedResponse(), itemList = " + itemList);
                            }
                        });
                    }
                });
                t.start();
            }

        }
        return result;
    }

    /**
     * WebLinkのAudioFocusロス
     */
    public void onWLAudioFocusLoss() {
        stopAlert(true);

        // 対話モデル下位の音楽再生機能へも伝える
        AlexaAudioManager.getInstance().onWLAudioFocusLoss();
    }

    /**
     * アラートの鳴動停止
     */
    public void stopAlert(boolean isLostAudioFocus) {
        if(DBG)android.util.Log.d(TAG, " - stopAlert()");
        if (isLostAudioFocus) {
            // AudioFocusをロス
            if (mWLAlertPlayer != null) {
                // アラート再生中の場合
                mWLAlertPlayer.stop();

                // 鳴動時間になっていたアラートの停止処理
                mCurrentAlertItem = null;
                if (mCurrentAlertRunnable != null) {
                    mCurrentAlertRunnable.cancel();
                }
                mCurrentAlertRunnable = null;
                for (SetAlertItem item : mActiveAlertList) {
                    mFocusLossAlertList.add(item);
                }
                mActiveAlertList.clear();
                mIAlertCallback.onAlertFinished();
                // Alertチャネルを非アクティブ
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.endAlertChannel();
                doAlertBackground();
            }
        }
        else if (mWLAlertPlayer != null) {
            mWLAlertPlayer.stop();
            doAlertBackground();
        }
        else if (mAlertPlayer != null) {
            mAlertPlayer.stop();
            doAlertBackground();
        }
    }

    /**
     * アラートを一時停止
     */
    public void pause() {
        if(DBG)android.util.Log.d(TAG, " - pauseAlert()");
        if (mWLAlertPlayer != null) {
            mWLAlertPlayer.pause();
        }
        else if (mAlertPlayer != null) {
            mAlertPlayer.pause();
        }
    }

    /**
     * Dialogチャネルが非アクティブになり、Alertチャネルがアクティブ
     */
    public void resume() {
        if(DBG)android.util.Log.d(TAG, " - resume()");
        if (mWLAlertPlayer != null) {
            mWLAlertPlayer.start();
        }
        else if (mAlertPlayer != null) {
            // アラート再生中にDialogチャネルがアクティブになっていた場合
            mAlertPlayer.start();
        }
        else if (mActiveAlertList.size() > 0) {
            // アクティブなアラートがある場合
            executeAlert();
        }

    }

    /**
     * アラートチャンネルがバックチャンネルに移行
     */
    public void onAlertChannelBackground() {
        if (DBG) android.util.Log.d(TAG, "onAlertChannelBackground()");
        doAlertBackground();
    }

    /**
     * 一時停止したアラートを再開
     */
    @Deprecated
    public void resumeAlert() {
        if(DBG)android.util.Log.d(TAG, " - resumeAlert()");
        if (mWLAlertPlayer != null) {
            mWLAlertPlayer.start();
            doAlertForeground();
        }
        else if (mAlertPlayer != null) {
            mAlertPlayer.start();
            doAlertForeground();
        }
    }

    /**
     * アラートチャンネルがフォアグラウンドへ移行
     */
    public void onAlertChannelForeground() {
        if (DBG) android.util.Log.d(TAG, "onAlertChannelForeground()");
        doAlertForeground();
    }

    /**
     * アラートがフォアグラウンドに移行
     */
    private void doAlertForeground() {
        // AlertEnteredForegroundイベントを送信する
        if (mActiveAlertList.size() > 0) {
            for (SetAlertItem item : mActiveAlertList) {
                final AlertEnteredForegroundItem event = new AlertEnteredForegroundItem(item.token);
                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredForeground onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredForeground onResponse()");

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - AlertEnteredForeground onResponse(), Success");
                        }
                        else {
                            // 失敗
                            if (DBG) android.util.Log.w(TAG, " - AlertEnteredForeground onResponse(), Error");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredForeground onFailure(), e = " + e);
                        mSendFailedItemList.add(event);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredForeground onParsedResponse(), itemList = " + itemList);
                    }
                });
            }
        }
    }

    /**
     * アラートがバックグラウンドに移行
     */
    private void doAlertBackground() {
        // AlertEnteredBackgroundイベントを送信する
        if (mActiveAlertList.size() > 0) {
            for (SetAlertItem item : mActiveAlertList) {
                final AlertEnteredBackgroundItem event = new AlertEnteredBackgroundItem(item.token);
                AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredBackground onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredBackground onResponse()");

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - AlertEnteredBackground onResponse(), Success");
                        }
                        else {
                            // 失敗
                            if (DBG) android.util.Log.w(TAG, " - AlertEnteredBackground onResponse(), Error");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredBackground onFailure(), e = " + e);
                        mSendFailedItemList.add(event);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredBackground onParsedResponse(), itemList = " + itemList);
                    }
                });
            }
        }
    }

    /**
     * アラート鳴動開始
     * @param alertItem
     */
    private void doAlertStart(SetAlertItem alertItem) {
        if(DBG)android.util.Log.d(TAG, "doAlertStart() 1 ");
        // すぐにアラートを鳴動
        doAlertStart(alertItem, 0);
    }

    /**
     * アラート鳴動
     * @param alertItem
     * @param delay 鳴動する時間を指定(ms)
     */
    private void doAlertStart(SetAlertItem alertItem, long delay) {
        if (DBG) android.util.Log.d(TAG, "doAlertStart() 2 ");
        if (DBG) android.util.Log.d(TAG, " - doAlertStart(), ", new Throwable());

        AlertActivateRunnable runnable = new AlertActivateRunnable(alertItem);
        mAlertMap.put(alertItem, runnable);
        mHandler.postDelayed(runnable, delay);
    }

    /**
     * アラート音源再生用プレーヤーの保持
     * @param player
     */
    private void setAlertPlayer(MediaPlayer player) {
        if (DBG) android.util.Log.d(TAG, "setAlertPlayer(), player = " + player);
        if (mAlertPlayer != null) {
            if (DBG) android.util.Log.d(TAG, " - setAlertPlayer(), Release player. " + mAlertPlayer);
            MediaPlayer tempPlayer = mAlertPlayer;
            mAlertPlayer = null;
            tempPlayer.release();
        }
        mAlertPlayer = player;
    }

    /**
     * アラート音源再生用プレーヤーの保持
     * @param player
     */
    private void setWLAlertPlayer(WLPlayer player) {
        if (DBG) android.util.Log.d(TAG, "setWLAlertPlayer(), player = " + player);
        if (mWLAlertPlayer != null) {
            if (DBG) android.util.Log.d(TAG, " - setWLAlertPlayer(), Release player. " + mWLAlertPlayer);
            WLPlayer tempPlayer = mWLAlertPlayer;
            mWLAlertPlayer = null;
            tempPlayer.release();
        }
        mWLAlertPlayer = player;
    }


    /**
     * アラートチャネルのバックグラウンド判定
     * @return
     */
    public boolean isBackgroundAlert() {
        if (DBG) android.util.Log.d(TAG, "isBackgroundAlert()");
        boolean isBackground = false;
        AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
        AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
        if (currentChannel == AlexaQueueManager.AlexaChannel.DialogChannel) {
            //  優先度高が実行中
            isBackground = true;
        }
        else {
            isBackground = false;
        }

        if (DBG) android.util.Log.d(TAG, " - isBackgroundAlert(), return " + isBackground);

        return isBackground;
    }

    /**
     * デフォルト音源のプレーヤー生成
     * @param type
     * @return
     */
    private MediaPlayer getDefaultAssetPlayer(String type) {
        if (DBG) android.util.Log.d(TAG, "getDefaultAssetPlayer(), type = " + type);
        MediaPlayer player = null;
        boolean isShortVersion = isBackgroundAlert();

        if (TextUtils.isEmpty(type)) {
            // Typeが無い
            if (DBG) android.util.Log.w(TAG, " - getDefaultAssetPlayer(), Type is none.");
        }
        else if (type.equals("TIMER")) {
            if (DBG) android.util.Log.d(TAG, " - getDefaultAssetPlayer(), Type is TIMER.");
            // タイマー
            if (isShortVersion) {
                player = MediaPlayer.create(mContext, R.raw.med_system_alerts_melodic_02_short);
            }
            else {
                player = MediaPlayer.create(mContext, R.raw.med_system_alerts_melodic_02);
            }
        }
        else if (type.equals("ALARM")) {
            if (DBG) android.util.Log.d(TAG, " - getDefaultAssetPlayer(), Type is ALARM.");
            // アラーム
            if (isShortVersion) {
                player = MediaPlayer.create(mContext, R.raw.med_system_alerts_melodic_01_short);
            }
            else {
                player = MediaPlayer.create(mContext, R.raw.med_system_alerts_melodic_01);
            }
        }
        else {
            // 不明な値
            if (DBG) android.util.Log.w(TAG, " - getDefaultAssetPlayer(), Type is Mismatch.");
        }

        return player;
    }

    /**
     * デフォルト音源のプレーヤー生成
     * @param type
     * @return
     */
    private WLPlayer getDefaultAssetWLPlayer(String type) {
        if (DBG) android.util.Log.d(TAG, "getDefaultAssetWLPlayer(), type = " + type);
        WLPlayer player = null;
        boolean isShortVersion = isBackgroundAlert();

        if (TextUtils.isEmpty(type)) {
            // Typeが無い
            if (DBG) android.util.Log.w(TAG, " - getDefaultAssetWLPlayer(), Type is none.");
        }
        else if (type.equals("TIMER")) {
            if (DBG) android.util.Log.d(TAG, " - getDefaultAssetWLPlayer(), Type is TIMER.");
            // タイマー
            if (isShortVersion) {
                player = WLPlayer.create(mContext, R.raw.med_system_alerts_melodic_02_short, true);
            }
            else {
                player = WLPlayer.create(mContext, R.raw.med_system_alerts_melodic_02, true);
            }
        }
        else if (type.equals("ALARM")) {
            if (DBG) android.util.Log.d(TAG, " - getDefaultAssetWLPlayer(), Type is ALARM.");
            // アラーム
            if (isShortVersion) {
                player = WLPlayer.create(mContext, R.raw.med_system_alerts_melodic_01_short, true);
            }
            else {
                player = WLPlayer.create(mContext, R.raw.med_system_alerts_melodic_01, true);
            }
        }
        else {
            // 不明な値
            if (DBG) android.util.Log.w(TAG, " - getDefaultAssetWLPlayer(), Type is Mismatch.");
        }

        return player;
    }
// -- sakamori 2017.12.27 start --------------------------------------
    /**
     * アラート鳴動時間の解析
     * @param scheduledTime
     * @return
     */
    private long parseScheduledTime(String scheduledTime) {
        if (DBG) android.util.Log.d(TAG, "parseScheduledTime(), scheduledTime = " + scheduledTime);
        long milliTime = 0;

        if (!TextUtils.isEmpty(scheduledTime)) {
            StringBuffer formatBuffer = new StringBuffer();
            formatBuffer.append("yyyy-MM-dd'T'HH:mm:ss");       // ISOローカル日付および時間のフォーマット
            // TODO:Amazon公式サンプルのjavaclientでは、yyyy-MM-dd'T'HH:mm:ssのフォーマット以外はサポートされていなかった。(2017.12.07時点)
            // タイムゾーン部分の有無を判定
            // TODO:最小限に省略された型で、時差部分が表示されるのが、index=13の箇所　例：「20111203T1015+09:00」→2011年12月03日 10時15分 時差9時間
            if (scheduledTime.length() > 13 && (scheduledTime.substring(13, scheduledTime.length()).contains("+") || scheduledTime.substring(13, scheduledTime.length()).contains("-"))) {
                // タイムゾーン指定がある場合
                formatBuffer.append("Z");
            }
            String dateFormat = formatBuffer.toString();

            if (DBG) android.util.Log.d(TAG, " - parseScheduledTime, dateFormat    = " + dateFormat);

            Date date = null;
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(dateFormat, Locale.ENGLISH);
            format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            try {
                date = format.parse(scheduledTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            if (date != null) {
                milliTime = date.getTime();
            }
        }
        if (DBG) android.util.Log.d(TAG, " - parseScheduledTime, return " + milliTime);

        return milliTime;
    }
// -- sakamori 2017.12.27 end ----------------------------------------

/* Alert Queue対応 */
    /**
     * Alertを鳴動
     */
    public void executeAlert() {
        if (DBG) android.util.Log.d(TAG, "executeAlert()");

        if (mIsAlertQueueBlockFlag) {
            // AlertQueueをブロック中
            return;
        }

        AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
        if (notificationManager.getAudioIndicatorQueueSize() > 0) {
            // Notificationが待機中、又は鳴動中
            if(DBG)android.util.Log.d(TAG, " - executeAlert(), Play AudioIndicator");
            notificationManager.playAudioIndicator();

            return;
        }

        if (mCurrentAlertItem != null) {
            // アラート鳴動中の場合
            // NOP
            if (DBG) android.util.Log.d(TAG, " - executeAlert(), Alert executed, AlertItem = " + mCurrentAlertItem);
        }
        else {
            if (mCurrentAlertRunnable != null) {
                // フェールセーフ
                mCurrentAlertRunnable.cancel();
                if (DBG) android.util.Log.w(TAG, " - executeAlert(), mCurrentAlertRunnable cancel.");
            }
            // 鳴動処理開始
            boolean isBackgroundAlert = isBackgroundAlert();
            if (isBackgroundAlert == true) {
                // アラートチャネルがバックグラウンド
                if (mActiveAlertList.size() == 1) {
                    // アクティブなアラートが１つのみの場合
                    postAlertRunnable(mActiveAlertList.get(0), isBackgroundAlert);
                }
                else {
                    // アクティブなアラートが２つ以上、又は、１つも無い場合
                    // TODO:ショートアラートを鳴動しない。
                }
            }
            else {
                // アラートチャネルがフォアグラウンド
                if (mActiveAlertList.size() > 0) {
                    postAlertRunnable(mActiveAlertList.get(0), isBackgroundAlert);
                }
            }
            if(DBG)android.util.Log.d(TAG, " - executeAlert(), execute, AlertItem = " + mCurrentAlertItem);
        }
    }

    /**
     * Alert鳴動処理開始
     * @param item
     * @param isBackground
     */
    private void postAlertRunnable(SetAlertItem item, boolean isBackground) {
        // 再生するAssetデータを準備
        mCurrentAlertItem = item;

        ArrayList<AlexaIfDirectiveItem.Asset> assetArrayList = new ArrayList<>();
        ArrayList<AlexaIfDirectiveItem.Asset> backAssetArrayList = new ArrayList<>();
        ArrayList<AlexaIfDirectiveItem.Asset> assets = new ArrayList<>();
        if (mCurrentAlertItem.assets != null && mCurrentAlertItem.assets.size() > 0) {
            assets.addAll(mCurrentAlertItem.assets);
        }
        ArrayList<String> assetOrder = new ArrayList<>();
        if (mCurrentAlertItem.assetPlayOrder != null && mCurrentAlertItem.assetPlayOrder.size() > 0) {
            assetOrder.addAll(mCurrentAlertItem.assetPlayOrder);
        }

        // fore sound
        for (String order : assetOrder) {
            for (AlexaIfDirectiveItem.Asset asset : assets) {
                if (order.equals(asset.assetId)) {
                    // AssetIDが一致
                    assetArrayList.add(asset);
                    break;
                }
            }
        }

        // back sound
        for (AlexaIfDirectiveItem.Asset asset : assets) {
            if (backAssetArrayList.size() == 0) {
                if (mCurrentAlertItem.backgroundAlertAsset.equals(asset.assetId)) {
                    backAssetArrayList.add(asset);
                    break; // one only
                }
            }
        }

        // -> 鳴動処理
        if (isBackground == false) {
            // フォアグラウンド
            mCurrentAlertRunnable = new AlertRingingRunnable(mCurrentAlertItem, assetArrayList, 0);
            mHandler.post(mCurrentAlertRunnable);
            if (mIAlertCallback != null) {
                mIAlertCallback.onAlertStarted();
            }
        }
        else {
            // バックグラウンド
            mCurrentAlertRunnable = new AlertBackgroundRingingRunnable(mCurrentAlertItem, backAssetArrayList);
            mHandler.post(mCurrentAlertRunnable);
            if (mIAlertCallback != null) {
                mIAlertCallback.onShortAlertStarted();
            }
        }
    }

    /**
     * 鳴動したAlertの完了コールバック
     */
    private void onCompletionAlert(SetAlertItem item) {
        if (DBG) android.util.Log.d(TAG, "onCompletionAlert(), item = " + item);
        if (DBG) android.util.Log.d(TAG, " - onCompletionAlert(), Alert token = " + item.token);
        if (mCurrentAlertItem == item) {
            // 鳴動処理完了 -> Stoppedイベントを送信
            if (mIAlertCallback != null) {
                mIAlertCallback.onAlertFinished();
            }

            if (mActiveAlertList.contains(item) == true) {
                mActiveAlertList.remove(item);
            }
            if (mAlertList.contains(item) == true) {
                mAlertList.remove(item);
            }
            mCurrentAlertItem = null;
            mCurrentAlertRunnable = null;

            AlexaNotificationManager notificationManager = AlexaNotificationManager.getInstance();
            if (notificationManager.getAudioIndicatorQueueSize() > 0) {
                // NotificationのAudioIndicatorが待機中
                if (DBG) android.util.Log.d(TAG, " - onCompletionAlert(), Play Notification");
                notificationManager.playAudioIndicator();
            }
            else if (mActiveAlertList.size() > 0) {
                // 次のAlertを再生
                if (DBG) android.util.Log.d(TAG, " - onCompletionAlert(), Next Alert");
                executeAlert();
            }
            else {
                // QueueにAlertが溜まっていない場合、一連の処理は終了
                if (DBG) android.util.Log.d(TAG, " - onCompletionAlert(), Active alert none.");
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.endAlertChannel();
                if (mAlertList.size() == 0) {
                    mIAlertCallback.onStopAlertAll();
                }
            }
        }
        else {
            if(DBG)android.util.Log.w(TAG, " - onCompletionAlert(), Alert is mismatch, Current = " + mCurrentAlertItem + ", Completion = " + item);
        }
    }

    /**
     * ショートアラートの鳴動終了処理
     * @param item
     */
    private void onCompletionShortAlert(SetAlertItem item) {
        if (DBG) android.util.Log.d(TAG, "onCompletionShortAlert()");
        if (mCurrentAlertItem == item) {
            // itemをAlertList、及び、ActiveAlertListから削除しない
            mCurrentAlertItem = null;
            mCurrentAlertRunnable = null;
            // Notificationの判定も行わない -> Dialogチャネルがアクティブなので先に鳴動しているはず
            if (isBackgroundAlert() == false) {
                // ショートアラート鳴動中にAlertチャネルがフォアグラウンドになっていた場合
                // TODO:動作の流れとして正しいかは要確認
                if (mActiveAlertList.size() > 0) {
                    // 次のAlertを再生
                    if (DBG) android.util.Log.d(TAG, " - onCompletionShortAlert(), Next Alert");
                    executeAlert();
                }
            }
        }
    }

    /*
    * Snoozeした際に、AlarmのTokenを比較
    * */
    public boolean compareAlarmToken(SetAlertItem item){
        boolean result = false;

        List<SetAlertItem> activeAlertList = getActiveAlertList();
        String activeAlertToken = null;
        String alertToken = item.token;
        if(activeAlertList.size() > 0) {
            //鳴動しているアラートを取得
            activeAlertToken = activeAlertList.get(0).token;
            //同じTokenだったら鳴動中のアラートを停止
            if (activeAlertToken.equals(alertToken)) {
                //onAlertStop();
                result = true;
            }
        }
        return result;
    }

    /**
     * AlertQueueの動作を一時停止／再開
     * @param isBlock
     */
    public void setAlertQueueBlockFlag(boolean isBlock) {
        this.mIsAlertQueueBlockFlag = isBlock;
    }

    /**
     * AlertQueue動作一時停止フラグの取得
     * @return
     */
    public boolean getAlertQueueBlockFlag() {
        return this.mIsAlertQueueBlockFlag;
    }

    /**
     * アラートを有効化するRunnable
     * -> アラートの鳴動時間が来た際にrun()が1回だけ実行される
     */
    private class AlertActivateRunnable implements Runnable {
        SetAlertItem mAlertItem = null;
        AlertActivateRunnable(SetAlertItem alertItem) {
            this.mAlertItem = alertItem;
        }

        @Override
        public void run() {
            if (DBG) android.util.Log.d(TAG, "AlertActivateRunnable#run()");

            if (mAlertMap.containsKey(mAlertItem)) {
                // ハッシュマップから削除
                mAlertMap.remove(mAlertItem);
            }

            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
            SetAlertItem alertItem = null;
            for (SetAlertItem item : mAlertList) {
//                    // トークンが一致するものがある
                if (this.mAlertItem == item) {
                    // TODO:インスタンス自体の比較に変更
                    alertItem = item;
                    break;
                }
            }
            if (alertItem == null) {
                // アラートリストに該当するアラートがない
                // -> DeleteAlertディレクティブでキャンセルされた場合など
                if (DBG) android.util.Log.d(TAG, " - AlertActivateRunnable#run() Alert is deleted. Token = " + this.mAlertItem.token);
                return;
            }
            mActiveAlertList.add(alertItem);
            if(DBG)android.util.Log.d(TAG, " - AlertActivateRunnable#run() first Alert");
            final AlertStartedItem event = new AlertStartedItem(this.mAlertItem.token);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "AlertStarted onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "AlertStarted onResponse()");

                            if (200 <= httpCode && httpCode < 300) {
                                // 成功
                                if (DBG) android.util.Log.d(TAG, " - AlertStarted onResponse(), Success");
                            }
                            else {
                                // 失敗
                                if (DBG) android.util.Log.w(TAG, " - AlertStarted onResponse(), Error");
                            }

                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG) android.util.Log.w(TAG, "AlertStarted onFailure(), e = " + e);
                            mSendFailedItemList.add(event);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                            if (DBG) android.util.Log.w(TAG, "AlertStarted onParsedResponse(), itemList = " + itemList);
                        }
                    });
                }
            });
            t.start();

            if (currentChannel == AlexaQueueManager.AlexaChannel.DialogChannel) {
                final AlertEnteredBackgroundItem alertEvent = new AlertEnteredBackgroundItem(alertItem.token);
                AlexaEventManager.sendEvent(alertItem.token, alertEvent, mContext, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredBackground onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredBackground onResponse()");

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - AlertEnteredBackground onResponse(), Success");
                        }
                        else {
                            // 失敗
                            if (DBG) android.util.Log.w(TAG, " - AlertEnteredBackground onResponse(), Error");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredBackground onFailure(), e = " + e);
                        mSendFailedItemList.add(alertEvent);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredBackground onParsedResponse(), itemList = " + itemList);
                    }
                });
            } else {
                final AlertEnteredForegroundItem alertEvent = new AlertEnteredForegroundItem(alertItem.token);
                AlexaEventManager.sendEvent(alertItem.token, alertEvent, mContext, new AlexaEventManager.AlexaCallback() {
                    @Override
                    public void onExecute(Call call) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredForeground onExecute()");
                    }

                    @Override
                    public void onResponse(Call call, int httpCode) {
                        if (DBG) android.util.Log.d(TAG, "AlertEnteredForeground onResponse()");

                        if (200 <= httpCode && httpCode < 300) {
                            // 成功
                            if (DBG) android.util.Log.d(TAG, " - AlertEnteredForeground onResponse(), Success");
                        }
                        else {
                            // 失敗
                            if (DBG) android.util.Log.w(TAG, " - AlertEnteredForeground onResponse(), Error");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredForeground onFailure(), e = " + e);
                        mSendFailedItemList.add(alertEvent);
                    }

                    @Override
                    public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {
                        if (DBG) android.util.Log.w(TAG, "AlertEnteredForeground onParsedResponse(), itemList = " + itemList);
                    }
                });
            }

            executeAlert();
        }
    }

    /**
     * Alert鳴動Runnableの共通化
     *  -> 通常アラートとショートアラートの両方でキャンセルメソッドを用意するため
     */
    private interface IAlertRingingRunnable extends Runnable {
        void cancel();
        void finish();
    }

    /**
     * アラートを鳴動させるRunnable.
     * -> run()はAssetList内の１つのAssetを処理する
     * -> AssetListを１周するとonFinishAlert()が実行される
     * -> onFinishAlert()で、ループ、又は、終了が判定される
     * -> ループを行う際は、同じAlertRingingRunnableが再利用される
     */
    private class AlertRingingRunnable implements IAlertRingingRunnable, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, WLPlayer.IOnCompletionListener, WLPlayer.IOnPreparedListener {

        SetAlertItem mAlertItem = null;
        ArrayList<AlexaIfDirectiveItem.Asset> mAssetList = null;
        boolean isFinishedAlert = false;
        int loopedCount = 0;
// AnyPlace:18-445-5-00024 Start
//        boolean mIsInfiniteLoop = false;
// AnyPlace:18-445-5-00024 End
        int mIndex = 0;

        long mAlertStartTimeMillis = 0L;

        AlertRingingRunnable(SetAlertItem alertItem, ArrayList<AlexaIfDirectiveItem.Asset> assetList, int loopedCount) {
            this.mAlertItem = alertItem;
            this.mAssetList = assetList;
            this.loopedCount = loopedCount;
// AnyPlace:18-445-5-00024 Start
//            this.mIsInfiniteLoop = false;
// AnyPlace:18-445-5-00024 End
            this.mIndex = 0;
        }

        @Override
        public void run() {
            if (DBG) android.util.Log.d(TAG, "AlertRingingRunnable#run()");
            if (mAlertStartTimeMillis <= 0) {
                mAlertStartTimeMillis = System.currentTimeMillis();
                if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#run(), mAlertStartTimeMillis = " + mAlertStartTimeMillis);
            }
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
                return;
            }
            if (mIsFinish) {
                // Managerを破棄済み
                return;
            }
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
            SetAlertItem alertItem = null;
            for (SetAlertItem item : mAlertList) {
                if (this.mAlertItem.token.equals(item.token) == true) {
                    // トークンが一致するものがある
                    alertItem = item;
                    break;
                }
            }
            if (alertItem == null) {
                // アラートリストに該当するアラートがない
                if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#run() Alert is deleted. Token = " + this.mAlertItem.token);
                isFinishedAlert = true;
                onFinishAlert();
                return;
            }

            // AndroidOSのAudioFocusを取得
            AmazonAlexaManager.getInstance().requestAudioFocus();

            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Foreground Alert");
            if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_ALERT)*/) {
                if (this.mAssetList != null && this.mAssetList.size() > mIndex) {
                    AlexaIfDirectiveItem.Asset asset = this.mAssetList.get(mIndex);
                    if (asset != null) {
                        // asset.urlの音を再生する
                        WLPlayer mediaPlayer = null;
                        if (asset.cache != null && asset.cache.length > 0) {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Use Cache");
                            // キャッシュ取得済み
                            mediaPlayer = new WLPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);

                            File tempMp3;
                            try {
                                tempMp3 = File.createTempFile("alert_asset_cache", ".mp3", mContext.getCacheDir());
                                FileOutputStream fos = new FileOutputStream(tempMp3);
                                fos.write(asset.cache);
                                fos.close();

                                FileInputStream fis = new FileInputStream(tempMp3);
                                mediaPlayer.setDataSource(fis.getFD(), true);
                                fis.close();

                                tempMp3.delete();

                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                        else if (TextUtils.isEmpty(asset.url) == true) {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Asset is NULL");
                            playDefaultAlert();
                        }
                        else {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Use URL");
                            // Assetはあるが、キャッシュ未取得
                            mediaPlayer = new WLPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);
                            try {
                                mediaPlayer.setDataSource(mContext, asset.getUrl(), true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                    }
                }
                else {
                    // Asset未設定（Assetフィールドが無い）
                    if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run() Asset = null or 0");
                    playDefaultAlertWL();
                }
            }
            else {
                if (this.mAssetList != null && this.mAssetList.size() > mIndex) {
                    AlexaIfDirectiveItem.Asset asset = this.mAssetList.get(mIndex);
                    if (asset != null) {
                        // asset.urlの音を再生する
                        MediaPlayer mediaPlayer = null;
                        if (asset.cache != null && asset.cache.length > 0) {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Use Cache");
                            // キャッシュ取得済み
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);

                            File tempMp3;
                            try {
                                tempMp3 = File.createTempFile("alert_asset_cache", ".mp3", mContext.getCacheDir());
                                FileOutputStream fos = new FileOutputStream(tempMp3);
                                fos.write(asset.cache);
                                fos.close();

                                FileInputStream fis = new FileInputStream(tempMp3);
                                mediaPlayer.setDataSource(fis.getFD());
                                mediaPlayer.prepareAsync();
                                fis.close();

                                tempMp3.delete();

                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                        else if (TextUtils.isEmpty(asset.url) == true) {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Asset is NULL");
                            playDefaultAlert();
                        }
                        else {
                            if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run(), Use URL");
                            // Assetはあるが、キャッシュ未取得
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);
                            try {
                                mediaPlayer.setDataSource(mContext, Uri.parse(asset.getUrl()));
                                mediaPlayer.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                    }
                }
                else {
                    // Asset未設定（Assetフィールドが無い）
                    if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#run() Asset = null or 0");
                    playDefaultAlert();
                }
            }
        }

        /**
         * Default音源再生処理
         */
        private void playDefaultAlert(){
            MediaPlayer mediaPlayer = getDefaultAssetPlayer(mAlertItem.type);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(this);
                if(isBackgroundAlert() == true){
                    mediaPlayer.pause();
                }
                else {
                    setAlertPlayer(mediaPlayer);
                    mediaPlayer.start();
                }
            }
            else {
                // typeがサポート外
                isFinishedAlert = true;
                onFinishAlert();
            }
        }

        /**
         * Default音源再生処理
         */
        private void playDefaultAlertWL(){
            WLPlayer mediaPlayer = getDefaultAssetWLPlayer(mAlertItem.type);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(this);
                if(isBackgroundAlert() == true){
                    mediaPlayer.pause();
                }
                else {
                    setWLAlertPlayer(mediaPlayer);
                    mediaPlayer.start();
                }
            }
            else {
                // typeがサポート外
                isFinishedAlert = true;
                onFinishAlert();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onPrepared()");
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
            //[Alert] アラートが一時停止されない事がある [AnyPlace:18-445-1-00043]
            if(isBackgroundAlert() == true){
                mp.pause();
            }else {
                // 再生開始
                setAlertPlayer(mp);
                mp.start();
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onCompletion()");
            // 再生終了
            setAlertPlayer(null);
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
                return;
            }
            if (mIsFinish) {
                // Managerを破棄済み
                return;
            }

            this.mIndex = this.mIndex + 1;    // 次のAssetへ
// AnyPlace:18-445-5-00024 Start
//            if (mIsInfiniteLoop == true) {
//                // Assetが無いアラートの場合、ユーザーに停止されるまで無限にループする
//                if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#onCompletion() InfiniteLoop = true.");
//                onFinishAlert();
//            }
//            else if (this.mAssetList != null && this.mAssetList.size() > this.mIndex) {
            if (this.mAssetList != null && this.mAssetList.size() > this.mIndex) {
// AnyPlace:18-445-5-00024 End
                mHandler.postDelayed(this, 0);
            }
            else {
                // AssetListを１周、又は、Assetが無い場合
                onFinishAlert();
            }
        }

        @Override
        public void onPrepared(WLPlayer player) {
            AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
            AlexaQueueManager.AlexaChannel currentChannel = queueManager.getForegroundChannel();
            //[Alert] アラートが一時停止されない事がある [AnyPlace:18-445-1-00043]
            if(isBackgroundAlert() == true){
                player.pause();
            }else {
                // 再生開始
                setWLAlertPlayer(player);
                player.start();
            }
        }

        @Override
        public void onCompletion(WLPlayer player) {
            // 再生終了
            setWLAlertPlayer(null);
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
                return;
            }
            if (mIsFinish) {
                // Managerを破棄済み
                return;
            }

            this.mIndex = this.mIndex + 1;    // 次のAssetへ
            if (this.mAssetList != null && this.mAssetList.size() > this.mIndex) {
                mHandler.postDelayed(this, 0);
            }
            else {
                // AssetListを１周、又は、Assetが無い場合
                onFinishAlert();
            }
        }

        /**
         * アラートの停止(DB削除なし)
         */
        @Override
        public void cancel() {
            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onCancel()");
            // 再生終了
            isFinishedAlert = true;
            setAlertPlayer(null);
            onFinishAlert();
        }

        /**
         * アラートの終了(DB削除あり)
         */
        @Override
        public void finish() {
            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#finish()");

            // 再生終了
            isFinishedAlert = true;
            setAlertPlayer(null);
            onFinishAlert();

            sendAlertStoppedEvent();
        }


        private void onFinishAlert() {
            if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert()");
            if (isFinishedAlert == true) {
                // このアラートは終了（キャンセル含む）
                onCompletionAlert(mAlertItem);
                return;
            }
            loopedCount = loopedCount + 1;
            if (mAlertItem.loopCount == null) {
                // 1時間の間ループ、又は、ユーザーが止めるまでループ(Alexa仕様)
                if(DBG)android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert() loopCount is null.");
                if (mAlertStartTimeMillis + (60 * 60 * 1000) < System.currentTimeMillis()) {
                    // 1時間経過
                    if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert() finished loop since one hour passed.");
                    finish();
                }
                else {
                    if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert() looping. loopCount is NONE.");
                    this.mIndex = 0; // AssetのIndexを初期化
                    mHandler.postDelayed(this, 0);
                }
            }
            else if (mAlertItem.loopCount.longValue() <= loopedCount) {
                // 規定回数ループした
                if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert() finished loop");
                finish();
            }
            else {
                // もう一度ループ
                if (DBG) android.util.Log.d(TAG, " - AlertRingingRunnable#onFinishAlert() loop : " + loopedCount);
                long delay = 0;
                if (mAlertItem.loopPauseInMilliseconds != null) {
                    delay = mAlertItem.loopPauseInMilliseconds.longValue();
                }

                mIndex = 0; // AssetのIndexを初期化
                mHandler.postDelayed(this, delay);
            }
        }


        private void sendAlertStoppedEvent(){
            final SetAlertItem item = mAlertItem;
            updateAlertItem(item.getAlertId(), 1);
            final AlertStoppedItem event = new AlertStoppedItem(item.token);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    AlexaEventManager.sendEvent(TokenManager.getToken(), event, mContext, new AlexaEventManager.AlexaCallback() {

                        @Override
                        public void onExecute(Call call) {
                            if (DBG) android.util.Log.d(TAG, "sendAlertStoppedEvent onExecute()");
                        }

                        @Override
                        public void onResponse(Call call, int httpCode) {
                            if (DBG) android.util.Log.d(TAG, "sendAlertStoppedEvent onResponse()");
                            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();

                            if (200 <= httpCode && httpCode < 300) {
                                if (DBG) android.util.Log.w(TAG, " - sendAlertStoppedEvent onResponse(), Success");
                                alertManager.deleteAlertItem(item.token);
                            } else {
                                if (DBG) android.util.Log.w(TAG, " - sendAlertStoppedEvent onResponse(), Error");
                            }
                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (DBG)
                                android.util.Log.w(TAG, "onCompletionAlert AlertStopped onFailure(), e = " + e);
                            mSendFailedItemList.add(event);
                        }

                        @Override
                        public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {

                        }
                    });
                }
            });
            t.start();
        }
    }

    /**
     * アラートチャネルがバックグラウンドの場合にアラートを鳴動させるRunnable.
     * -> run()はAssetList内の１つのAssetを処理する
     * -> AssetListを１周するとonFinishAlert()が実行される
     * -> onFinishAlert()で、ループ、又は、終了が判定される
     * -> ループを行う際は、同じAlertRingingRunnableが再利用される
     */
    private class AlertBackgroundRingingRunnable implements IAlertRingingRunnable, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, WLPlayer.IOnPreparedListener, WLPlayer.IOnCompletionListener {

        SetAlertItem mAlertItem = null;
        ArrayList<AlexaIfDirectiveItem.Asset> mAssetList = null;
        boolean isFinishedAlert = false;

        AlertBackgroundRingingRunnable(SetAlertItem alertItem, ArrayList<AlexaIfDirectiveItem.Asset> assetList) {
            this.mAlertItem = alertItem;
            this.mAssetList = assetList;
        }

        @Override
        public void run() {
            if(DBG)android.util.Log.d(TAG, "AlertBackgroundRingingRunnable#run()");
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
                return;
            }
            SetAlertItem alertItem = null;
            for (SetAlertItem item : mAlertList) {
                if (this.mAlertItem.token.equals(item.token) == true) {
                    // トークンが一致するものがある
                    alertItem = item;
                    break;
                }
            }
            if (alertItem == null) {
                // アラートリストに該当するアラートがない
                if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run() Alert is deleted. Token = " + this.mAlertItem.token);
                isFinishedAlert = true;
                onFinishAlert();
                return;
            }

            // AndroidOSのAudioFocusを取得
            AmazonAlexaManager.getInstance().requestAudioFocus();

            if (false/*DebugManager.isBoolean(DebugManager.DebugSettingId.DEBUG_SETTING_ID_WEBLINK_ALERT)*/) {
                // WebLinkAudio
                if (this.mAssetList != null && this.mAssetList.size() > 0) {
                    // backgroundAlertAssetで指定されるのは、1つのみなので、index=0のみを参照
                    AlexaIfDirectiveItem.Asset asset = this.mAssetList.get(0);

                    if (asset != null) {
                        // asset.urlの音を再生する
                        WLPlayer mediaPlayer = null;
                        if (asset.cache != null && asset.cache.length > 0) {
                            if (DBG) android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Use Cache");
                            // キャッシュ取得済み
                            mediaPlayer = new WLPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);

                            File tempMp3;
                            try {
                                tempMp3 = File.createTempFile("alert_asset_cache", ".mp3", mContext.getCacheDir());
                                FileOutputStream fos = new FileOutputStream(tempMp3);
                                fos.write(asset.cache);
                                fos.close();

                                FileInputStream fis = new FileInputStream(tempMp3);
                                mediaPlayer.setDataSource(fis.getFD(), true);
                                fis.close();

                                tempMp3.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                        else if (TextUtils.isEmpty(asset.url) == true) {
                            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Asset is None");
                            // 再生する音声が無い
                            mediaPlayer = getDefaultAssetWLPlayer(mAlertItem.type);
                            if (mediaPlayer != null) {
                                mediaPlayer.setOnCompletionListener(this);
                                setWLAlertPlayer(mediaPlayer);
                                mediaPlayer.start();
                            }
                            else {
                                if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), DefaultAssetPlayer NULL");
                                // typeがサポート外
                                isFinishedAlert = true;
                                onFinishAlert();            // TODO:とりあえず終了させる
                            }
                        }
                        else {
                            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Use URL");
                            mediaPlayer = new WLPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);
                            try {
                                mediaPlayer.setDataSource(mContext, asset.getUrl(), true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                    }
                }
                else {
                    // assetが無い
                    if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run() Asset = null or 0");
                    WLPlayer mediaPlayer = getDefaultAssetWLPlayer(mAlertItem.type);
                    if (mediaPlayer != null) {
                        mediaPlayer.setOnCompletionListener(this);
                        setWLAlertPlayer(mediaPlayer);
                        mediaPlayer.start();
                    }
                    else {
                        // typeがサポート外
                        isFinishedAlert = true;
                        onFinishAlert();            // TODO:とりあえず終了させる
                    }
                }
            }
            else {
                if (this.mAssetList != null && this.mAssetList.size() > 0) {
                    // backgroundAlertAssetで指定されるのは、1つのみなので、index=0のみを参照
                    AlexaIfDirectiveItem.Asset asset = this.mAssetList.get(0);

                    if (asset != null) {
                        // asset.urlの音を再生する
                        MediaPlayer mediaPlayer = null;
                        if (asset.cache != null && asset.cache.length > 0) {
                            if (DBG) android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Use Cache");
                            // キャッシュ取得済み
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);

                            File tempMp3;
                            try {
                                tempMp3 = File.createTempFile("alert_asset_cache", ".mp3", mContext.getCacheDir());
//                            tempMp3.deleteOnExit();
                                FileOutputStream fos = new FileOutputStream(tempMp3);
                                fos.write(asset.cache);
                                fos.close();

                                FileInputStream fis = new FileInputStream(tempMp3);
                                mediaPlayer.setDataSource(fis.getFD());
                                mediaPlayer.prepareAsync();
                                fis.close();

                                tempMp3.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                        else if (TextUtils.isEmpty(asset.url) == true) {
                            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Asset is None");
                            // 再生する音声が無い
                            mediaPlayer = getDefaultAssetPlayer(mAlertItem.type);
                            if (mediaPlayer != null) {
                                mediaPlayer.setOnCompletionListener(this);
                                setAlertPlayer(mediaPlayer);
                                mediaPlayer.start();
                            }
                            else {
                                if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), DefaultAssetPlayer NULL");
                                // typeがサポート外
                                isFinishedAlert = true;
                                onFinishAlert();            // TODO:とりあえず終了させる
                            }
                        }
                        else {
                            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run(), Use URL");
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setOnCompletionListener(this);
                            mediaPlayer.setOnPreparedListener(this);
                            try {
                                mediaPlayer.setDataSource(mContext, Uri.parse(asset.getUrl()));
                                mediaPlayer.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                                isFinishedAlert = true;
                                onFinishAlert();
                            }
                        }
                    }
                }
                else {
                    // assetが無い
                    if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#run() Asset = null or 0");
                    MediaPlayer mediaPlayer = getDefaultAssetPlayer(mAlertItem.type);
                    if (mediaPlayer != null) {
                        mediaPlayer.setOnCompletionListener(this);
                        setAlertPlayer(mediaPlayer);
                        mediaPlayer.start();
                    }
                    else {
                        // typeがサポート外
                        isFinishedAlert = true;
                        onFinishAlert();            // TODO:とりあえず終了させる
                    }
                }
            }

        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#onPrepared()");
            // 再生開始
            setAlertPlayer(mp);
            mp.start();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#onCompletion()");
            // 再生終了
            setAlertPlayer(null);
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
            }
            else {
                // 鳴動終了
                onFinishAlert();
            }
        }

        @Override
        public void onPrepared(WLPlayer player) {
            // 再生開始
            setWLAlertPlayer(player);
            player.start();
        }

        @Override
        public void onCompletion(WLPlayer player) {
            // 再生終了
            setWLAlertPlayer(null);
            if (isFinishedAlert == true) {
                // キャンセル等で終了済み
            }
            else {
                // 鳴動終了
                onFinishAlert();
            }
        }

        /**
         * キャンセル
         */
        @Override
        public void cancel() {
            if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#onCancel()");
            // 再生終了
            isFinishedAlert = true;
            setAlertPlayer(null);
            setWLAlertPlayer(null);
            onFinishAlert();
        }

        @Override
        public void finish() {
            // キャンセルと同じ
            cancel();
        }

        /**
         * 鳴動の終了
         */
        private void onFinishAlert() {
           if(DBG)android.util.Log.d(TAG, " - AlertBackgroundRingingRunnable#onFinishAlert()");
            // ショートアラートはループしない
            onCompletionShortAlert(mAlertItem);
        }

    }

    private static class RetrySendEventRunnable implements Runnable{
        private AlexaIfEventItem mAlexaIfItem = null;

        private Callback mCallback = null;
        private Context mContext = null;

        public interface Callback {
            void onSuccess(AlexaIfEventItem alexaIfItem);
            void onFailure(AlexaIfEventItem alexaIfItem);
        }

        public RetrySendEventRunnable(Context context,AlexaIfEventItem alexaIfItem,Callback callback){
            this.mContext = context;
            this.mAlexaIfItem = alexaIfItem;
            this.mCallback = callback;
        }

        @Override
        public void run() {
            boolean delete = false;
            if (mAlexaIfItem instanceof AlertStoppedItem) {
                delete = true;
            } else if (mAlexaIfItem instanceof DeleteAlertSucceededItem) {
                delete = true;
            }
            final boolean isDeleteDB = delete;

            AlexaEventManager.sendEvent(TokenManager.getToken(), mAlexaIfItem, mContext, new AlexaEventManager.AlexaCallback() {

                @Override
                public void onExecute(Call call) {
                    if (DBG) android.util.Log.d(TAG, mAlexaIfItem.getName() + " onExecute()");
                }

                @Override
                public void onResponse(Call call, int httpCode) {
                    if (DBG) android.util.Log.d(TAG, mAlexaIfItem.getName() + " onResponse()");
                    if (200 <= httpCode && httpCode < 300) {
                        if (isDeleteDB) {
                            AlexaAlertManager alertManager = AlexaAlertManager.getInstance();
                            if (mAlexaIfItem instanceof AlertStoppedItem) {
                                alertManager.deleteAlertItem(((AlertStoppedItem) mAlexaIfItem).token);
                            }
                            if (mAlexaIfItem instanceof DeleteAlertSucceededItem) {
                                if (alertManager != null) {
                                    alertManager.deleteAlertItem(((DeleteAlertSucceededItem) mAlexaIfItem).token);
                                }
                            }
                        }
                        if (mCallback != null) {
                            mCallback.onSuccess(mAlexaIfItem);
                        }
                    } else {
                        if (mCallback != null) {
                            mCallback.onFailure(mAlexaIfItem);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    if (DBG) android.util.Log.w(TAG, mAlexaIfItem.getName() + " onFailure(), e = " + e);
                    if (mCallback != null) {
                        mCallback.onFailure(mAlexaIfItem);
                    }
                }

                @Override
                public void onParsedResponse(ArrayList<AlexaIfDirectiveItem> itemList) {

                }
            });
        }
    }
}
