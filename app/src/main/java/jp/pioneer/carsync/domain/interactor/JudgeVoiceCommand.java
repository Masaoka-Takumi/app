package jp.pioneer.carsync.domain.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.domain.model.VoiceRecognitionSearchType;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.interactor.JudgeVoiceCommand.JudgeResult.*;

/**
 * 音声認識判定.
 * <p>
 * 音声認識の結果から有効な枕詞かどうかを判定する
 * 判定結果の説明は{@link JudgeResult}参照
 */
public class JudgeVoiceCommand {
    @Inject Context mContext;
    @Inject GetStatusHolder mStatusCase;
    private Set<MediaSourceType> mCurrentAvailableSourceType = new HashSet<>();
    /**
     * コンストラクタ
     */
    @Inject
    public JudgeVoiceCommand() {

    }

    public void setCurrentAvailableSourceType(Set<MediaSourceType> currentAvailableSourceType) {
        mCurrentAvailableSourceType = currentAvailableSourceType;
    }

    /**
     * 実行.
     *
     * @param recognizeResults 音声認識結果
     * @param searchType       検索種別
     * @return 判定結果
     * @throws NullPointerException {@code recognizeResults}、{@code searchType}がnull
     */
    @Nullable
    public JudgeResult execute(@NonNull ArrayList<String> recognizeResults, @NonNull VoiceRecognitionSearchType searchType) {
        checkNotNull(recognizeResults);
        checkNotNull(searchType);

        VoiceCommand[] commands = searchType.enabledCommands;
        for (VoiceCommand command : commands) {
            String[] recognizeWords = recognizeResults.subList(0, Math.min(recognizeResults.size(), 10)).toArray(new String[0]);
            String[] judgeWords = mContext.getString(command.id).split(",");

            for (String recognizeWord : recognizeWords) {
                for (String keyword : judgeWords) {
                    if (recognizeWord.toUpperCase().startsWith(keyword.toUpperCase())) {
                        if (recognizeWord.toUpperCase().equals(keyword.toUpperCase())) {
                            return createNotSearchWordResult(command);
                        } else {
                            if (command.isMusicCommand()) {
                                return createLocalResult(command, recognizeWords);
                            } else {
                                return createGlobalResult(command, recognizeWords);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 検索内容判定.
     *
     * @param command    音声コマンド
     * @param searchWord 検索内容
     * @return 検索内容の判定結果
     */
    public String judgeSearchWord(VoiceCommand command, String searchWord) {
        String text;
        switch (command) {
            case SETTING:
                text = judgeSettingType(searchWord);
                break;
            case AUDIO:
                text = judgeAudioSourceType(searchWord);
                break;
            default:
                text = searchWord;
                break;
        }
        return text;
    }

    private JudgeResult createLocalResult(VoiceCommand command, String[] recognizeWords){
        return new JudgeResult(
                command,
                createSearchWords(command, recognizeWords)
        );
    }

    private JudgeResult createGlobalResult(VoiceCommand command, String[] recognizeWords){
        return new JudgeResult(
                command,
                createSearchWords(command, recognizeWords)
        );
    }

    private JudgeResult createNotSearchWordResult(VoiceCommand command){
        return new JudgeResult(
                command,
                null
        );
    }

    private String[] createSearchWords(VoiceCommand command, String[] recognizeWords){
        ArrayList<String> results = new ArrayList<>();

        for (String recognizeWord : recognizeWords) {
            for (String keyword : mContext.getString(command.id).split(",")) {
                if (recognizeWord.toUpperCase().startsWith(keyword.toUpperCase()) &&
                        !recognizeWord.toUpperCase().equals(keyword.toUpperCase())) {
                    String word = recognizeWord.substring(keyword.length()).trim();
                    if(!results.contains(word)){
                        results.add(word);
                    }
                }
            }
        }

        return results.toArray(new String[0]);
    }

    /**
     * 設定分類判定.
     * <p>
     * 発話内容から設定分類名を返す
     * 発話されていない場合は空文字を返す
     * 発話内容から設定分類名が存在しなかった場合はunknownを返す
     *
     * @param searchText 分類名
     * @return 結果
     */
    private String judgeSettingType(String searchText) {
        Map<String, String[]> settingTypeWordMap = getSettingTypeWordMap();
        for (Map.Entry<String, String[]> entry : settingTypeWordMap.entrySet()) {
            for (String word : entry.getValue()) {
                if (searchText.toUpperCase().startsWith(word.toUpperCase())) {
                    return entry.getKey();
                }
            }
        }

        return UNKNOWN;
    }

    /**
     * オーディオソース判定.
     * <p>
     * 発話内容からソース名を返す
     * 発話されていない場合は空文字を返す
     * 発話内容からソース名が存在しなかった場合はunknownを返す
     *
     * @param searchText ソース名
     * @return 結果
     */
    private String judgeAudioSourceType(String searchText) {
        Map<String, String[]> sourceTypeWordMap = getSourceTypeWordMap();
        for (Map.Entry<String, String[]> entry : sourceTypeWordMap.entrySet()) {
            for (String word : entry.getValue()) {
                if (searchText.toUpperCase().startsWith(word.toUpperCase())) {
                    return entry.getKey();
                }
            }
        }

        return UNKNOWN;
    }

    /**
     * 設定分類名と判定用のワード群のマップ取得.
     *
     * @return 設定分類名と判定用のワード群のマップ
     */
    private Map<String, String[]> getSettingTypeWordMap() {
        StatusHolder holder = mStatusCase.execute();
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        return new HashMap<String, String[]>() {{
            put(SETTING_SYSTEM, mContext.getString(R.string.vrkey_008).split(","));
            put(SETTING_VOICE, mContext.getString(R.string.vrkey_009).split(","));
            put(SETTING_NAVI, mContext.getString(R.string.vrkey_010).split(","));
            put(SETTING_MESSAGE, mContext.getString(R.string.vrkey_011).split(","));
            put(SETTING_PHONE, mContext.getString(R.string.vrkey_012).split(","));
            put(SETTING_CAR_SAFETY, mContext.getString(R.string.vrkey_013).split(","));
            put(SETTING_THEME, mContext.getString(R.string.vrkey_014).split(","));
            put(SETTING_SOUND_FX, mContext.getString(R.string.vrkey_015).split(","));
            put(SETTING_AUDIO, mContext.getString(R.string.vrkey_016).split(","));
            if(spec.tunerFunctionSettingSupported) {
                put(SETTING_RADIO, mContext.getString(R.string.vrkey_017).split(","));
            }
            put(SETTING_DAB, mContext.getString(R.string.vrkey_032).split(","));
            if(spec.hdRadioFunctionSettingSupported) {
                put(SETTING_HD_RADIO, mContext.getString(R.string.vrkey_033).split(","));
            }
            put(SETTING_FUNCTION, mContext.getString(R.string.vrkey_018).split(","));
            put(SETTING_INFO, mContext.getString(R.string.vrkey_020).split(","));
        }};
    }

    /**
     * ソース名と判定用のワード群のマップ取得.
     *
     * @return ソース名と判定用のワード群のマップ
     */
    private Map<String, String[]> getSourceTypeWordMap() {
        return new HashMap<String, String[]>() {{
            if(mCurrentAvailableSourceType.contains(MediaSourceType.RADIO)) {
                put(SOURCE_RADIO, mContext.getString(R.string.vrkey_021).split(","));
            }
            put(SOURCE_DAB, mContext.getString(R.string.vrkey_034).split(","));
            if(mCurrentAvailableSourceType.contains(MediaSourceType.HD_RADIO)) {
                put(SOURCE_HD_RADIO, mContext.getString(R.string.vrkey_035).split(","));
            }
            put(SOURCE_TI, mContext.getString(R.string.vrkey_030).split(","));
            put(SOURCE_CD, mContext.getString(R.string.vrkey_024).split(","));
            put(SOURCE_USB, mContext.getString(R.string.vrkey_023).split(","));
            put(SOURCE_AUX, mContext.getString(R.string.vrkey_029).split(","));
            put(SOURCE_BT_AUDIO, mContext.getString(R.string.vrkey_027).split(","));
            put(SOURCE_PANDORA, mContext.getString(R.string.vrkey_025).split(","));
            put(SOURCE_SPOTIFY, mContext.getString(R.string.vrkey_026).split(","));
            put(SOURCE_APP_MUSIC, mContext.getString(R.string.vrkey_028).split(","));
            put(SOURCE_SIRIUS_XM, mContext.getString(R.string.vrkey_022).split(","));
            put(SOURCE_OFF, mContext.getString(R.string.vrkey_031).split(","));
        }};
    }

    /**
     * 判定結果.
     */
    public static class JudgeResult {
        /**
         * 音声認識コマンド.
         */
        public VoiceCommand mVoiceCommand;

        /**
         * 検索内容.
         * <p>
         * 音声認識コマンドに対する検索内容
         * nullの場合は検索内容の発話を促す必要がある
         * コマンドによって配列の参照方法が異なる
         * <pre>
         * グローバルサーチ系
         * 配列の先頭を使用して検索する
         *  Phone:電話発信対象者の名前
         *  Audio:オーディオソース名
         *  Setting:設定分類名
         * 配列を使用して検索する
         *  Navi:目的地
         * </pre>
         * <pre>
         * ローカルサーチ系
         * 配列を使用して検索する
         *  Artist:アーティスト名
         *  Album:アルバム名
         *  Song:曲名
         * </pre>
         */
        public String[] mSearchWords;

        /**
         * コンストラクタ.
         *
         * @param command     音声認識コマンド
         * @param searchWords 検索内容
         * @throws NullPointerException {@code command}がnull
         */
        public JudgeResult(@NonNull VoiceCommand command,
                           @Nullable String[] searchWords) {
            mVoiceCommand = VoiceCommand.valueOf(checkNotNull(command).name());
            mSearchWords = searchWords == null ? null : searchWords.clone();
        }

        /// MARK - オーディオソース名

        /**
         * ソース名：ラジオ.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればラジオソースを表示する
         */
        public static final String SOURCE_RADIO = "radio";
        /**
         * ソース名：DAB.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればDABソースを表示する
         */
        public static final String SOURCE_DAB = "dab";
        /**
         * ソース名：HDラジオ.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればHDラジオソースを表示する
         */
        public static final String SOURCE_HD_RADIO = "hd_radio";
        /**
         * ソース名：TI.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればTIソースを表示する
         */
        public static final String SOURCE_TI = "ti";
        /**
         * ソース名：CD.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればCDソースを表示する
         */
        public static final String SOURCE_CD = "cd";
        /**
         * ソース名：USB.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればUSBソースを表示する
         */
        public static final String SOURCE_USB = "usb";
        /**
         * ソース名：AUX.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればAUXソースを表示する
         */
        public static final String SOURCE_AUX = "aux";
        /**
         * ソース名：BT Audio.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればBT Audioソースを表示する
         */
        public static final String SOURCE_BT_AUDIO = "bt_audio";
        /**
         * ソース名：Pandora.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればPandoraソースを表示する
         */
        public static final String SOURCE_PANDORA = "pandora";
        /**
         * ソース名：Spotify.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればSpotifyソースを表示する
         */
        public static final String SOURCE_SPOTIFY = "spotify";
        /**
         * ソース名：ローカルコンテンツ.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればローカルコンテンツソースを表示する
         */
        public static final String SOURCE_APP_MUSIC = "app_music";
        /**
         * ソース名：Sirius XM.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればSirius XMソースを表示する
         */
        public static final String SOURCE_SIRIUS_XM = "sirius";
        /**
         * ソース名：ソースOFF.
         * <p>
         * 音声コマンドがAUDIO({@link VoiceCommand#AUDIO})の場合に検索内容と一致していればソースOFFソースを表示する
         */
        public static final String SOURCE_OFF = "off";

        /// MARK - 設定分類名

        /**
         * 設定分類：システム.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればシステム設定画面を表示する
         */
        public static final String SETTING_SYSTEM = "system";
        /**
         * 設定分類：Voice.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればVoice設定画面を表示する
         */
        public static final String SETTING_VOICE = "voice_recognition";
        /**
         * 設定分類：Navi.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればNavi設定画面を表示する
         */
        public static final String SETTING_NAVI = "navigation";
        /**
         * 設定分類：Message.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればMessage設定画面を表示する
         */
        public static final String SETTING_MESSAGE = "message";
        /**
         * 設定分類：Phone.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればPhone設定画面を表示する
         */
        public static final String SETTING_PHONE = "phone";
        /**
         * 設定分類：Car Safety.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればCar Safety設定画面を表示する
         */
        public static final String SETTING_CAR_SAFETY = "car_safety";
        /**
         * 設定分類：テーマ.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればテーマ設定画面を表示する
         */
        public static final String SETTING_THEME = "theme";
        /**
         * 設定分類：Sound FX.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればSound FX設定画面を表示する
         */
        public static final String SETTING_SOUND_FX = "sound_fx";
        /**
         * 設定分類：オーディオ.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればオーディオ設定画面を表示する
         */
        public static final String SETTING_AUDIO = "audio";
        /**
         * 設定分類：ラジオ.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればラジオ設定画面を表示する
         */
        public static final String SETTING_RADIO = "radio";
        /**
         * 設定分類：DAB.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればDAB設定画面を表示する
         */
        public static final String SETTING_DAB = "dab";
        /**
         * 設定分類：HDラジオ.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればHDラジオ設定画面を表示する
         */
        public static final String SETTING_HD_RADIO = "hd_radio";
        /**
         * 設定分類：Function.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればFunction設定画面を表示する
         */
        public static final String SETTING_FUNCTION = "app_function";
        /**
         * 設定分類：Information.
         * <p>
         * 音声コマンドがSETTING({@link VoiceCommand#SETTING})の場合に検索内容と一致していればInformation設定画面を表示する
         */
        public static final String SETTING_INFO = "information";

        /// MARK - 不明

        /**
         * 不明.
         */
        public static final String UNKNOWN = "unknown";
    }
}
