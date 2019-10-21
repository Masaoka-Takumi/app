package jp.pioneer.carsync.application.content;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * プロバイダのコントラクト.
 * <p>
 * 本アプリ内のコンテンツプロバイダのコントラクト。
 */
public class ProviderContract {
    /** コンテンツプロバイダのauthority. */
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    /**
     * お気に入り
     */
    public static class Favorite implements BaseColumns, FavoriteColumns {
        public static final String TABLE_NAME = "favorite";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        public static final String MIME_TYPE_SINGLE = "vnd.android.cursor.item/vnd.carsync." + TABLE_NAME;
        public static final String MIME_TYPE_MANY = "vnd.android.cursor.dir/vnd.carsync." + TABLE_NAME;
    }

    /**
     * ジャンル
     */
    public static class Genres implements BaseColumns, MediaStore.Audio.GenresColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + "genres");

        /**
         * ジャンル内曲
         */
        public static class Members implements  MediaStore.Audio.AudioColumns {
            public static Uri getContentUri(long genreId) {
                return Uri.parse("content://" + AUTHORITY + "/genres/" + genreId + "/members");
            }
        }
    }

    /**
     * アルバム
     */
    public static class Albums implements BaseColumns, MediaStore.Audio.AlbumColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + "albums");
    }

    /**
     * アーティストアルバム
     */
    public static class ArtistsAlbums implements MediaStore.Audio.AlbumColumns {
        public static Uri getArtistsAlbumsUri(long artistId) {
            return Uri.parse("content://" + AUTHORITY + "/artists/" + artistId + "/albums");
        }
    }


    /**
     * プレイリスト
     */
    public static class Playlists implements MediaStore.Audio.PlaylistsColumns {
        /**
         * プレイリスト内曲
         */
        public static class Members implements  MediaStore.Audio.AudioColumns {
            public static Uri getContentUri(long playlistId) {
                return Uri.parse("content://" + AUTHORITY + "/playlists/" + playlistId + "/members");
            }
        }
    }

    /**
     * Artworkエンドポイント.
     * <p>
     * 各カテゴリに応じたArtworkを取得するための統一した方法を提供する。
     * …というのは結果的にそうなっただけで、アーティスト、ジャンル、プレイリストのArtwork取得を
     * 上手いこと行うためである。
     * そもそも、Artworkはアルバムに紐づくため、アーティスト、ジャンル、プレイリストのArtworkは、
     * 以下の独自のルールにより決定したものを利用する。
     * <p>
     * アーティスト:<br>
     *  対象アーティストに関連するArtworkが存在するアルバム一覧を、アルバム名で昇順ソートした先頭アルバムのArtwork。
     * <p>
     * ジャンル:<br>
     *  対象ジャンル内の全曲に関連するArtworkが存在するアルバム一覧を、アルバム名で昇順ソートした先頭アルバムのArtwork。
     * <p>
     * プレイリスト:<br>
     *  対象プレイリスト内の全曲に関連するArtworkが存在するアルバム一覧を、アルバム名で昇順ソートした先頭アルバムのArtwork。
     * <p>
     * どの場合も少なくとも1回はクエリーを発行する必要があるため、バックグラウンドスレッドで処理
     * することを考慮すると、リスト画面は特に処理が複雑になる。<br>
     * そこで、Glideがバックグラウンドスレッドでデータ読み込みを行う特性を利用し、コンテンツプロバイダの
     * openFileのタイミングで上記処理を行うようにした。本エンドポイントは、そのために存在する。<br>
     * 曲とアルバムは不要ではあるが、合わせるために用意した。
     */
    public static class Artwork {
        /** ArtworkエンドポイントURI. */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/artwork");

        /**
         * 曲のArtwork URI取得.
         * <p>
         * 曲IDではなくアルバムIDを要求するのは、曲IDからアルバムIDを取得する処理を省略するためである。
         * {@link AppMusicContract.Song#getArtworkUri(Cursor)}を経由することを想定しているので、
         * 大元の利用者はアルバムIDを渡すことは意識する必要はない。
         *
         * @param albumId アルバムID
         * @return Artwork URI
         */
        @NonNull
        public static Uri getSongArtworkUri(long albumId) {
            return Uri.parse(CONTENT_URI + "/songs/" + albumId);
        }

        /**
         * アルバムのArtwork URI取得.
         *
         * @param albumId アルバムID
         * @return Artwork URI
         */
        @NonNull
        public static Uri getAlbumArtworkUri(long albumId) {
            return Uri.parse(CONTENT_URI + "/albums/" + albumId);
        }

        /**
         * アーティストのArtwork URI取得.
         *
         * @param artistId アーティストID
         * @return Artwork URI
         */
        @NonNull
        public static Uri getArtistArtworkUri(long artistId) {
            return Uri.parse(CONTENT_URI + "/artists/" + artistId);
        }

        /**
         * ジャンルのArtwork URI取得.
         *
         * @param genreId ジャンルID
         * @return Artwork URI
         */
        @NonNull
        public static Uri getGenreArtworkUri(long genreId) {
            return Uri.parse(CONTENT_URI + "/genres/" + genreId);
        }

        /**
         * プレイリストのArtwork URI取得.
         *
         * @param playlistId プレイリストID
         * @return Artwork URI
         */
        @NonNull
        public static Uri getPlaylistArtworkUri(long playlistId) {
            return Uri.parse(CONTENT_URI + "/playlists/" + playlistId);
        }
    }

    /**
     * お気に入りカラム定義
     */
    public interface FavoriteColumns {
        /**
         * 名前.
         */
        String NAME = "name";

        /**
         * 説明.
         */
        String DESCRIPTION = "description";

        /**
         * 車載機ソースのコード.
         *
         * @see jp.pioneer.carsync.domain.model.MediaSourceType#code
         */
        String SOURCE_ID = "source_id";

        /**
         * チューナー系チャンネルキー1.
         * <p>
         * 検索する場合はこれをキーに利用する。
         * Radio, DAB, HD Radioの場合: 周波数(kHz)
         * Sirius XMの場合: チャンネル番号
         */
        String TUNER_CHANNEL_KEY1 = "tuner_channel_key1";

        /**
         * チューナー系チャンネルキー2.
         * <p>
         * 検索する場合はこれをキーに利用する。
         * HD Radioの場合: HDチャンネル番号
         * その他のソース: null
         */
        String TUNER_CHANNEL_KEY2 = "tuner_channel_key2";

        /**
         * 周波数index.
         * <p>
         * Favorite情報通知の際に利用する。
         * Radio, DAB, HD Radio: index
         * その他のソース: null
         */
        String TUNER_FREQUENCY_INDEX = "tuner_frequency_index";

        /**
         * チューナー系ソースのバンドコード.
         * <p>
         * Favorite情報通知の際に利用する。
         * Radio, DAB, HD Radio, Sirius XM: バンドコード
         * その他のソース: null
         */
        String TUNER_BAND = "tuner_band";

        /**
         * チューナーパラメーター1.
         * <p>
         * Favorite情報通知の際に利用する。
         * Radioの場合: pi(ushort)
         * DABの場合: eid(ushort)
         * HD Radioの場合: multicastChannelNumber(ubyte)
         * Sirius XMの場合: SID(ushort)
         * その他のソース: null
         */
        String TUNER_PARAM1 = "tuner_param1";

        /**
         * チューナーパラメーター2.
         * <p>
         * Favorite情報通知の際に利用する。
         * Radio(AM)の場合: seekStep
         * Radio(AM以外)の場合: null
         * DABの場合: sid(uint)
         * HD Radioの場合: null
         * その他のソース: null
         */
        String TUNER_PARAM2 = "tuner_param2";

        /**
         * チューナーパラメーター3
         * <p>
         * Favorite情報通知の際に利用する。
         * Radioの場合: null
         * DABの場合: scids(ushort)
         * HD Radioの場合: null
         * その他のソース: null
         */
        String TUNER_PARAM3 = "tuner_param3";

        /**
         * 登録日時
         */
        String CREATE_DATE = "create_date";
    }
}
