package jp.pioneer.carsync.presentation.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.VisibleForTesting;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;
import timber.log.Timber;

public class TipsImageTask implements Runnable {

    private Callback mCallback;
    private ArrayList<TipsItem> mTipsItems;

    @Inject
    public TipsImageTask(){

    }

    /**
     * コンストラクタ.
     *
     * @param callback コールバック
     */
    public TipsImageTask setParams(ArrayList<TipsItem> tipsItems,  Callback callback) {
        mCallback = callback;
        mTipsItems = tipsItems;
        return this;
    }

    @Override
    public void run() {
        Bitmap bitmap;
        try {
            for (TipsItem item : mTipsItems) {
                // サムネイル画像取得
                if (item.thumbUrl.length() > 0) {
                    bitmap = get(item.thumbUrl);
                    if(bitmap != null){
                        item.thumbImage = bitmap;
                        mCallback.onSuccess();
                    }
                }

                // アイコン画像取得
                for (TipsTag tag : item.tags) {
                    if (tag.iconUrl.length() > 0) {
                        bitmap = get(tag.iconUrl);
                        if(bitmap != null){
                            tag.iconImage = bitmap;
                            mCallback.onSuccess();
                        }
                    }
                }
            }
        } catch (Exception ex){
            Timber.d("run() Interrupted.");
        }
    }

    Bitmap get(String url){
        try {
            HttpURLConnection connection = createHttpUrlConnection(url);
            connection.setRequestMethod("GET");
            int response = connection.getResponseCode();

            // レスポンスコードを判定
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream stream = null;
                try {
                    StringBuilder result = new StringBuilder();
                    stream = connection.getInputStream();
                    return createBitmap(stream);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @VisibleForTesting
    public HttpURLConnection createHttpUrlConnection(String url) throws Exception {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    @VisibleForTesting
    public Bitmap createBitmap(InputStream stream) {
        return BitmapFactory.decodeStream(stream);
    }

    /**
     * コールバック.
     */
    public interface Callback {
        /**
         * 成功.
         */
        void onSuccess();

        /**
         * 失敗.
         */
        void onError();
    }
}
