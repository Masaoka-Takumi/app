package jp.pioneer.carsync.domain.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public enum MarinApp {
    /** Buoyweather. */
    BUOY_WEATHER("com.buoyweather.android", MarinAppCategory.WEATHER),
    /** AccuWeather: Weather Alerts. */
    ACCU_WEATHER("com.accuweather.android", MarinAppCategory.WEATHER),
    /** WeatherBug - Radar, Forecast. */
    WEATHER_BUG("com.aws.android", MarinAppCategory.WEATHER),
    /** MyRadar NOAA Weather Radar. */
    MYRADAR_NOAA("com.acmeaom.android.myradar", MarinAppCategory.WEATHER),
    /** The Weather chanel: Forecast. */
    THE_WEATHER_CHANEL("com.weather.Weather", MarinAppCategory.WEATHER),
    /** NOAA Radar - Weather & Alerts. */
    NOAA_RADAR("com.apalon.weatherradar.free", MarinAppCategory.WEATHER),
    /** Tide Charts. */
    TIDE_CHARTS("com.SeventhGear.tides", MarinAppCategory.BOATING),
    /** WINDY - wind & waves forecast. */
    WINDY("co.windyapp.android", MarinAppCategory.BOATING),
    /** Windy.com. */
    WINDY_COM("com.windyty.android", MarinAppCategory.BOATING),
    /** Ship Finder. */
    SHIP_FINDER("com.pinkfroot.shipfinder", MarinAppCategory.BOATING),
    /** Boating Marine & Lakes. */
    BOATING_LAKES("it.navionics.singleAppMarineLakes", MarinAppCategory.BOATING),
    /** Fishbrain - Fishing App. */
    FISHBRAIN("com.fishbrain.app", MarinAppCategory.FISHING),
    /** FishTrack - Charts & Forecasts. */
    FISH_TRACK("com.fishtrack.android", MarinAppCategory.FISHING),
    /** Pro Angler - Fishing App. */
    PRO_ANGLER("us.openocean.proangler", MarinAppCategory.FISHING),
    ;
    private String mPackageName;
    private MarinAppCategory mCategory;

    /**
     * パッケージ名から{@link NaviApp}取得.
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MarinApp}
     * @throws NullPointerException {@code packageName}がnull
     * @throws IllegalArgumentException パッケージ名に該当するものがない
     * @see #fromPackageNameNoThrow(String)
     */
    @NonNull
    public static MarinApp fromPackageName(@NonNull String packageName) {
        MarinApp marinApp = fromPackageNameNoThrow(packageName);
        if (marinApp == null) {
            throw new IllegalArgumentException("invalid packageName: " + packageName);
        }

        return marinApp;
    }

    /**
     * パッケージ名から{@link NaviApp}取得.
     * <p>
     * パッケージ名に該当するものがない場合にnullを返してほしい場合に使用する。
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MarinApp}。該当するものがない場合はnull。
     * @throws NullPointerException {@code packageName}がnull
     * @see #fromPackageName(String)
     */
    @Nullable
    public static MarinApp fromPackageNameNoThrow(@NonNull String packageName) {
        checkNotNull(packageName);

        for (MarinApp marinApp : MarinApp.values()) {
            if (packageName.equals(marinApp.getPackageName())) {
                return marinApp;
            }
        }

        return null;
    }

    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     * @param category カテゴリー
     */
    MarinApp(String packageName, MarinAppCategory category) {
        mPackageName = packageName;
        mCategory = category;
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
     * カテゴリー取得.
     *
     * @return カテゴリー
     */
    @NonNull
    public MarinAppCategory getCategory() {
        return mCategory;
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
}
