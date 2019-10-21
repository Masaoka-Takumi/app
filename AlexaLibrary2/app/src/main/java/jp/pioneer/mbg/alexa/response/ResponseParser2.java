package jp.pioneer.mbg.alexa.response;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.AudioPlayer.PlayItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.SpeechSynthesizer.SpeakItem;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import jp.pioneer.mbg.alexa.util.Constant;
import jp.pioneer.mbg.alexa.util.LogUtil;
import jp.pioneer.mbg.logmanager.TAGS;
import jp.pioneer.mbg.logmanager.TagManager;
import jp.pioneer.mobile.logger.api.Logger;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

//import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Created by esft-komiya on 2016/10/19.
 */

/**
 * OkHttpにて通信したレスポンスを解析するクラス
 */
public class ResponseParser2 {
    private static final Logger logger = TagManager.getInstance().getLogger(TAGS.AVS_Response);
    private static final String TAG = ResponseParser2.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * レスポンス解析メソッド.(マルチパート対応)
     * @param response
     * @param context
     * @param isRecognize
     * @return ArrayList<AlexaIfItem>
     */
    public static ArrayList<AlexaIfDirectiveItem> parseResponse(Response response, Context context, boolean isRecognize) {
        LogUtil.d(TAG, "parseResponse start, isRecognize = " + isRecognize);
        ArrayList<AlexaIfDirectiveItem> itemList = new ArrayList<>();

        ResponseBody body = response.body();
        LogUtil.d(TAG, "network response:" + response.networkResponse());
        logger.d("[response] " + response.networkResponse());

        Headers headers = response.headers();
        String header = headers.get("content-type");
        LogUtil.d(TAG, "headers:" + headers);

        if (header == null) {
            if (isRecognize) {
                // Recognizeイベントのレスポンス無し
                AlexaQueueManager queueManager = AlexaQueueManager.getInstance();
                queueManager.onRecognizeNoResponse();
            }
            return itemList;
        }

        // マルチパートの敷居を判定
        String boundary = getBoundary(response);

        try {
            // 実際のデータ解析処理.
            itemList = parseData2(body, context, boundary, isRecognize);
        } catch (IOException e) {
            e.printStackTrace();
            // 例外発生 -> TODO:エラー
        } finally {
            if(body != null){
                body.close();
            }
        }

        return itemList;
    }

    public static String getBoundary(Response response) {
        String boundary = null;

        Headers headers = response.headers();
        String header = headers.get("content-type");
        LogUtil.d(TAG, "headers:" + headers);

        // マルチパートの敷居を判定
        Pattern pattern = Pattern.compile("boundary=(.*?);");
        if (pattern == null || header == null) {
            // レスポンスなし -> TODO:エラー
            return boundary;
        }

        Matcher matcher = pattern.matcher(header);
        if (matcher.find()) {
            // boundaryの取得.
            boundary = "--" + matcher.group(1);     // [--] + [ヘッダーの設定値]
        }

        return boundary;
    }

