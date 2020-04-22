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
    PANDORA("com.pandora.android",1,"Pandora"),
    /** Spotify. */
    SPOTIFY("com.spotify.music",2,"Spotify"),
    /** Deezer. */
    DEEZER("deezer.android.app",3,"Deezer"),
    /** Last.fm. */
    LAST_FM("fm.last.android",4,"Last.fm"),
    /** MOG. */
    MOG("com.mog.android",5,"MOG"),
    /** LiveXLive. */
    SLACKER("com.slacker.radio",6,"Slacker Radio"),
    /** SoundCloud. */
    SOUND_CLOUD("com.soundcloud.android",7, "SoundCloud"),
    /** Youtube Music. */
    YOUTUBE_MUSIC("com.google.android.apps.youtube.music",8, "Youtube Music"),
    /** Sing! カラオケ. */
    SING_KARAOKE("com.smule.singandroid",9, "Sing!"),
    /** Google Play Music. */
    GOOGLE_PLAY_MUSIC("com.google.android.music",10, "Google Play Music"),
    /** Mixcloud. */
    MIX_CLOUD("com.mixcloud.player",11, "Mixcloud"),
    /** Saavn. */
    SAAVN("com.saavn.android",12, "Saavn"),
    /** TuneIn Radio. */
    TUNELN_RADIO("tunein.player",13, "TuneIn Radio"),
    /** iHeartRadio. */
    IHEART_RADIO("com.clearchannel.iheartradio.controller",14, "iHeartRadio"),
    /** Qello Concerts. */
    QELLO_CONCERTS("com.qello.handheld",15, "Qello Concerts"),
    /** Tidal. */
    TIDAL("com.aspiro.tidal",16, "TIDAL"),
    /** Hungama. */
    HUNGAMA("com.hungama.myplay.activity",17, "Hungama"),
    /** 8Tracks. */
    EIGHT_TRACKS("com.e8tracks",18, "8tracks"),
    /** Jango. */
    JANGO("com.jangomobile.android",19, "Jango Radio"),
    /** Radio Tunes. */
    RADIO_TUNES("com.audioaddict.sky",20, "RadioTunes"),
    /** Gaana. */
    GAANA("com.gaana",21, "Gaana"),
    /** Raaga. */
    RAAGA("com.raaga.android",22, "Raaga"),
    /** radiko. */
    RADIKO("jp.radiko.Player",23, "Radiko.jp"),
    /** Napster. */
    NAPSTER("com.rhapsody.napster",24, "Napster"),
    /** Anghami. */
    ANGHAMI("com.anghami",25, "Anghami"),
    /** DragonFli. */
    DRAGON_FLI("com.guvera.android",26, "Guvera"),
    /** Earbits. */
    EARBITS("com.earbits.earbitsradio",27, "Earbits Radio"),
    /** MIXTRAX. */
    MIXTRAX("jp.pioneer.mle.android.mixtrax",28, "Mixtrax"),
    /** Claro Musica. */
    CLARO_MUSICA("com.claro.claromusica.latam",29, "Claro Musica"),
    /** Radionomy. */
    RADIONOMY("com.mobile.radionomy",31, "Radionomy"),
    /** Sticher. */
    STICHER("com.stitcher.app",32, "Stitcher"),
    /** SiriusXM. */
    SIRIUS_XM("com.sirius",33, "SiriusXM"),
    /** Amazon Music. */
    AMAZON_MUSIC("com.amazon.mp3",34, "Amazon Music"),
    /** Yandex Music. */
    YANDEX_MUSIC("ru.yandex.music",35, "Yandex.Music"),
    /** VK Music. */
    VK_MUSIC("com.vk.music.remote",36, "VK Music"),
    /** Musixmatch. */
    MUSIXMATCH("com.musixmatch.android.lyrify",37, "Musixmatch"),
    /** StarMaker. */
    STAR_MAKER("com.starmakerinteractive.starmaker",38, "StarMaker"),
    /** Wynk Music. */
    WYNK_MUSIC("com.bsbportal.music",39, "Wynk Music"),
    /** JioMusic. */
    JIO_MUSIC("com.jio.media.jiobeats",40, "JioMusic"),
    /** AWA. */
    AWA("fm.awa.liverpool",41, "AWA"),
    /** JOOX Music. */
    JOOX_MUSIC("com.tencent.ibg.joox",42, "JOOX Music"),
    /** KKBOX. */
    KKBOX("com.skysoft.kkbox.android",43, "KKBOX"),
    /** LINE Music. */
    LINE_MUSIC("jp.linecorp.linemusic.android",44, "LINE MUSIC"),
    /** ミュージッククルーズチャンネル2 (MCC2) . */
    MCC2("jp.pioneer.mle.rcapp16",45, "ミュージッククルーズチャンネル2 (MCC2) "),
    /** Apple Music */
    APPLE_MUSIC("com.apple.android.music",46, "Apple Music"),;
    private String mPackageName;
    private int mNumber;
    private String mAppName;
    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    MusicApp(String packageName, int number, @NonNull String appName) {
        mPackageName = packageName;
        mNumber = number;
        mAppName = appName;
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
     * Analytics用App名取得.
     *
     * @return App名
     */
    @NonNull
    public String getAppName() {
        return mAppName;
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
