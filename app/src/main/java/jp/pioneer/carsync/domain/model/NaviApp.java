package jp.pioneer.carsync.domain.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ナビアプリ.
 * <p>
 * 本アプリと連携するナビアプリの定義。
 */
public enum NaviApp implements BaseApp{
    /** Google Maps. */
    GOOGLE_MAP("com.google.android.apps.maps") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            Uri uri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** Here WeGo. */
    HERE_WE_GO("com.here.app.maps"),
    /** INRIX. */
    INRIX("inrix.android.ui"),
    /** Sygic. */
    SYGIC("com.sygic.aura") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            Uri uri = Uri.parse("com.sygic.aura://coordinate|" + longitude + "|" + latitude + "|drive");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** Waze. */
    WAZE("com.waze") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            Uri uri = Uri.parse("waze://?ll=" + latitude + "," + longitude + "&navigate=yes");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** Yahoo カーナビ. */
    YAHOO_CAR_NAVI("jp.co.yahoo.android.apps.navi"),
    /** Yandex. */
    YANDEX("ru.yandex.yandexnavi") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            Intent intent = createMainIntent(context);
            intent.setAction(getAction());
            intent.putExtra("lat_to", latitude);
            intent.putExtra("lon_to", longitude);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        String getAction() {
            return "ru.yandex.yandexnavi.ACTION.BUILD_ROUTE_ON_MAP";
        }
    },
    /** NAVITIME ドライブサポーター. */
    NAVITIME_DRIVE("com.navitime.local.navitimedrive") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createMainIntent(Context context) {
            Uri uri = Uri.parse("launchnavitime://navitimedrive/top");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            //世界測地系→日本測地系の変換
            double lonJ, latJ;
            int minuteLongitude, minuteLatitude;
            lonJ = longitude + latitude * 0.000046047 + longitude * 0.000083049 - 0.010041;
            latJ = latitude + latitude * 0.00010696 - longitude * 0.000017467 - 0.0046020;
            //度分秒形式からミリ秒形式に変換する
            minuteLongitude = (int)NaviApp.toMillisecond(lonJ);
            minuteLatitude = (int)NaviApp.toMillisecond(latJ);
            String encodedResult = "";
            try {
                encodedResult = URLEncoder.encode(destination, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Timber.e("UnsupportedEncodingException");
            }
            Uri uri = Uri.parse("launchnavitime://navitimedrive/route/result?dLat=" + minuteLatitude + "&dLon=" + minuteLongitude + "&dNm=" + encodedResult);
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** カーナビタイム */
    CAR_NAVI_TIME("com.navitime.local.carnavitime") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createMainIntent(Context context) {
            Uri uri = Uri.parse("carnavitime://nativecontents?page=topmenu");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            String encodedResult = "";
            //世界測地系→日本測地系の変換
            double lonJ, latJ;
            int minuteLongitude, minuteLatitude;
            lonJ = longitude + latitude * 0.000046047 + longitude * 0.000083049 - 0.010041;
            latJ = latitude + latitude * 0.00010696 - longitude * 0.000017467 - 0.0046020;
            //度分秒形式からミリ秒形式に変換する
            minuteLongitude = (int)NaviApp.toMillisecond(lonJ);
            minuteLatitude = (int)NaviApp.toMillisecond(latJ);
            try {
                encodedResult = URLEncoder.encode(destination, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Timber.e("UnsupportedEncodingException");
            }
            Uri uri = Uri.parse("carnavitime://route?g_lat=" + minuteLatitude + "&g_lon=" + minuteLongitude + "&g_name=" + encodedResult);
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** ドコモ ドライブネットナビ（カーナビ） */
    DRIVE_NET_NAVI("com.nttdocomo.android.drivenet.navi") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            String encodedResult = "";
            try {
                encodedResult = URLEncoder.encode(destination, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Timber.e("UnsupportedEncodingException");
            }
            Uri uri = Uri.parse("drivenet://?mode=view&center=" + latitude + "," + longitude + "&pin=true");
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** MapFan */
    MAP_FAN("jp.co.incrementp.mapfan.navi") {
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            Uri uri = Uri.parse("mapfannavi://jp.co.incrementp.mapfan.navi?mode=routesearch&rstype=car&goal=" + latitude + "," + longitude);
            Intent intent = new Intent(getAction(), uri);
            intent.setPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    },
    /** Uber Driver */
    UBER_DRIVER("com.ubercab.driver"){
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            //起動のみ対応
            return createMainIntent(context);
        }
    },
    /** Lyft Driver */
    LYFT_DRIVER("com.lyft.android.driver"){
        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
            //起動のみ対応
            return createMainIntent(context);
        }
    },
    ;

    /**
     * パッケージ名から{@link NaviApp}取得.
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link NaviApp}
     * @throws NullPointerException {@code packageName}がnull
     * @throws IllegalArgumentException パッケージ名に該当するものがない
     * @see #fromPackageNameNoThrow(String)
     */
    @NonNull
    public static NaviApp fromPackageName(@NonNull String packageName) {
        NaviApp naviApp = fromPackageNameNoThrow(packageName);
        if (naviApp == null) {
            throw new IllegalArgumentException("invalid packageName: " + packageName);
        }

        return naviApp;
    }

    /**
     * パッケージ名から{@link NaviApp}取得.
     * <p>
     * パッケージ名に該当するものがない場合にnullを返してほしい場合に使用する。
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link NaviApp}。該当するものがない場合はnull。
     * @throws NullPointerException {@code packageName}がnull
     * @see #fromPackageName(String)
     */
    @Nullable
    public static NaviApp fromPackageNameNoThrow(@NonNull String packageName) {
        checkNotNull(packageName);

        for (NaviApp naviApp : NaviApp.values()) {
            if (packageName.equals(naviApp.getPackageName())) {
                return naviApp;
            }
        }

        return null;
    }

    private String mPackageName;

    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    NaviApp(String packageName) {
        mPackageName = packageName;
    }

    /**
     * パッケージ名取得.
     *
     * @return パッケージ名
     */
    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * 通常起動インテント作成.
     *
     * @return 通常起動を行う {@link Intent}
     */
    @NonNull
    public Intent createMainIntent(Context context) {
        Intent intent = new Intent(getAction());
        intent.setPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);

        // packageNameで指定されたアプリのメインActivityを探す
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if(list.size() > 0) {
            ComponentName component = new ComponentName(getPackageName(), list.get(0).activityInfo.name);
            intent.setComponent(component);
        }
        return intent;
    }

    /**
     * ナビゲーションインテント作成.
     * <p>
     * ナビアプリにナビゲーション指定の起動を行う。
     * 起動方法はナビアプリ固有で、非公開のものが多い（そもそも非対応）ので、
     * 一般的な地図表示を行うインテントを既定の実装としている.
     * ナビゲーション指定の起動が判明している場合、オーバーライドして
     * 適切なインテントを作成すること。
     *
     * @return ナビゲーション起動を行う {@link Intent}
     * @see #createMapIntent(double, double)
     */
    @NonNull
    public Intent createNavigationIntent(double latitude, double longitude, String destination, Context context) {
        return createMapIntent(latitude, longitude);
    }

    /**
     * アクション取得.
     * <p>
     * 通常は{@link Intent#ACTION_VIEW}であるが、そうでない場合はオーバーライドする。
     *
     * @return インテントのアクション
     */
    @NonNull
    String getAction() {
        return Intent.ACTION_VIEW;
    }

    /**
     * 地図表示インテント作成.
     * <p>
     * 指定した位置の地図表示を行う一般的なインテントを作成する。
     * ナビゲーション起動の方法が非公開のものは、このインテントでとりあえず
     * 指定位置の地図表示を行うことが大抵出来る。（全てではない…）
     *
     * @param latitude 緯度
     * @param longitude 経度
     * @return 表示位置指定の起動を行う {@link Intent}
     */
    @NonNull
    private Intent createMapIntent(double latitude, double longitude) {
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
        Intent intent = new Intent(getAction(), uri);
        intent.setPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 度分秒形式からミリ秒形式に変換する
     *
     * 度→分: *60
     * 分→秒: *60
     * 秒→ミリ秒: *1000
     *
     * @param degree 度分秒形式の緯度・経度
     * @return ミリ秒形式の緯度・経度
     */
    private static double toMillisecond(double degree){
        return degree * 3600000;
    }
}
