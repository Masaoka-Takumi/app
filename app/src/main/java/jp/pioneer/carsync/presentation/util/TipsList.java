package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.presentation.event.UpdateTipsItemEvent;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;
import jp.pioneer.carsync.presentation.task.TipsImageTask;
import jp.pioneer.carsync.presentation.task.TipsListTask;
import timber.log.Timber;

/**
 * TIPS記事リスト.
 * <p>
 * サムネイルとアイコン画像は非同期に取得するため取得次第更新する。
 * 更新に成功した場合は{@link UpdateTipsItemEvent}が発行される。
 */
public class TipsList {
    /**
     * TIPS記事項目群.
     * <p>
     * 通信エラー等により取得できなかった場合は空配列となる。
     * 正常に取得した結果、又は初期状態による空であることと、
     * 取得エラーによる空かどうかを区別したい場合は、
     * {@link #isError}を合わせて参照する。
     */
    public ArrayList<TipsItem> items = new ArrayList<>();

    /**
     * 取得エラーか否か.
     * <p>
     * TIPS記事が取得できなかった場合に{@code true}となる。
     * {@link #update()}を呼び出した時点で一度{@code false}となり、
     * 取得できなかった場合{@code true}になる。
     * TIPS記事項目群が対象であり、
     * サムネイルとアイコンの画像の取得可否は対象外である。
     */
    public boolean isError;

    /**
     * 更新の必要があるか否か.
     */
    public boolean isUpdate = true;

    private static final String TIPS_LIST = "list.json";
    @Inject AppSharedPreference mPreference;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject TipsImageTask mTipsImageTask;
    @Inject TipsListTask mTipsListTask;
    private ExecutorService mTaskExecutor = Executors.newSingleThreadExecutor();
    private Future<?> mTaskFuture;

    /** TIPS記事取得タスク コールバック. */
    private TipsListTask.Callback mTipsListCallback = new TipsListTask.Callback() {
        @Override
        public void onSuccess(TipsItem[] result) {
            items.addAll(Arrays.asList((TipsItem[]) result));
            mEventBus.post(new UpdateTipsItemEvent());
            getImages();
        }

        @Override
        public void onError() {
            Timber.d("Failed get tips list.");
            isError = true;
            mEventBus.post(new UpdateTipsItemEvent());
        }
    };

    /** TIPS記事画像取得タスク コールバック. */
    private TipsImageTask.Callback mTipsImageCallback = new TipsImageTask.Callback() {
        @Override
        public void onSuccess() {
            mEventBus.post(new UpdateTipsItemEvent());
        }

        @Override
        public void onError() {
            Timber.d("Failed get image.");
        }
    };

    /**
     * コンストラクタ.
     */
    @Inject
    public TipsList() {

    }

    /**
     * 初期化
     */
    public void initialize(){
        if(!mEventBus.isRegistered(this)){
            mEventBus.register(this);
        }
    }

    /**
     * CrpSessionStartedEventハンドラ.
     *
     * @param ev CrpSessionStartedEvent
     */
    @Subscribe
    public void onCrpSessionStartedEvent(CrpSessionStartedEvent ev){
        cancel();
        isUpdate = true;
    }

    /**
     * TIPS記事更新処理.
     */
    public void update() {
        isError = false;
        cancel();

        mTaskFuture = mTaskExecutor.submit(mTipsListTask.setParams(createTipsListUrl(),mTipsListCallback));
    }

    /**
     * キャンセル処理.
     */
    public void cancel(){
        cancelTask();
        items.clear();
    }

    /**
     * サムネイル画像及びアイコン画像取得.
     */
    private void getImages() {
        mTaskFuture = mTaskExecutor.submit(mTipsImageTask.setParams(items,mTipsImageCallback));
    }

    private String createTipsListUrl() {
        String baseUrl = mPreference.getTipsListEndpoint().endpoint;
        String urlLangCode = mContext.getString(R.string.url_003);
        String model = mPreference.getLastConnectedCarDeviceModel();

        if (model.length() > 0) {
            return baseUrl + urlLangCode + "/" + model.toLowerCase() + "/" + TIPS_LIST;
        } else {
            return baseUrl + urlLangCode + "/" + TIPS_LIST;
        }
    }

    private void cancelTask() {
        if(isRunningTask()){
            mTaskFuture.cancel(true);
        }
    }

    private boolean isRunningTask() {
        return (mTaskFuture != null && !mTaskFuture.isDone());
    }
}
