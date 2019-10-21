package jp.pioneer.carsync.presentation.task;

import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.model.TipsItem;
import timber.log.Timber;

public class TipsListTask implements Runnable {
    private String mUrl;
    private Callback mCallback;

    @Inject
    public TipsListTask(){

    }

    public TipsListTask setParams(String url, Callback callback) {
        mUrl = url;
        mCallback = callback;
        return this;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection connection = createHttpUrlConnection(mUrl);
            connection.setRequestMethod("GET");
            int response = connection.getResponseCode();

            // レスポンスコードを判定
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream stream = null;
                BufferedReader reader = null;
                try {
                    stream = connection.getInputStream();
                    reader = createBufferedReader(stream);
                    TipsItem[] items = buildTipsItems(reader);
                    if(items.length>0) {
                        // ID降順でソート
                        Arrays.sort(items, (obj1, obj2) -> obj2.id - obj1.id);
                        mCallback.onSuccess(items);
                    }
                } finally {
                    if (stream != null) {
                        stream.close();
                    }

                    if (reader != null) {
                        reader.close();
                    }
                }
            } else {
                Timber.w("GetTipsListTask Response NG.");
                mCallback.onError();
            }
        } catch (Exception e) {
            Timber.w(e);
            mCallback.onError();
        }
    }

    @VisibleForTesting
    public HttpURLConnection createHttpUrlConnection(String url) throws Exception {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    @VisibleForTesting
    public BufferedReader createBufferedReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

    @VisibleForTesting
    public TipsItem[] buildTipsItems(BufferedReader reader) throws Exception {
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return new Gson().fromJson(result.toString(), TipsItem[].class);
    }

    /**
     * コールバック.
     */
    public interface Callback {
        /**
         * 成功.
         *
         * @param result 取得結果
         */
        void onSuccess(TipsItem[] result);

        /**
         * 失敗.
         */
        void onError();
    }
}
