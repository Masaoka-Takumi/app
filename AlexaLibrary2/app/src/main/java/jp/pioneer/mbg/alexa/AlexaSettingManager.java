package jp.pioneer.mbg.alexa;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Alexa設定関連の処理を行うクラス.
 */
public class AlexaSettingManager {

    /** 設定値保存ファイル名. */
    private static final String PREF_FILE_NAME = "alexa_setting";

    /** インスタンス. */
    private static AlexaSettingManager sInstance;

    /**
     * インスタンスを取得する.
     * @return AlexaSettingManagerインスタンス
     */
    public static AlexaSettingManager getInstance() {
        if (sInstance == null) {
            sInstance = new AlexaSettingManager();
        }
        return sInstance;
    }

    /**
     * SharedPreferencesインスタンスを取得する.
     * @return SharedPreferencesインスタンス
     */
    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Editorインスタンスを取得する.
     * @return
     */
    private SharedPreferences.Editor getEditor(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
    }

    /**
     * Alexa設定を初期化する.
     */
    public void settingToDefault(Context context) {
        // 言語設定
        setLanguage(context, Language.ENGLISH_US);
    }

    /**
     * =============================================================================================
     * 言語設定.
     * =============================================================================================
     */

    /** キー(Language). */
    private static final String KEY_LANGUAGE = "alexa_setting_language";

    /**
     * 言語設定値を保存する.
     * @param value 言語設定値
     */
    public void setLanguage(Context context, Language value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(KEY_LANGUAGE, value.getLanguageCode());
        editor.commit();
    }

    /**
     * 言語設定値を取得する.(デフォルトENGLISH_US)
     * @return Language 言語設定値
     */
    public Language getLanguage(Context context) {
        String languageCode = getSharedPreferences(context).getString(KEY_LANGUAGE, Language.ENGLISH_US.getLanguageCode());
        Language[] languages = Language.values();
        for (Language language : languages) {
            if (language.getLanguageCode().equals(languageCode)) return language;
        }
        return Language.ENGLISH_US;
    }

    /**
     * 利用可能な言語一覧を返却する.
     * 順番は言語設定画面の表示順に準拠.
     * @return Language[] 言語一覧
     */
    public Language[] getLanguages() {

        //Language[] languages = new Language[6];

        //1stらウンチはEnglishのみ
        Language[] languages = new Language[1];
        languages[0] = Language.ENGLISH_US;


//        languages[0] = Language.DEUTSCH;
//        languages[1] = Language.ENGLISH_US;
//        languages[2] = Language.ENGLISH_UK;
//        languages[3] = Language.ENGLISH_CA;
//        languages[4] = Language.ENGLISH_AU;
//        languages[5] = Language.ENGLISH_IN;

        return languages;
    }

    /**
     * 言語設定値.
     */
    public enum Language {
//        JAPANESE("Japanese", "Japanese", "ja-JP"),
//        DEUTSCH("Deutsch", "Deutsch", "de-DE"),
//        ENGLISH_AU("English(Australia)", "English (AU)", "en-AU"),
//        ENGLISH_CA("English(Canada)", "English (CA)", "en-CA"),
//        ENGLISH_IN("English(India)", "English (IN)", "en-IN"),
//        ENGLISH_UK("English(United Kingdom)", "English (UK)", "en-GB"),
        ENGLISH_US("English(United States)", "English (US)", "en-US");
        // 言語設定増やしたい場合はここに追加しよう！

        private String mName;
        private String mShortName;
        private String mLanguageCode;

        Language(String name, String shortName, String languageCode) {
            this.mName = name;
            this.mShortName = shortName;
            this.mLanguageCode = languageCode;
        }

        /**
         * 言語名を取得する.
         * @return String 言語名
         */
        public String getName() {
            return mName;
        }

        /**
         * 言語名(短縮版)を取得する.
         * @return String 言語名(短縮版)
         */
        public String getShortName() {
            return mShortName;
        }

        /**
         * 言語コードを取得する.
         * @return String 言語コード
         */
        public String getLanguageCode() {
            return mLanguageCode;
        }
    }

}
