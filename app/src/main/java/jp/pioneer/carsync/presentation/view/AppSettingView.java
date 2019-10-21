package jp.pioneer.carsync.presentation.view;


/**
 * App設定画面の抽象クラス
 */
public interface AppSettingView {

    /**
     * ShortCutButton設定の有効/無効
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     */
    void setShortCutSettingEnabled(boolean isEnabled);

    /**
     * ShortCutButtonの設定
     *
     * @param isEnabled 有効/無効
     */
    void setShortCutEnabled(boolean isEnabled);

    /**
     * AlbumArt表示の設定
     *
     * @param isEnabled {@code true}:AlbumArt表示。｛@code false}:List表示。
     */
    void setAlbumArtEnabled(boolean isEnabled);

    /**
     * GenreCard表示の設定
     *
     * @param isEnabled {@code true}:Card表示。｛@code false}:List表示。
     */
    void setGenreCardEnabled(boolean isEnabled);

    /**
     * PlaylistCard表示の設定
     *
     * @param isEnabled {@code true}:Card表示。｛@code false}:List表示。
     */
    void setPlaylistCardEnabled(boolean isEnabled);

    /**
     * 常時待ち受け設定の表示/非表示
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     */
    void setAppServiceResidentEnabled(boolean isEnabled);
}
