package jp.pioneer.carsync.domain.interactor;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.domain.model.VoiceRecognitionSearchType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;

/**
 * Created by NSW00_008320 on 2017/05/08.
 */
@RunWith(Theories.class)
public class JudgeVoiceCommandTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks JudgeVoiceCommand mJudgeVoiceCommand;
    @Mock Context mContext;

    // MARK - コマンド系(共通含む)
    private static final String[] NAVIGATION_WORD ={"なび","ナビ","navi","Navi"};
    private static final String[] PHONE_WORD      ={"ふぉん","フォン","phone","Phone"};
    private static final String[] AUDIO_WORD      ={"おーでぃお","オーディオ","audio","Audio"};
    private static final String[] SETTING_WORD    ={"せってぃんぐ","セッティング","setting","Setting"};
    private static final String[] ARTIST_WORD     ={"あーてぃすと","アーティスト","artist","Artist"};
    private static final String[] ALBUM_WORD      ={"あるばむ","アルバム","album","Album"};
    private static final String[] SONG_WORD       ={"そんぐ","ソング","song","Song"};
    // MARK - 共通
    private static final String[] RADIO_WORD      ={"らじお","ラジオ","radio","Radio"};
    // MARK - 設定系
    private static final String[] SYSTEM_WORD     ={"しすてむ","システム","system","System"};
    private static final String[] VOICE_WORD      ={"ぼいす","ボイス","voice","Voice"};
    private static final String[] MESSAGE_WORD    ={"めっせーじ","メッセージ","message","Message"};
    private static final String[] CAR_SAFETY_WORD ={"かー","カー","car","Car"};
    private static final String[] THEME_WORD      ={"てーま","テーマ","Theme","theme"};
    private static final String[] SOUND_FX_WORD   ={"さうんど","サウンド","sound","Sound"};
    private static final String[] FUNCTION_WORD   ={"ふぁんくしょん","ファンクション","function","Function"};
    private static final String[] INFO_WORD       ={"いんふぉ","インフォ","info","Info"};
    // MARK - ソース系
    private static final String[] CD_WORD         ={"しーでぃ","シーディ","cd","Cd","CD"};
    private static final String[] USB_WORD        ={"ゆーえすびー","ユーエスビー","usb","Usb","USB"};
    private static final String[] AUX_WORD        ={"えーゆーえっくす","エーユーエックス","aux","Aux","AUX"};
    private static final String[] BT_AUDIO_WORD   ={"びーてぃ","ビーティ","bt","Bt","BT"};
    private static final String[] PANDORA_WORD    ={"ぱんどら","パンドラ","pandora","Pandora"};
    private static final String[] SPOTIFY_WORD    ={"すぽてぃふぁい","スポティファイ","spotify","Spotify"};
    private static final String[] APP_MUSIC_WORD  ={"みゅーじっく","ミュージック","music","Music"};
    private static final String[] SIRIUS_WORD     ={"しりうす","シリウス","sirius","Sirius"};
    private static final String[] SOURCE_OFF_WORD ={"おふ","オフ","off","Off", "OFF"};

    static class Fixture {
        ArrayList<String> recognizeResults;
        VoiceCommand expected_command;
        String expected_text;

        Fixture(ArrayList<String> recognizeResults, VoiceCommand expected_command, String expected_text) {
            this.recognizeResults = recognizeResults;
            this.expected_command = expected_command;
            this.expected_text = expected_text;
        }
    }

    static class GlobalContentsFixture extends Fixture{
        GlobalContentsFixture(ArrayList<String> recognizeResults, VoiceCommand expected_command, String expected_text){
            super(recognizeResults, expected_command, expected_text);
        }
    }

    static class LocalContentsFixture extends Fixture{
        LocalContentsFixture(ArrayList<String> recognizeResults, VoiceCommand expected_command, String expected_text){
            super(recognizeResults, expected_command, expected_text);
        }
    }

    @DataPoints
    public static final GlobalContentsFixture[] FIXTURES = new GlobalContentsFixture[]{
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("navi", "TEST", "TEST", "TEST")),
                    VoiceCommand.NAVI, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("naviTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.NAVI, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "Navi", "TEST", "TEST")),
                    VoiceCommand.NAVI, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "NaviTEST", "TEST", "TEST")),
                    VoiceCommand.NAVI, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "なび", "TEST")),
                    VoiceCommand.NAVI, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "なびTEST", "TEST")),
                    VoiceCommand.NAVI, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "ナビ")),
                    VoiceCommand.NAVI, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "ナビTEST")),
                    VoiceCommand.NAVI, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("phone", "TEST", "TEST", "TEST")),
                    VoiceCommand.PHONE, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("phoneTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.PHONE, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "Phone", "TEST", "TEST")),
                    VoiceCommand.PHONE, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "PhoneTEST", "TEST", "TEST")),
                    VoiceCommand.PHONE, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "ふぉん", "TEST")),
                    VoiceCommand.PHONE, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "ふぉんTEST", "TEST")),
                    VoiceCommand.PHONE, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "フォン")),
                    VoiceCommand.PHONE, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "フォンTEST")),
                    VoiceCommand.PHONE, "TEST"),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("おーでぃお", "TEST", "TEST", "TEST")),
                    VoiceCommand.AUDIO, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("おーでぃおTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "オーディオ", "TEST", "TEST")),
                    VoiceCommand.AUDIO, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "オーディオTEST", "TEST", "TEST")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "audio", "TEST")),
                    VoiceCommand.AUDIO, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "audioTEST", "TEST")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "Audio")),
                    VoiceCommand.AUDIO, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "AudioTEST")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("せってぃんぐ", "TEST", "TEST", "TEST")),
                    VoiceCommand.SETTING, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("せってぃんぐTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "セッティング", "TEST", "TEST")),
                    VoiceCommand.SETTING, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "セッティングTEST", "TEST", "TEST")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "setting", "TEST")),
                    VoiceCommand.SETTING, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "settingTEST", "TEST")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "Setting")),
                    VoiceCommand.SETTING, null),
            new GlobalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "SettingTEST")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.UNKNOWN),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioしーでぃ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_CD),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioシーディ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_CD),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiocd")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_CD),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioCd")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_CD),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioCD")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_CD),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioゆーえすびー")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_USB),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioユーエスビー")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_USB),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiousb")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_USB),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioUsb")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_USB),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioUSB")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_USB),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioえーゆーえっくす")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_AUX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioエーユーエックス")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_AUX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioaux")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_AUX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioAux")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_AUX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioAUX")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_AUX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioびーてぃ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioビーティ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiobt")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioBt")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioBT")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_BT_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioぱんどら")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_PANDORA),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioパンドラ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_PANDORA),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiopandora")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_PANDORA),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioPandora")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_PANDORA),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioすぽてぃふぁい")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SPOTIFY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioスポティファイ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SPOTIFY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiospotify")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SPOTIFY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioSpotify")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SPOTIFY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioみゅーじっく")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_APP_MUSIC),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioミュージック")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_APP_MUSIC),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiomusic")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_APP_MUSIC),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioMusic")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_APP_MUSIC),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioらじお")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioラジオ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioradio")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioRadio")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioしりうす")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SIRIUS_XM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioシリウス")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SIRIUS_XM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiosirius")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SIRIUS_XM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioSirius")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_SIRIUS_XM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioおふ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_OFF),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audioオフ")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_OFF),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Audiooff")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_OFF),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioOff")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_OFF),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("AudioOFF")),
                    VoiceCommand.AUDIO, JudgeVoiceCommand.JudgeResult.SOURCE_OFF),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingしすてむ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingシステム")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingsystem")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingSystem")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingぼいす")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_VOICE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingボイス")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_VOICE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingvoice")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_VOICE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingVoice")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_VOICE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingめっせーじ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_MESSAGE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingめっせーじ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_MESSAGE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingmessage")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_MESSAGE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingMessage")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_MESSAGE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingかー")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_CAR_SAFETY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingカー")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_CAR_SAFETY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingcar")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_CAR_SAFETY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingCar")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_CAR_SAFETY),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingてーま")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_THEME),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingテーマ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_THEME),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingtheme")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_THEME),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingTheme")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_THEME),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingさうんど")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SOUND_FX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingサウンド")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SOUND_FX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingsound")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SOUND_FX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingSound")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_SOUND_FX),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingふぁんくしょん")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_FUNCTION),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingファンクション")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_FUNCTION),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingfunction")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_FUNCTION),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingFunction")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_FUNCTION),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingいんふぉ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_INFO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingインフォ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_INFO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settinginfo")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_INFO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingInfo")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_INFO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingふぉん")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_PHONE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingフォン")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_PHONE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingphone")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_PHONE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingPhone")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_PHONE),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingおーでぃお")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingオーディオ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingaudio")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingAudio")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_AUDIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingらじお")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingラジオ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingradio")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingRadio")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_RADIO),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingなび")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_NAVI),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingナビ")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_NAVI),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("Settingnavi")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_NAVI),
            new GlobalContentsFixture(new ArrayList<>(Collections.singletonList("SettingNavi")),
                    VoiceCommand.SETTING, JudgeVoiceCommand.JudgeResult.SETTING_NAVI)
    };

    @DataPoints
    public static final LocalContentsFixture[] MUSIC_FIXTURES = new LocalContentsFixture[]{
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("あーてぃすと", "TEST", "TEST", "TEST")),
                    VoiceCommand.ARTIST, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("あーてぃすとTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.ARTIST, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "アーティスト", "TEST", "TEST")),
                    VoiceCommand.ARTIST, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "アーティストTEST", "TEST", "TEST")),
                    VoiceCommand.ARTIST, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "artist", "TEST")),
                    VoiceCommand.ARTIST, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "artistTEST", "TEST")),
                    VoiceCommand.ARTIST, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "Artist")),
                    VoiceCommand.ARTIST, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "ArtistTEST")),
                    VoiceCommand.ARTIST, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("あるばむ", "TEST", "TEST", "TEST")),
                    VoiceCommand.ALBUM, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("あるばむTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.ALBUM, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "アルバム", "TEST", "TEST")),
                    VoiceCommand.ALBUM, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "アルバムTEST", "TEST", "TEST")),
                    VoiceCommand.ALBUM, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "album", "TEST")),
                    VoiceCommand.ALBUM, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "albumTEST", "TEST")),
                    VoiceCommand.ALBUM, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "Album")),
                    VoiceCommand.ALBUM, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "AlbumTEST")),
                    VoiceCommand.ALBUM, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("そんぐ", "TEST", "TEST", "TEST")),
                    VoiceCommand.SONG, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("そんぐTEST", "TEST", "TEST", "TEST")),
                    VoiceCommand.SONG, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "ソング", "TEST", "TEST")),
                    VoiceCommand.SONG, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "ソングTEST", "TEST", "TEST")),
                    VoiceCommand.SONG, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "song", "TEST")),
                    VoiceCommand.SONG, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "songTEST", "TEST")),
                    VoiceCommand.SONG, "TEST"),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "Song")),
                    VoiceCommand.SONG, null),
            new LocalContentsFixture(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "SongTEST")),
                    VoiceCommand.SONG, "TEST"),
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        Resources resources = mock(Resources.class);
        when(resources.getStringArray(eq(R.array.judge_navigation_word))).thenReturn(NAVIGATION_WORD);
        when(resources.getStringArray(eq(R.array.judge_phone_word))).thenReturn(PHONE_WORD);
        when(resources.getStringArray(eq(R.array.judge_audio_word))).thenReturn(AUDIO_WORD);
        when(resources.getStringArray(eq(R.array.judge_radio_word))).thenReturn(RADIO_WORD);
        when(resources.getStringArray(eq(R.array.judge_setting_word))).thenReturn(SETTING_WORD);
        when(resources.getStringArray(eq(R.array.judge_artist_word))).thenReturn(ARTIST_WORD);
        when(resources.getStringArray(eq(R.array.judge_album_word))).thenReturn(ALBUM_WORD);
        when(resources.getStringArray(eq(R.array.judge_song_word))).thenReturn(SONG_WORD);
        when(resources.getStringArray(eq(R.array.judge_system_setting_word))).thenReturn(SYSTEM_WORD);
        when(resources.getStringArray(eq(R.array.judge_voice_recognition_setting_word))).thenReturn(VOICE_WORD);
        when(resources.getStringArray(eq(R.array.judge_message_setting_word))).thenReturn(MESSAGE_WORD);
        when(resources.getStringArray(eq(R.array.judge_car_safety_setting_word))).thenReturn(CAR_SAFETY_WORD);
        when(resources.getStringArray(eq(R.array.judge_theme_setting_word))).thenReturn(THEME_WORD);
        when(resources.getStringArray(eq(R.array.judge_sound_fx_setting_word))).thenReturn(SOUND_FX_WORD);
        when(resources.getStringArray(eq(R.array.judge_app_function_setting_word))).thenReturn(FUNCTION_WORD);
        when(resources.getStringArray(eq(R.array.judge_information_setting_word))).thenReturn(INFO_WORD);
        when(resources.getStringArray(eq(R.array.judge_cd_source_word))).thenReturn(CD_WORD);
        when(resources.getStringArray(eq(R.array.judge_usb_source_word))).thenReturn(USB_WORD);
        when(resources.getStringArray(eq(R.array.judge_aux_source_word))).thenReturn(AUX_WORD);
        when(resources.getStringArray(eq(R.array.judge_bt_audio_source_word))).thenReturn(BT_AUDIO_WORD);
        when(resources.getStringArray(eq(R.array.judge_pandora_source_word))).thenReturn(PANDORA_WORD);
        when(resources.getStringArray(eq(R.array.judge_spotify_source_word))).thenReturn(SPOTIFY_WORD);
        when(resources.getStringArray(eq(R.array.judge_app_music_source_word))).thenReturn(APP_MUSIC_WORD);
        when(resources.getStringArray(eq(R.array.judge_sirius_source_word))).thenReturn(SIRIUS_WORD);
        when(resources.getStringArray(eq(R.array.judge_source_off_word))).thenReturn(SOURCE_OFF_WORD);
        when(mContext.getResources()).thenReturn(resources);
    }

    @Test
    public void execute_null() throws Exception {
        // exercise
        JudgeVoiceCommand.JudgeResult actual = mJudgeVoiceCommand.execute(new ArrayList<>(Arrays.asList("TEST", "TEST", "TEST", "TEST")), VoiceRecognitionSearchType.GLOBAL);

        // verify
        assertThat(actual,is(nullValue()));
    }

    @Theory
    public void execute(GlobalContentsFixture fixture) throws Exception {
        // exercise
        JudgeVoiceCommand.JudgeResult actual = mJudgeVoiceCommand.execute(fixture.recognizeResults, VoiceRecognitionSearchType.GLOBAL);

        // verify
        assertThat(actual.mVoiceCommand,is(fixture.expected_command));
        if(fixture.expected_text == null){
            assertThat(actual.mSearchWords, is(nullValue()));
        } else {
            assertThat(actual.mSearchWords[0], is(fixture.expected_text));
        }
    }

    @Theory
    public void execute_GlobalSearch(LocalContentsFixture fixture) throws Exception {
        // exercise
        JudgeVoiceCommand.JudgeResult actual = mJudgeVoiceCommand.execute(fixture.recognizeResults, VoiceRecognitionSearchType.GLOBAL);

        // verify
        assertThat(actual,is(nullValue()));
    }

    @Theory
    public void execute_LocalSearch(LocalContentsFixture fixture) throws Exception {
        // exercise
        JudgeVoiceCommand.JudgeResult actual = mJudgeVoiceCommand.execute(fixture.recognizeResults, VoiceRecognitionSearchType.LOCAL);

        // verify
        assertThat(actual.mVoiceCommand,is(fixture.expected_command));
        if(fixture.expected_text == null){
            assertThat(actual.mSearchWords, is(nullValue()));
        } else {
            assertThat(actual.mSearchWords[0], is(fixture.expected_text));
        }

    }

    @Test(expected = NullPointerException.class)
    public void execute_ArgCommandNull()throws Exception{
        // exercise
        mJudgeVoiceCommand.execute(null, VoiceRecognitionSearchType.LOCAL);

    }

    @Test(expected = NullPointerException.class)
    public void execute_ArgTypeNull()throws Exception{
        // exercise
        mJudgeVoiceCommand.execute(new ArrayList<>(), null);

    }

}