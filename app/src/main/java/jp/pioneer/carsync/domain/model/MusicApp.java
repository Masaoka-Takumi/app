package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 音楽アプリ.
 * <p>
 * 本アプリと連携する音楽アプリの定義。
 */
public enum MusicApp {
    /** Pandora. */
    PANDORA("com.pandora.android",1),
    /** Spotify. */
    SPOTIFY("com.spotify.music",2),
    /** Deezer. */
    DEEZER("deezer.android.app",3),
    /** Last.fm. */
    LAST_FM("fm.last.android",4),
    /** MOG. */
    MOG("com.mog.android",5),
    /** LiveXLive. */
    SLACKER("com.slacker.radio",6),
    /** SoundCloud. */
    SOUND_CLOUD("com.soundcloud.android",7),
    /** Youtube Music. */
    YOUTUBE_MUSIC("com.google.android.apps.youtube.music",8),
    /** Sing! カラオケ. */
    SING_KARAOKE("com.smule.singandroid",9),
    /** Google Play Music. */
    GOOGLE_PLAY_MUSIC("com.google.android.music",10),
    /** Mixcloud. */
    MIX_CLOUD("com.mixcloud.player",11),
    /** Saavn. */
    SAAVN("com.saavn.android",12),
    /** TuneIn Radio. */
    TUNELN_RADIO("tunein.player",13),
    /** iHeartRadio. */
    IHEART_RADIO("com.clearchannel.iheartradio.controller",14),
    /** Qello Concerts. */
    QELLO_CONCERTS("com.qello.handheld",15),
    /** Tidal. */
    TIDAL("com.aspiro.tidal",16),
    /** Hungama. */
    HUNGAMA("com.hungama.myplay.activity",17),
    /** 8Tracks. */
    EIGHT_TRACKS("com.e8tracks",18),
    /** Jango. */
    JANGO("com.jangomobile.android",19),
    /** Radio Tunes. */
    RADIO_TUNES("com.audioaddict.sky",20),
    /** Gaana. */
    GAANA("com.gaana",21),
    /** Raaga. */
    RAAGA("com.raaga.android",22),
    /** radiko. */
    RADIKO("jp.radiko.Player",23),
    /** Napster. */
    NAPSTER("com.rhapsody.napster",24),
    /** Anghami. */
    ANGHAMI("com.anghami",25),
    /** DragonFli. */
    DRAGON_FLI("com.guvera.android",26),
    /** Earbits. */
    EARBITS("com.earbits.earbitsradio",27),
    /** MIXTRAX. */
    MIXTRAX("jp.pioneer.mle.android.mixtrax",28),
    /** Claro Musica. */
    CLARO_MUSICA("com.claro.claromusica.latam",29),
    /** Radionomy. */
    RADIONOMY("com.mobile.radionomy",31),
    /** Sticher. */
    STICHER("com.stitcher.app",32),
    /** SiriusXM. */
    SIRIUS_XM("com.sirius",33),
    /** Amazon Music. */
    AMAZON_MUSIC("com.amazon.mp3",34),
    /** Yandex Music. */
    YANDEX_MUSIC("ru.yandex.music",35),
    /** VK Music. */
    VK_MUSIC("com.vk.music.remote",36),
    /** Musixmatch. */
    MUSIXMATCH("com.musixmatch.android.lyrify",37),
    /** StarMaker. */
    STAR_MAKER("com.starmakerinteractive.starmaker",38),
    /** Wynk Music. */
    WYNK_MUSIC("com.bsbportal.music",39),
    /** JioMusic. */
    JIO_MUSIC("com.jio.media.jiobeats",40),
    /** AWA. */
    AWA("fm.awa.liverpool",41),
    /** JOOX Music. */
    JOOX_MUSIC("com.tencent.ibg.joox",42),
    /** KKBOX. */
    KKBOX("com.skysoft.kkbox.android",43),
    /** LINE Music. */
    LINE_MUSIC("jp.linecorp.linemusic.android",44),
    /** ミュージッククルーズチャンネル2 (MCC2) . */
    MCC2("jp.pioneer.mle.rcapp16",45),
    /** Apple Music */
    APPLE_MUSIC("com.apple.android.music",46),;
    private String mPackageName;
    private int mNumber;
    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    MusicApp(String packageName, int number) {
        mPackageName = packageName;
        mNumber = number;
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

    public int getNumber() {
        return mNumber;
    }

    /**
     * パッケージ名から{@link MusicApp}取得.
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MusicApp}
     * @throws NullPointerException     {@code packageName}がnull
     * @throws IllegalArgumentException パッケージ名に該当するものがない
     * @see #fromPackageNameNoThrow(String)
     */
    @NonNull
    public static MusicApp fromPackageName(@NonNull String packageName) {
        MusicApp musicApp = fromPackageNameNoThrow(packageName);
        if (musicApp == null) {
            throw new IllegalArgumentException("invalid packageName: " + packageName);
        }

        return musicApp;
    }

    /**
     * パッケージ名から{@link MusicApp}取得.
     * <p>
     * パッケージ名に該当するものがない場合にnullを返してほしい場合に使用する。
     *
     * @param packageName パッケージ名
     * @return パッケージ名に該当する {@link MusicApp}。該当するものがない場合はnull。
     * @throws NullPointerException {@code packageName}がnull
     * @see #fromPackageName(String)
     */
    @Nullable
    public static MusicApp fromPackageNameNoThrow(@NonNull String packageName) {
        checkNotNull(packageName);

        for (MusicApp musicApp : MusicApp.values()) {
            if (packageName.equals(musicApp.getPackageName())) {
                return musicApp;
            }
        }

        return null;
    }

}
