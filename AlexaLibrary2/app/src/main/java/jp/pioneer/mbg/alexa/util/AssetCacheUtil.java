package jp.pioneer.mbg.alexa.util;

import java.io.IOException;

import jp.pioneer.mbg.alexa.connection.OkHttpConnector;

/**
 * Created by esft-sakamori on 2018/01/19.
 * インターネット上の音楽ファイル、及び、画像ファイルのバイナリデータを取得する
 */

public class AssetCacheUtil {

    /**
     * Assetデータのキャッシュ取得
     * @param url
     * @return
     */
    public static byte[] getAssetCache(String url){
        byte[] result = null;

        try {
            result = OkHttpConnector.getBody(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


}
