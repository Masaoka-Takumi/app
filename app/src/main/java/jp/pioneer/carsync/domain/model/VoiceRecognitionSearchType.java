package jp.pioneer.carsync.domain.model;

/**
 * 音声認識検索種別.
 */
public enum VoiceRecognitionSearchType {
    /**
     * グローバルサーチ.
     * <p>
     * 全画面が対象
     */
    GLOBAL(new VoiceCommand[]{
            VoiceCommand.NAVI, VoiceCommand.PHONE, VoiceCommand.AUDIO, VoiceCommand.SETTING
    }),
    /**
     * ローカルサーチ.
     * <p>
     * ローカルコンテンツ再生が対象
     * グローバルサーチに加えてアーティスト、アルバム、曲が検索対象となる
     */
    LOCAL(new VoiceCommand[]{
            VoiceCommand.NAVI, VoiceCommand.PHONE, VoiceCommand.AUDIO, VoiceCommand.SETTING,
            VoiceCommand.ARTIST, VoiceCommand.ALBUM, VoiceCommand.SONG
    })
    ;

    /** 有効なコマンド(枕詞). */
    public final VoiceCommand[] enabledCommands;

    /**
     * コンストラクタ.
     *
     * @param enabledCommands 有効なコマンド(枕詞)
     */
    VoiceRecognitionSearchType(VoiceCommand[] enabledCommands){
        this.enabledCommands = enabledCommands;
    }
}
