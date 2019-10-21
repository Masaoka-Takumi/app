package jp.pioneer.mbg.alexa.manager.notification;

import java.io.Serializable;


/**
 * Created by esft-sakamori on 2018/01/24.
 */

/**
 * Notificationのバースト音を保存するデータセット
 */
public class NotificationAssetCache implements Serializable {
    private static final String TAG = NotificationAssetCache.class.getSimpleName();
    private static final boolean DBG = true;

    private static final long serialVersionUID = 1L;

    public String assetId = null;
    public byte[] cache = null;

    public NotificationAssetCache(String assetId, byte[] cache) {
        if (DBG) android.util.Log.d(TAG, "NotificationAssetCache(), assetId = " + assetId);
        this.assetId = assetId;
        this.cache = cache;
    }

}