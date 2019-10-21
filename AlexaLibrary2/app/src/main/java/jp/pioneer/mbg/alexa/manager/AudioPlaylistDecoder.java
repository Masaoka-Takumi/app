package jp.pioneer.mbg.alexa.manager;

/**
 * Created by esft-sakamori on 2017/09/13.
 */

import android.webkit.URLUtil;

import java.io.IOException;
import java.util.ArrayList;

import jp.pioneer.mbg.alexa.connection.OkHttpClientUtil;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Alexaの音楽再生機能がサポートするプレイリストを解析するクラス
 * 参考URL：
 * 　https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/recommended-media-support-for-avs
 * 　http://takuya-1st.hatenablog.jp/entry/2017/03/30/023120
 */
public class AudioPlaylistDecoder {
    private final static String TAG = AudioPlaylistDecoder.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * urlのプレイリストの音楽一覧を取得する
     * ※非同期処理必須
     * @param url
     * @return
     */
    public static ArrayList<String> decodePlaylist(String url) {
        if (DBG) android.util.Log .d(TAG, "decodePlaylist() : " + url);
        if (DBG) android.util.Log .d(TAG, " - decodePlaylist() : ", new Throwable());
        ArrayList<String> playlist = new ArrayList<>();
        if (URLUtil.isValidUrl(url) == true) {
            Response response = null;
            try {
                response = AudioPlaylistDecoder.connection(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response != null && response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (DBG) android.util.Log .d(TAG, "decodePlaylist() MediaType = " + mediaType);
                    //Revision: 2377 [18-278-1-00064]「99Dream_Android」NHKニュースから日経ニュースへ切り替わると、アプリはクラッシュしてしまう。
                    //jp.pioneer.mbg.alexa.manager.AudioPlaylistDecoder.decodePlaylist(AudioPlaylistDecoder.java:60)のjava.lang.String okhttp3.MediaType.type()メソッドでnullアクセスで落ちている
					if(mediaType==null)return playlist;
					StringBuffer contentType = new StringBuffer().append(mediaType.type()).append("/").append(mediaType.subtype());
                    switch (contentType.toString().toLowerCase()) {
                        case "audio/x-mpegurl" : {              // 参考サイトでは"application/x-mpegURL"となっていたが、audioの間違い？
                            // m3u/m3u8形式 or HLS形式
                            // TODO:HLS形式は、m3u/m3u8形式の中身にtsファイルを入れたものらしい
                            BufferedSource bufferedSource = response.body().source();
                            try {
                                while (!bufferedSource.exhausted()) {
                                    String line = bufferedSource.readUtf8Line();
                                    if (DBG) android.util.Log .d(TAG, "decodePlaylist() While : exhausted LINE = " + line);
                                    if (URLUtil.isValidUrl(line)) {
                                        playlist.add(line);
                                    }
                                }
                                bufferedSource.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (DBG) android.util.Log .d(TAG, "decodePlaylist(), [m3u/m3u8 or HLS] PlayList size = " + playlist.size());
                            break;
                        }
                        case "audio/x-scpls" : {
                            // pls形式
                            BufferedSource bufferedSource = response.body().source();
                            try {
                                while (!bufferedSource.exhausted()) {
                                    String line = bufferedSource.readUtf8Line();
                                    if (DBG) android.util.Log .d(TAG, "decodePlaylist() While : exhausted LINE = " + line);
                                    if (line.indexOf("File", 0) == 0) {
                                        // lineの先頭に"File"がある場合
                                        String musicUrl = line.substring(line.indexOf("=", 0) + "=".length(), line.length());
                                        if (DBG) android.util.Log .d(TAG, "decodePlaylist() musicUrl = " + musicUrl);
                                        if (URLUtil.isValidUrl(musicUrl)) {
                                            if (DBG) android.util.Log .d(TAG, "decodePlaylist() Playlist add musicUrl.");
                                            playlist.add(musicUrl);
                                        }
                                    }
                                }
                                bufferedSource.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (DBG) android.util.Log .d(TAG, "decodePlaylist(), [pls] PlayList size = " + playlist.size());
                            break;
                        }
                        case "application/vnd.apple.mpegurl" : {
                            // Apple仕様プレイリスト
                            // プレイリストだが、AndroidのMediaPlayerで再生できるので素通りさせる
                            break;
                        }
                        case "application/pls+xml" : {
                            // 「NPR Program Stream」で登場したプレイリスト
                            BufferedSource bufferedSource = response.body().source();
                            int numberOfEntries = -1;
                            try {
                                while (!bufferedSource.exhausted()) {
                                    String line = bufferedSource.readUtf8Line();
                                    if (DBG) android.util.Log .d(TAG, "decodePlaylist() While : exhausted LINE = " + line);
                                    if (line.indexOf("NumberOfEntries", 0) == 0) {
                                        // エントリー数
                                        String tempString = line.substring(line.indexOf("=", 0) + "=".length(), line.length());
                                        try {
                                            numberOfEntries = Integer.parseInt(tempString);
                                        }
                                        catch (NumberFormatException e) {
                                            e.printStackTrace();
                                            numberOfEntries = 0;
                                        }
                                        if (DBG) android.util.Log .d(TAG, "decodePlaylist() NumberOfEntries = " + numberOfEntries);
                                    }
                                    else if (line.indexOf("File", 0) == 0) {
                                        // lineの先頭に"File"がある場合
                                        String musicUrl = line.substring(line.indexOf("=", 0) + "=".length(), line.length());
                                        if (DBG) android.util.Log .d(TAG, "decodePlaylist() musicUrl = " + musicUrl);
                                        if (URLUtil.isValidUrl(musicUrl)) {
                                            if (DBG) android.util.Log .d(TAG, "decodePlaylist() Playlist add musicUrl.");
                                            playlist.add(musicUrl);
                                        }
                                    }
                                    else if (line.indexOf("Title", 0) == 0) {
                                        String title = line.substring(line.indexOf("=", 0) + "=".length(), line.length());
                                        if (DBG) android.util.Log .d(TAG, "decodePlaylist() title = " + title);
                                    }
                                }
                                bufferedSource.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (DBG) android.util.Log .d(TAG, "decodePlaylist(), [pls] PlayList size = " + playlist.size());
                            break;
                        }
                        default : {
                            // サポート外 or 音楽ファイル
                            break;
                        }
                    }
                    body.close();
                }
                response.close();
            }
            else {
                if (DBG) android.util.Log .d(TAG, "decodePlaylist(), connection failure, response = " + response);
                if(response != null && response.body() != null) {
                    ResponseBody body = response.body();
                    //ついか
                    body.close();
                    response.close();
                }
            }
        }
        return playlist;
    }

    /**
     * URLにHTTP接続
     * @param url
     * @return
     * @throws IOException
     */
    private synchronized static Response connection(String url) throws IOException {
        if (DBG) android.util.Log .d(TAG, "connection() : " + url);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = null;
        try {
            OkHttpClient client = OkHttpClientUtil.getNormalConnectionOkHttpClient();
            call = client.newCall(request);

            response = call.execute();

            ResponseBody body = response.body();
            if(response.isSuccessful()){
                return response;
            }
            else{
                body.close();
                response.close();
                return null;
            }
        }catch (IOException e){
            return null;
        }finally {
            if(call != null){
                call.cancel();
            }
        }
    }
}
