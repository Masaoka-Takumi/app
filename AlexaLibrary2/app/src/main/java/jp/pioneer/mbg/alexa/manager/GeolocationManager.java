package jp.pioneer.mbg.alexa.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Contextに付与するGeolocationStateの値を管理するマネージャ
 */
public class GeolocationManager {
    private static String TAG = GeolocationManager.class.getSimpleName();
    private static boolean DBG = false ;

    private static GeolocationManager mManager = null;

    private Context mContext = null;
    private LocationManager mLocationManager = null;
    private Location mLastKnownLocation = null;
    private GeolocationListener mListener = null;

    //緯度経度関連
    private double latitude1;
    private double longitude1;
    private double latitude2;
    private double longitude2;

    //距離測定用
    private float[] distance;

    private  boolean isFirst = false;
    private GeolocationManager() {
    }

    /**
     * 生成済みマネージャインスタンスを取得
     * @return
     */
    public static GeolocationManager getInstance() {
        if (DBG) android.util.Log.d(TAG, "getInstance()");
        if (mManager == null) {
            mManager = new GeolocationManager();
        }
        return mManager;
    }

    public static void resetManager() {
        if (mManager != null) {

            // LocationManagerを解除
            //mManager.mLocationManager.removeUpdates(mManager.mListener);
            mManager.mLocationManager = null;
            mManager.mListener = null;
            // 最後に受信した位置情報を削除
            mManager.mLastKnownLocation = null;
            // コンテキストを削除
            mManager.mContext = null;

            // 緯度経度情報を初期化
            mManager.latitude1 = 0;
            mManager.longitude1 = 0;
            mManager.latitude2 = 0;
            mManager.longitude2 = 0;

            mManager.distance = null;

            mManager.isFirst = false;
        }
    }

    /**
     * 初期化
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        if (mLocationManager != null && mListener != null) {
            // 古い設定をリセット
            mLocationManager.removeUpdates(mListener);
        }
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            mListener = new GeolocationListener();
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 10, mListener);
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 10, mListener);
            }
        }
    }

    public Location getLocation() {
        if (DBG) android.util.Log.d(TAG, "getLocation()");
        Location location = null;
        if (mLastKnownLocation != null) {
            location = mLastKnownLocation;
        }
        else {
            if (mLocationManager != null) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (location == null) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
        }

        if (DBG) android.util.Log.d(TAG, "getLocation(), location = " + location);

        return location;
    }
    /*
     * 2点間の距離（メートル）、方位角（始点、終点）を取得
     * ※配列で返す[距離、始点から見た方位角、終点から見た方位角]
     */
    public float[] getDistance(double x, double y, double x2, double y2) {
        // 結果を格納するための配列を生成
        float[] results = new float[3];

        // 距離計算
        Location.distanceBetween(x, y, x2, y2, results);

        return results;
    }
    /**
     * 位置情報リスナー
     */
    private class GeolocationListener implements LocationListener {
        private String TAG = GeolocationManager.TAG + "#" + GeolocationListener.class.getSimpleName();
        /**
         * 位置情報の更新通知
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            if (DBG) android.util.Log.d(TAG, "************************locationChange()" + location);

            //現在の緯度経度
            latitude2 = location.getLatitude();
            longitude2 = location.getLongitude();


            if (mLastKnownLocation != null) {
                //前回取得分の緯度経度
                latitude1 = mLastKnownLocation.getLatitude();
                longitude1 = mLastKnownLocation.getLongitude();

                //距離測定
                distance =
                        getDistance(latitude1, longitude1, latitude2, longitude2);
                if (DBG) android.util.Log.d(TAG, "************************distance()" + distance[0]/1000);

                //時間取得
                long time = location.getTime() - mLastKnownLocation.getTime();
                if (DBG) android.util.Log.d(TAG, "************************time()" + time);

                //距離の差が10m異常、時間の差が5秒以内だったら更新
                if (distance[0]/1000 > 10 && time >5000) {
                    //更新
                    mLastKnownLocation = location;
                    if (DBG) android.util.Log.d(TAG, "************************こうしんされた");
                }
            }
            //初回取得時のみ
            if(!isFirst){
                mLastKnownLocation = location;
                isFirst = true;
            }
        }

        /**
         * プロバイダーの状態更新通知
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (DBG) android.util.Log.d(TAG, "onStatusChanged(), provider = " + provider + ", status = " + status + ", extras = " + extras);
        }

        /**
         * プロバイダーの有効通知
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            if (DBG) android.util.Log.d(TAG, "onProviderEnabled(), provider = " + provider);
        }

        /**
         * プロバイダーの無効通知
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
            if (DBG) android.util.Log.d(TAG, "onProviderDisabled(), provider = " + provider);
        }
    }

}
