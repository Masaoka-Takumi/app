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
    PANDORA("com.pandora.android"),
    /** Spotify. */
    SPOTIFY("com.spotify.music"),
    /** Deezer. */
    DEEZER("deezer.android.app"),
    /** Last.fm. */
    LAST_FM("fm.last.android"),
    /** MOG. */
    MOG("com.mog.android"),
    /** LiveXLive. */
    SLACKER("com.slacker.radio"),
    /** SoundCloud. */
    SOUND_CLOUD("com.soundcloud.android"),
    /** Youtube Music. */
    YOUTUBE_MUSIC("com.google.android.apps.youtube.music"),
    /** Sing! カラオケ. */
    SING_KARAOKE("com.smule.singandroid"),
    /** Google Play Music. */
    GOOGLE_PLAY_MUSIC("com.google.android.music"),
    /** Mixcloud. */
    MIX_CLOUD("com.mixcloud.player"),
    /** Saavn. */
    SAAVN("com.saavn.android"),
    /** TuneIn Radio. */
    TUNELN_RADIO("tunein.player"),
    /** iHeartRadio. */
    IHEART_RADIO("com.clearchannel.iheartradio.controller"),
    /** Qello Concerts. */
    QELLO_CONCERTS("com.qello.handheld"),
    /** Tidal. */
    TIDAL("com.aspiro.tidal"),
    /** Hungama. */
    HUNGAMA("com.hungama.myplay.activity"),
    /** 8Tracks. */
    EIGHT_TRACKS("com.e8tracks"),
    /** Jango. */
    JANGO("com.jangomobile.android"),
    /** Radio Tunes. */
    RADIO_TUNES("com.audioaddict.sky"),
    /** Gaana. */
    GAANA("com.gaana"),
    /** Raaga. */
    RAAGA("com.raaga.android"),
    /** radiko. */
    RADIKO("jp.radiko.Player"),
    /** Napster. */
    NAPSTER("com.rhapsody.napster"),
    /** Anghami. */
    ANGHAMI("com.anghami"),
    /** DragonFli. */
    DRAGON_FLI("com.guvera.android"),
    /** Earbits. */
    EARBITS("com.earbits.earbitsradio"),
    /** MIXTRAX. */
    MIXTRAX("jp.pioneer.mle.android.mixtrax"),
    /** Claro Musica. */
    CLARO_MUSICA("com.claro.claromusica.latam"),
    /** Radionomy. */
    RADIONOMY("com.mobile.radionomy"),
    /** Sticher. */
    STICHER("com.stitcher.app"),
    /** SiriusXM. */
    SIRIUS_XM("com.sirius"),
    /** Amazon Music. */
    AMAZON_MUSIC("com.amazon.mp3"),
    /** Yandex Music. */
    YANDEX_MUSIC("ru.yandex.music"),
    /** VK Music. */
    VK_MUSIC("com.vk.music.remote"),
    /** Musixmatch. */
    MUSIXMATCH("com.musixmatch.android.lyrify"),
    /** StarMaker. */
    STAR_MAKER("com.starmakerinteractive.starmaker"),
    /** Wynk Music. */
    WYNK_MUSIC("com.bsbportal.music"),
    /** JioMusic. */
    JIO_MUSIC("com.jio.media.jiobeats"),
    /** AWA. */
    AWA("fm.awa.liverpool"),
    /** JOOX Music. */
    JOOX_MUSIC("com.tencent.ibg.joox"),
    /** KKBOX. */
    KKBOX("com.skysoft.kkbox.android"),
    /** LINE Music. */
    LINE_MUSIC("jp.linecorp.linemusic.android"),
    /** ミュージッククルーズチャンネル2 (MCC2) . */
    MCC2("jp.pioneer.mle.rcapp16"),
    /** Apple Music */
    APPLE_MUSIC("com.apple.android.music"),;
    private String mPackageName;

    /**
     * コンストラクタ.
     *
     * @param packageName パッケージ名
     */
    MusicApp(String packageName) {
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
