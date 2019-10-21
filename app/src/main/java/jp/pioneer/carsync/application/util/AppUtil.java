package jp.pioneer.carsync.application.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.PowerManager;

import java.util.Locale;

import jp.pioneer.carsync.R;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * 各層で使用できるUtilクラス.
 * <p>
 * 複数の層で使用する可能性のあるものを定義している。
 */
public class AppUtil {

    /**
     * 画面ON状態(非スリープ)か否か
     *
     * @param context Context
     */
    public static boolean isScreenOn(Context context) {
        boolean isScreenOn, isScreenLock;

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

        if(pm != null){
            isScreenOn = pm.isInteractive();

            if(km != null){
                isScreenLock = km.isKeyguardLocked();

                return isScreenOn && !isScreenLock;
            }
            return isScreenOn;
        }

        return true;
    }

    /**
     * Locale取得.
     * <p>
     * TTS,音声認識で使用する。
     * 文言IDで定義されているLocaleを返す。
     *
     * @param context Context
     * @return 文言IDで定義されているLocale
     */
    public static Locale getCurrentLocale(Context context){
        return Locale.forLanguageTag(context.getString(R.string.com_011));
    }

    /**
     * デフォルトのLocale取得.
     * <p>
     * アプリとしてデフォルトとなっているUS Localeを返す
     *
     * @return US Locale
     */
    public static Locale getDefaultLocale(){
        return Locale.US;
    }

    /**
     * デフォルトLocaleのリソース取得.
     *
     * @param context Context
     * @return デフォルトLocaleのリソース
     */
    public static Resources getDefaultLocalizedResources(Context context) {
        Configuration conf = context.getResources().getConfiguration();
        conf.setLocale(getDefaultLocale());
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    /**
     * 12時間制で0-11の表記か否かを判定.
     * <p>
     * HOME画面、再生画面の時間表示、ディマー設定で使用する。
     *
     * @param context Context
     * @return 12時間制で0-11の表記か否か
     */
    public static boolean isZero2ElevenIn12Hour(Context context){
        return context.getString(R.string.com_012).equals("ja_jp");
    }
}
