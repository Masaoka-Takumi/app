package jp.pioneer.mbg.alexa.util;

import android.content.Context;


/**
 * Created by esft-sakamori on 2017/12/07.
 */

/**
 * SettingsUpdatedイベントにて送信するデータの補助を行う
 */
public class SettingsUpdatedUtil {
    private static String mLocale = "en-US";

    public static void setLocale(String mLocale) {
        SettingsUpdatedUtil.mLocale = mLocale;
    }
    /**
     * SettingsUpdatedのlocaleに設定する言語設定の文字列を取得する
     * @param context
     * @return
     */
    public static String getLocaleSetting(Context context) {
        return mLocale;
    }
    /**
     * SettingsUpdatedのlocaleに設定する言語設定の文字列を取得する
     * @return
     */
/*
    public static String getLocaleSetting(Context context) {
        return AlexaSettingManager.getInstance().getLanguage(context).getLanguageCode();
    }
*/

}