    /**
     * 解析処理本体
     * @param body
     * @param context
     * @param boundary
     * @param isRecognize
     * @return ArrayList<AlexaIfItem>
     * @throws IOException
     */
    private static ArrayList<AlexaIfDirectiveItem> parseData2(ResponseBody body, Context context, String boundary, boolean isRecognize) throws IOException {
        // Stream類
        InputStream responseStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        responseStream = body.byteStream();

        byte[] buf = new byte[1024];
        int n = 0;
        while ((n = responseStream.read(buf)) >= 0){
            baos.write(buf, 0, n);
        }
        byte[] content = baos.toByteArray();

        if (DBG) {
            // デバッグ
            String debugStr = new String(content, "UTF-8");
            LogUtil.d("TEST_20190606", "-- parseData2() body : \n" + debugStr);
        }
        String line = null;
        int countline = 0;
        int position = 0;            // 次に読み込むbyteのindexを保持（初期値は0）
        ArrayList<AlexaIfDirectiveItem> itemList = new ArrayList<AlexaIfDirectiveItem>();
        ArrayList<String> backLine = new ArrayList<>();

        boolean isFinish = false;
        while (isFinish == false) {
            // レスポンスの解析.
            {
                byte b = 0;
                baos.reset();
                do {
                    if (content.length <= position) {
                        // 最後まで読み込んだ
                        isFinish = true;
                        break;
                    }
                    b = content[position];
                    baos.write(b);         // byteOutStreamには改行コードも含まれる

                    position = position + 1;        // インクリメント
                } while (b != '\n');
                byte[] bytes = baos.toByteArray();
                line = new String(bytes, "UTF-8");
            }

            if (!TextUtils.isEmpty(line)) {
                LogUtil.d(TAG, "line(" + countline + "):" + line);
                countline++;
                backLine.add(line);
                // 一行ずつ解析して結果を入れる.
                AlexaIfDirectiveItem item = parseLine(line, boundary);
                if (item != null) {
                    itemList.add(item);     // TODO:すべてのアイテムをリストにして、次の処理へ回す
                } else {
                    if (line.contains(Constant.MEDIA_TYPE_OCTET)) {
                        LogUtil.i(TAG, "line reached application/octet-stream");

                        String contentId = null;
                        if (backLine.size() > 1) {
                            contentId = getContentId(backLine.get(backLine.size() - 2));
                        }
                        LogUtil.i(TAG, "contentId = " + contentId);
                        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
                        {
                            boolean isEndBinary = false;
                            while (isEndBinary == false) {
                                byte b = 0;
                                baos.reset();
                                do {
                                    if (content.length <= position) {
                                        // 最後まで読み込んだ
                                        isEndBinary = true;
                                        isFinish = true;
                                        break;
                                    }
                                    b = content[position];
                                    baos.write(b);         // byteOutStreamには改行コードも含まれる

                                    position = position + 1;        // インクリメント
                                } while (b != '\n');
                                byte[] bytes = baos.toByteArray();

                                String tempLine = new String(bytes, "UTF-8");
                                if (isBoundaryEquals(boundary, tempLine)) {
                                    // マルチパートの敷居
                                    isEndBinary = true;
                                } else {
                                    contentStream.write(bytes, 0, bytes.length);
                                }
                            }
                        }
                        byte[] audioContent = contentStream.toByteArray();
                        contentStream.close();
                        if (!TextUtils.isEmpty(contentId)) {
                            for (AlexaIfItem tempItem : itemList) {
                                if (tempItem instanceof SpeakItem) {
                                    String cid = ((SpeakItem) tempItem).url;
                                    if (contentId.equals(cid)) {
                                        ((SpeakItem) tempItem).setAudioContent(audioContent);
                                        break;
                                    }
                                } else if (tempItem instanceof PlayItem) {
                                    String cid = null;
                                    if (((PlayItem) tempItem).audioItem != null && ((PlayItem) tempItem).audioItem.stream != null) {
                                        cid = ((PlayItem) tempItem).audioItem.stream.url;
                                    }
                                    if (contentId.equals(cid)) {
                                        ((PlayItem) tempItem).setAudioContent(audioContent);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (body != null) {
            body.close();
        }
        if (responseStream != null) {
            responseStream.close();
        }
        if(baos != null){
            baos.close();
        }

        return itemList;
    }

    /**
     * 文字列からContent-IDの部分を抽出する
     * @param string
     * @return
     */
    public static String getContentId(String string) {
        String contentId = null;
        LogUtil.i(TAG, "getContentId() string = " + string);

        if (!TextUtils.isEmpty(string) && string.indexOf("Content-ID") > -1) {
            Pattern PATTERN = Pattern.compile("<(.*?)>");
            Matcher matcher = PATTERN.matcher(string);
            if (matcher.find()) {
                contentId = "cid:" + matcher.group(1);
            }
        }
        return contentId;
    }

    /**
     * Jsonの中身を一行ずつ解析するメソッド.
     * @param line
     * @param boundary
     * @return
     * @throws JSONException
     */
    public static AlexaIfDirectiveItem parseLine(String line, String boundary) {
        AlexaIfDirectiveItem item = null;

        Log.d(TAG, "ResponseParser.parseLine() line = " + line);
        Log.d(TAG, "ResponseParser.parseLine() boundary = " + boundary);

        if (!TextUtils.isEmpty(line)) {
//            if (!TextUtils.isEmpty(boundary) && new String("--" + boundary).equals(line)) {
            if (isBoundaryEquals(boundary, line)) {
                // ラインがバウンダリーだった場合はreturn.
                Log.d(TAG, "ResponseParser.parseLine() Match boundary : " + line);
                return null;
            } else if (line.contains(Constant.CONTENT_TYPE)) {       // TODO:正常なJson中に文字列が含まれるケースはあるのか？
                if (line.contains(Constant.MEDIA_TYPE_OCTET)) {
                    // ラインがoctetStreamだった場合はreturn.
                    return null;
                }
                else {
                    // ラインがoctetStreamでは無かった場合もreturn.
                    return null;
                }
            } else {
                // Jsonを解析
                try {
                    item = DirectiveParser.parse(line);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return item;
    }

    /**
     * DownChannel専用 ディレクティブ解析処理
     * @param content
     * @param context
     * @param boundary
     * @throws IOException
     */
    public static ArrayList<AlexaIfDirectiveItem> parseDataWithDownChannel(byte[] content, Context context, String boundary) throws IOException {
        logger.d();
        LogUtil.d(TAG, "parseDataWithDownChannel()");
        ArrayList<AlexaIfDirectiveItem> directiveItems = new ArrayList<AlexaIfDirectiveItem>();

        // Stream類
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        {
            // デバッグ
            String debugStr = new String(content, "UTF-8");
            LogUtil.d("TEST_20190606", "-- parseDataWithDownChannel() body : \n" + debugStr);
            LogUtil.d(TAG, "-- parseDataWithDownChannel() content.length = " + content.length);
        }
        String line = null;
        int countline = 0;
        int position = 0;            // 次に読み込むbyteのindexを保持（初期値は0）
        ArrayList<String> backLine = new ArrayList<>();

        boolean isFinish = false;
        while (isFinish == false) {
            // レスポンスの解析.
            {
                byte b = 0;
                baos.reset();
                do {
                    if (content.length <= position) {
                        // 最後まで読み込んだ
                        isFinish = true;
                        break;
                    }
                    b = content[position];
                    baos.write(b);         // byteOutStreamには改行コードも含まれる

                    position = position + 1;        // インクリメント
                } while (b != '\n');
                byte[] bytes = baos.toByteArray();
                line = new String(bytes, "UTF-8");
            }

            if (!TextUtils.isEmpty(line)) {
                LogUtil.d(TAG, "line(" + countline + "):" + line);
                countline++;
                backLine.add(line);
                // 一行ずつ解析して結果を入れる.
                AlexaIfDirectiveItem item = parseLine(line, boundary);
                if (item != null) {
                    directiveItems.add(item);
                } else {
                    if (line.contains(Constant.MEDIA_TYPE_OCTET)) {
                        LogUtil.i(TAG, "line reached application/octet-stream");

                        String contentId = null;
                        if (backLine.size() > 1) {
                            contentId = getContentId(backLine.get(backLine.size() - 2));
                        }
                        LogUtil.i(TAG, "contentId = " + contentId);
                        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
                        {
                            boolean isEndBinary = false;
                            while (isEndBinary == false) {
                                byte b = 0;
                                baos.reset();
                                do {
                                    if (content.length <= position) {
                                        // 最後まで読み込んだ
                                        isEndBinary = true;
                                        isFinish = true;
                                        break;
                                    }
                                    b = content[position];
                                    baos.write(b);         // byteOutStreamには改行コードも含まれる

                                    position = position + 1;        // インクリメント
                                } while (b != '\n');
                                byte[] bytes = baos.toByteArray();

                                String tempLine = new String(bytes, "UTF-8");
                                if (isBoundaryEquals(boundary, tempLine)) {
                                    // マルチパートの敷居
                                    isEndBinary = true;
                                } else {
                                    contentStream.write(bytes, 0, bytes.length);
                                }
                            }
                        }
                        byte[] audioContent = contentStream.toByteArray();
                        contentStream.close();
                        if (!TextUtils.isEmpty(contentId)) {
                            for (AlexaIfItem tempItem : directiveItems) {
                                if (tempItem instanceof SpeakItem) {
                                    String cid = ((SpeakItem) tempItem).url;
                                    if (contentId.equals(cid)) {
                                        ((SpeakItem) tempItem).setAudioContent(audioContent);
                                        break;
                                    }
                                } else if (tempItem instanceof PlayItem) {
                                    String cid = null;
                                    if (((PlayItem) tempItem).audioItem != null && ((PlayItem) tempItem).audioItem.stream != null) {
                                        cid = ((PlayItem) tempItem).audioItem.stream.url;
                                    }
                                    if (contentId.equals(cid)) {
                                        ((PlayItem) tempItem).setAudioContent(audioContent);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(baos != null){
            baos.close();
        }

        return directiveItems;
    }

    /**
     * マルチパートの敷居を判定する
     * @param boundary
     * @param str
     * @return
     */
    private static boolean isBoundaryEquals(String boundary, String str) {
        if (TextUtils.isEmpty(boundary) ) {
            return false;
        }
        else if (boundary.equals(str)) {
            return true;
        }
        else if (new String(boundary + "\r\n").equals(str)) {
            // 改行コード
            return true;
        }
        else if (new String(boundary + "--").equals(str)) {
            // 終端
            return true;
        }
        else if (new String(boundary + "--\r\n").equals(str)) {
            // 終端 + 改行コード
            return true;
        }
        return false;
    }

    /**
     * URLがcidか判定する
     * @param url
     * @return
     */
    public static boolean isCid(String url) {
        boolean result = false;
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("cid:")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean checkContentIdCount(byte[] content, String boundary) throws IOException {
        LogUtil.d(TAG, "checkContentIdCount()");
        boolean result = false;
        ArrayList<String> directiveCidList = new ArrayList<>();
        ArrayList<String> contentIdList = new ArrayList<>();
        // Stream類
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        {
            // デバッグ
            String debugStr = new String(content, "UTF-8");
            LogUtil.d(TAG, "-- checkContentIdCount() body : \n" + debugStr);
            LogUtil.d(TAG, "-- checkContentIdCount() content.length = " + content.length);
        }
        String line = null;
        int countline = 0;
        int position = 0;            // 次に読み込むbyteのindexを保持（初期値は0）
        ArrayList<String> backLine = new ArrayList<>();

        boolean isFinish = false;
        while (isFinish == false) {
            // レスポンスの解析.
            {
                byte b = 0;
                baos.reset();
                do {
                    if (content.length <= position) {
                        // 最後まで読み込んだ
                        isFinish = true;
                        break;
                    }
                    b = content[position];
                    baos.write(b);         // byteOutStreamには改行コードも含まれる

                    position = position + 1;        // インクリメント
                } while (b != '\n');
                byte[] bytes = baos.toByteArray();
                line = new String(bytes, "UTF-8");
            }

            if (!TextUtils.isEmpty(line)) {
                LogUtil.d(TAG, "line(" + countline + "):" + line);
                countline++;
                backLine.add(line);
                // 一行ずつ解析して結果を入れる.
                AlexaIfItem item = parseLine(line, boundary);
                if (item != null) {
                    if (item instanceof SpeakItem) {
                        String contentUrl = ((SpeakItem) item).url;
                        boolean hasCid = isCid(contentUrl);
                        if (hasCid) {
                           directiveCidList.add(contentUrl);
                        }
                    }
                    else if (item instanceof PlayItem) {
                        String contentUrl = ((PlayItem) item).audioItem.stream.url;
                        boolean hasCid = isCid(contentUrl);
                        if (hasCid) {
                            directiveCidList.add(contentUrl);
                        }
                    }
                } else {
                    if (line.contains(Constant.MEDIA_TYPE_OCTET)) {
                        LogUtil.i(TAG, "line reached application/octet-stream");
                        if (backLine.size() > 1) {
                            String contentId = getContentId(backLine.get(backLine.size() - 2));
                            contentIdList.add(contentId);
                        }
                    }
                }
            }
        }
        if(baos != null){
            baos.close();
        }

        if (contentIdList.size() == directiveCidList.size()) {
            result = true;  // 一時的に初期値を変更
            for (String contentId : contentIdList) {
                boolean isMatch = directiveCidList.contains(contentId);
                if (DBG) android.util.Log.d(TAG, " -- checkContentIdCount(), ContentId = " + contentId);
                if (DBG) android.util.Log.d(TAG, " -- checkContentIdCount(), ContentId isMatch = " + isMatch);
                if (!isMatch) {
                    result = false;
                    // 一致しないものが１つでもあれば終了
                    break;
                }
            }
        }
        else {
            if (DBG) android.util.Log.d(TAG, " -- checkContentIdCount(), Content-ID List is size does not match.");
        }
        return result;
    }
}
