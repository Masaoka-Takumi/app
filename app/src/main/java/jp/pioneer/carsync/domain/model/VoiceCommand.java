package jp.pioneer.carsync.domain.model;

import jp.pioneer.carsync.R;

/**
 * 音声認識コマンド.
 */
public enum VoiceCommand {
    /** Navi. */
    NAVI(R.string.vrkey_001),
    /** Phone. */
    PHONE(R.string.vrkey_002),
    /** Audio. */
    AUDIO(R.string.vrkey_004),
    /** Setting. */
    SETTING(R.string.vrkey_003),
    /** Artist. */
    ARTIST(R.string.vrkey_007),
    /** Album. */
    ALBUM(R.string.vrkey_006),
    /** Song. */
    SONG(R.string.vrkey_005)
    ;

    /** 枕詞のリソースid */
    public final int id;

    /**
     * コンストラクタ.
     *
     * @param id 枕詞のリソースid
     */
    VoiceCommand(int id){
        this.id = id;
    }

    /**
     * ローカルコンテンツ再生系のコマンドか否か.
     */
    public boolean isMusicCommand(){
        return this == ARTIST || this == ALBUM || this == SONG;
    }
}
