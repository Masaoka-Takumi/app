package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by tsuyosh on 2015/09/07.
 */
public class AppConfigUtil {
//    /**
//     * 3rd appsでMovie動画が利用可能な場合はtrueを返す
//     * @param context
//     * @return
//     */
//    public static boolean isMovieAppAvailable(@NonNull Context context) {
//        return isMovieAppAvailable(context, getCurrentCountryCode(context));
//    }
//
//    /**
//     * 3rd appsでMovie動画が利用可能な場合はtrueを返す
//     * @param context
//     * @param countryCode
//     * @return
//     */
//    public static boolean isMovieAppAvailable(@NonNull Context context, @NonNull String countryCode) {
//        if (BuildConfig.UNLOCKED) return true;
//
//        String[] countries = context.getResources().getStringArray(R.array.movieAvailableCountries);
//        for (String cc : countries) {
//            if (cc.equalsIgnoreCase(countryCode)) return true;
//        }
//        return false;
//    }
//
//    /**
//     * Share機能が使える場合はtrueを返す
//     * @param context
//     * @return
//     */
//    public static boolean isShareAvailable(@NonNull Context context) {
//        return isShareAvailable(context, getCurrentCountryCode(context));
//    }
//
//    /**
//     * Share機能が使える場合はtrueを返す
//     * @param context
//     * @param countryCode
//     * @return
//     */
//    public static boolean isShareAvailable(@NonNull Context context, @NonNull String countryCode) {
//        // 機能制限を解除する場合はlocaleに関係なくtrue
//        if (BuildConfig.UNLOCKED) return true;
//
//        String[] countries = context.getResources().getStringArray(R.array.shareAvailableCountries);
//        for (String cc : countries) {
//            if (cc.equalsIgnoreCase(countryCode)) return true;
//        }
//        return false;
//    }
//
//    /**
//     * この端末はあると思われる国コードを取得する
//     * @param context
//     * @return
//     */
//    public static String getCurrentCountryCode(Context context) {
//        Locale locale = context.getResources().getConfiguration().locale;
//        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String cc = manager.getNetworkCountryIso(); // TODO READ_PHONE_STATE 権限が必要
//        return !TextUtils.isEmpty(cc) ? cc : locale.getCountry();
//    }
//
//    public static boolean isIdexiSupported(@NonNull Context context) {
//        if (!BuildConfig.IDEXI_SUPPORTED) {
//            // iDEXiサポートを外す場合
//            return false;
//        }
//
//        String cc = getCurrentCountryCode(context);
//        // アメリカ以外は利用できない(Unlock版はその制約はない)
//        if (!isUnitedStates(cc) && !BuildConfig.UNLOCKED) {
//            return false;
//        }
//
//        return true;
//    }
//
//    public static boolean isIdexiAvailable(@NonNull Context context) {
//        if (!isIdexiSupported(context)) return false;
//
//        // iDEXi承諾済みならOK
//        return PreferencesUtil.isIdexiAccepted(context);
//    }
//
//    private static boolean isUnitedStates(String cc) {
//        // TODO 他の国コードもあるか確認
//        return "us".equalsIgnoreCase(cc);
//    }

    /**
     * 画面の向きを取得する
     * @param context
     * @return Eg. Configuration.ORIENTATION_PORTRAIT
     */
    public static int getCurrentOrientation(Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        return config.orientation;
    }



}
